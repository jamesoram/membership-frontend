package forms

import play.api.data.{Mapping, Form}
import play.api.data.Forms._

import com.gu.membership.salesforce.Tier
import com.gu.membership.salesforce.Tier.Tier


object MemberForm {
  case class AddressForm(lineOne: String, lineTwo: String, town: String, countyOrState: String,
                         postCode: String, country: String)

  case class NameForm(first: String, last: String)

  case class PaymentForm(annual: Boolean, token: String)

  case class MarketingChoicesForm(gnm: Boolean, thirdParty: Boolean)

  trait JoinForm {
    val name: NameForm
    val deliveryAddress: AddressForm
    val marketingChoices: MarketingChoicesForm
  }

  case class FriendJoinForm(name: NameForm, deliveryAddress: AddressForm, marketingChoices: MarketingChoicesForm) extends JoinForm

  case class PaidMemberJoinForm(tier: Tier, name: NameForm, payment: PaymentForm, deliveryAddress: AddressForm,
                                billingAddress: Option[AddressForm], marketingChoices: MarketingChoicesForm) extends JoinForm

  case class PaidMemberChangeForm(payment: PaymentForm, deliveryAddress: AddressForm,
                                  billingAddress: Option[AddressForm])

  val friendAddressMapping: Mapping[AddressForm] = mapping(
    "lineOne" -> text,
    "lineTwo" -> text,
    "town" -> text,
    "countyOrState" -> text,
    "postCode" -> nonEmptyText,
    "country" -> nonEmptyText
  )(AddressForm.apply)(AddressForm.unapply)

  val paidAddressMapping: Mapping[AddressForm] = mapping(
    "lineOne" -> nonEmptyText,
    "lineTwo" -> text,
    "town" -> nonEmptyText,
    "countyOrState" -> text,
    "postCode" -> nonEmptyText,
    "country" -> nonEmptyText
  )(AddressForm.apply)(AddressForm.unapply)

  val nameMapping: Mapping[NameForm] = mapping(
    "first" -> nonEmptyText,
    "last" -> nonEmptyText
  )(NameForm.apply)(NameForm.unapply)

  val marketingChoicesMapping: Mapping[MarketingChoicesForm] = mapping(
    "gnnMarketing" -> boolean,
    "thirdParty" -> boolean
  )(MarketingChoicesForm.apply)(MarketingChoicesForm.unapply)

  val paymentMapping: Mapping[PaymentForm] = mapping(
    "annual" -> boolean,
    "token" -> nonEmptyText
  )(PaymentForm.apply)(PaymentForm.unapply)

  val friendJoinForm: Form[FriendJoinForm] = Form(
    mapping(
      "name" -> nameMapping,
      "deliveryAddress" -> friendAddressMapping,
      "marketingChoices" -> marketingChoicesMapping
    )(FriendJoinForm.apply)(FriendJoinForm.unapply)
  )

  val paidMemberJoinForm: Form[PaidMemberJoinForm] = Form(
    mapping(
      "tier" -> nonEmptyText.transform[Tier](Tier.withName, _.toString),
      "name" -> nameMapping,
      "payment" -> paymentMapping,
      "deliveryAddress" -> paidAddressMapping,
      "billingAddress" -> optional(paidAddressMapping),
      "marketingChoices" -> marketingChoicesMapping
    )(PaidMemberJoinForm.apply)(PaidMemberJoinForm.unapply)
  )

  val paidMemberChangeForm: Form[PaidMemberChangeForm] = Form(
    mapping(
      "payment" -> paymentMapping,
      "deliveryAddress" -> paidAddressMapping,
      "billingAddress" -> optional(paidAddressMapping)
    )(PaidMemberChangeForm.apply)(PaidMemberChangeForm.unapply)
  )
}
