package model

import play.Logger
import play.api.libs.json.Json

import utils.TestUsers

case class IdMinimalUser(id: String, displayName: Option[String]) {
  lazy val isTestUser: Boolean = {
    val isValidTestUser = TestUsers.validate(this)
    if (isValidTestUser) {
      Logger.info(s"$id is a valid test user")
    }
    isValidTestUser
  }
}

case class IdUser(id: String,
                    primaryEmailAddress: String,
                    publicFields: PublicFields,
                    privateFields: PrivateFields,
                    statusFields: Option[StatusFields])

//this can't be a Map[String,String] as PrivateFields in Identity has other object types
case class PrivateFields(firstName: Option[String] = None,
                         secondName: Option[String] = None,
                         address1: Option[String] = None,
                         address2: Option[String] = None,
                         address3: Option[String] = None,
                         address4: Option[String] = None,
                         postcode: Option[String] = None,
                         country: Option[String] = None,
                         billingAddress1: Option[String] = None,
                         billingAddress2: Option[String] = None,
                         billingAddress3: Option[String] = None,
                         billingAddress4: Option[String] = None,
                         billingPostcode: Option[String] = None,
                         billingCountry: Option[String] = None,
                         socialAvatarUrl: Option[String] = None)

case class PublicFields(displayName: Option[String])

case class StatusFields(receiveGnmMarketing: Option[Boolean] = None,
                        receive3rdPartyMarketing: Option[Boolean] = None)

object UserDeserializer {
  implicit val readsStatusFields = Json.reads[StatusFields]
  implicit val readsPrivateFields = Json.reads[PrivateFields]
  implicit val readsPublicFields = Json.reads[PublicFields]
  implicit val readsUser = Json.reads[model.IdUser]
}