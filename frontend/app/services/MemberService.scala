package services

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.agent.Agent

import org.joda.time.DateTime

import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api.http.Status.{OK, CREATED, NOT_FOUND}
import play.api.Play.current
import play.api.libs.ws.WS
import play.api.libs.json.{JsValue, Json, JsPath, Reads}
import play.api.libs.functional.syntax._

import com.gu.scalaforce.{Authentication, Scalaforce}

import configuration.Config
import model.{Tier, Member}
import model.Tier.Tier
import model.Eventbrite.{EBEvent, EBDiscount}
import model.Stripe.{Customer, Subscription}
import com.gu.identity.model.User

case class MemberServiceError(s: String) extends Throwable {
  override def getMessage: String = s
}

abstract class MemberService {
  val salesforce: Scalaforce

  object Keys {
    val ID = "Id"
    val LAST_NAME = "LastName"
    val USER_ID = "IdentityID__c"
    val CUSTOMER_ID = "Stripe_Customer_ID__c"
    val TIER = "Membership_Tier__c"
    val OPT_IN = "Membership_Opt_In__c"
    val CREATED = "CreatedDate"
    val EMAIL = "Email"
  }

  def contactURL(key: String, id: String): String = s"/services/data/v29.0/sobjects/Contact/$key/$id"

  implicit val readsDateTime = JsPath.read[String].map(s => new DateTime(s))
  implicit val readsMember: Reads[Member] = (
    (JsPath \ Keys.ID).read[String] and
      (JsPath \ Keys.USER_ID).read[String] and
      (JsPath \ Keys.TIER).read[String].map(Tier.withName) and
      (JsPath \ Keys.CUSTOMER_ID).read[Option[String]] and
      (JsPath \ Keys.CREATED).read[DateTime] and
      (JsPath \ Keys.OPT_IN).read[Boolean]
    )(Member.apply _)

  def update(member: Member): Future[Member] = {
    for {
      result <- salesforce.patch(
        contactURL(Keys.USER_ID, member.identityId),
        Json.obj(
          Keys.CUSTOMER_ID -> member.stripeCustomerId,
          Keys.LAST_NAME-> "LAST NAME",
          Keys.TIER -> member.tier.toString,
          Keys.OPT_IN -> member.optedIn
        )
      )
    } yield member
  }

  def insert(user: User, customerId: String, tier: Tier): Future[String] = {
    for {
      result <- salesforce.patch(
        contactURL(Keys.USER_ID, user.id),
        Json.obj(
          Keys.CUSTOMER_ID -> customerId,
          Keys.LAST_NAME -> "LAST NAME", // TODO: fill surname
          Keys.TIER -> tier.toString,
          Keys.EMAIL -> user.getPrimaryEmailAddress
        )
      )
    } yield {
      result.status match {
        case CREATED => (result.json \ "id").as[String]
        case code =>
          Logger.error(s"insert failed, Salesforce returned $code")
          throw MemberServiceError(s"Salesforce return $code")
      }
    }
  }

  private def getMember(key: String, id: String): Future[Option[Member]] = {
    for {
      result <- salesforce.get(contactURL(key, id))
    } yield {
      result.status match {
        case OK => Some(result.json.as[Member])
        case NOT_FOUND => None
        case code =>
          Logger.error(s"getMember failed, Salesforce returned $code")
          throw MemberServiceError(s"Salesforce returned $code")
      }
    }
  }

  def get(userId: String): Future[Option[Member]] = getMember(Keys.USER_ID, userId)
  def getByCustomerId(customerId: String): Future[Option[Member]] = getMember(Keys.CUSTOMER_ID, customerId)

  def createEventDiscount(userId: String, event: EBEvent): Future[Option[EBDiscount]] = {

    def createDiscountFor(memberOpt: Option[Member]): Option[Future[EBDiscount]] = {
      // code should be unique for each user/event combination
      memberOpt
        .filter(_.tier >= Tier.Partner)
        .map { member =>
          EventbriteService.createOrGetDiscount(event.id, DiscountCode.generate(s"${member.identityId}_${event.id}"))
        }
    }

    for {
      member <- get(userId)
      discount <- Future.sequence(createDiscountFor(member).toSeq)
    } yield discount.headOption
  }

  def cancelAnySubscriptionPayment(member: Member): Future[Option[Subscription]] = {
    def cancelSubscription(customer: Customer): Option[Future[Subscription]] = {
      for {
        paymentDetails <- customer.paymentDetails
      } yield {
        StripeService.Subscription.delete(customer.id, paymentDetails.subscription.id)
      }
    }

    member.stripeCustomerId.map { customerId =>
      for {
        customer <- StripeService.Customer.read(customerId)
        cancelledOpt = cancelSubscription(customer)
        cancelledSubscription <- Future.sequence(cancelledOpt.toSeq)
      } yield cancelledSubscription.headOption
    }.getOrElse(Future.successful(None))
  }

}

object MemberService extends MemberService {
  val authenticationAgent = Agent[Authentication](Authentication("", ""))

  val salesforce = new Scalaforce {
    val consumerKey: String = Config.salesforceConsumerKey
    val consumerSecret: String = Config.salesforceConsumerSecret

    val apiURL: String = Config.salesforceApiUrl
    val apiUsername: String = Config.salesforceApiUsername
    val apiPassword: String = Config.salesforceApiPassword
    val apiToken: String = Config.salesforceApiToken

    private def requestWithAuth(endpoint: String) = {
      val authentication = authenticationAgent.get()

      WS.url(authentication.instance_url + endpoint)
        .withHeaders("Authorization" -> s"Bearer ${authentication.access_token}")
    }

    def login(endpoint: String, params: Seq[(String, String)]) =
      WS.url(apiURL + endpoint).withQueryString(params: _*).post("")

    def get(endpoint: String) = {
      Logger.debug(s"MemberService: get $endpoint")

      val futureResult = requestWithAuth(endpoint).get()
      futureResult.foreach { result =>
        Logger.debug(s"MemberService: get response ${result.status}")
        Logger.debug(result.body)
      }
      futureResult
    }

    def patch(endpoint: String, body: JsValue) = {
      Logger.debug(s"MemberService: patch $endpoint")
      Logger.debug(Json.prettyPrint(body))

      val futureResult = requestWithAuth(endpoint).patch(body)
      futureResult.foreach { result =>
        Logger.debug(s"MemberService: patch response ${result.status}")
        Logger.debug(result.body)
      }
      futureResult
    }
  }

  private implicit val system = Akka.system

  def refresh() {
    Logger.debug("Refreshing Scalaforce token")
    authenticationAgent.sendOff(_ => {
      val token = Await.result(salesforce.getAuthentication, 15.seconds)
      Logger.debug(s"Got token $token")
      token
    })
  }

  def start() {
    Logger.info("Starting Scalaforce background tasks")
    system.scheduler.schedule(0.seconds, 2.hours) { refresh() }
  }
}