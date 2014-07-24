define([
    '$',
    'bean',
    'src/utils/component',
    'src/utils/Form'
], function ($, bean, component, Form) {
    'use strict';

    var self;
    var JoinPaid = function () {
        self = this;
    };

    component.define(JoinPaid);

    JoinPaid.prototype.classes = {
        NAME_FIRST: 'js-name-first',
        NAME_LAST: 'js-name-last',
        STRIPE_FORM: 'js-stripe-form',
        CREDIT_CARD_NUMBER: 'js-credit-card-number',
        CREDIT_CARD_CVC: 'js-credit-card-cvc',
        CREDIT_CARD_EXPIRY_MONTH: 'js-credit-card-exp-month',
        CREDIT_CARD_EXPIRY_YEAR: 'js-credit-card-exp-year',
        ADDRESS_LINE_ONE: 'js-address-line-one',
        TOWN: 'js-town',
        POST_CODE: 'js-post-code',
        BILLING: 'js-toggle-billing-address',
        BILLING_CTA: 'js-toggle-billing-address-cta',
        BILLING_FIELDSET: 'js-billingAddress-fieldset'
    };

    JoinPaid.prototype.data = {
        CARD_TYPE: 'data-card-type'
    };

    JoinPaid.prototype.init = function () {
        this.addFormValidation();
        this.toggleBillingAddressListener();
    };

    JoinPaid.prototype.toggleBillingAddressListener = function() {
        this.removeValidatorFromValidationProfile();

        var $billing = $(this.getElem('BILLING')).removeClass('u-h');
        var $billingDetails = $(this.getElem('BILLING_FIELDSET')).detach();
        var $billingCTA = $(this.getElem('BILLING_CTA'));

        bean.on($billingCTA[0], 'click', function () {

            if ($billingDetails.parent().length === 0) {
                // open
                $billingDetails.insertAfter($billing);
                $billingCTA.text('Same billing address as above');
                self.addValidatorFromValidationProfile();
            } else {
                // closed
                $('.form-field', $billingDetails).removeClass('form-field--error');

                $billingCTA.text('Different billing address?');
                self.removeValidatorFromValidationProfile();
                $billingDetails.detach();
            }
        });
    };

    JoinPaid.prototype.addValidatorFromValidationProfile = function () {

        this.form.addValidatorFromValidationProfile(
            [
                {
                    elem: $(this.getClass('ADDRESS_LINE_ONE'), this.getClass('BILLING_FIELDSET'))[0],
                    validator: 'requiredValidator'
                },
                {
                    elem: $(this.getClass('TOWN'), this.getClass('BILLING_FIELDSET'))[0],
                    validator: 'requiredValidator'
                },
                {
                    elem: $(this.getClass('POST_CODE'), this.getClass('BILLING_FIELDSET'))[0],
                    validator: 'requiredValidator'
                }
            ]);
    };

    JoinPaid.prototype.removeValidatorFromValidationProfile = function () {

        this.form.removeValidatorFromValidationProfile(
            [
                {
                    elem: $(this.getClass('ADDRESS_LINE_ONE'), this.getClass('BILLING_FIELDSET'))[0],
                    validator: 'requiredValidator'
                },
                {
                    elem: $(this.getClass('TOWN'), this.getClass('BILLING_FIELDSET'))[0],
                    validator: 'requiredValidator'
                },
                {
                    elem: $(this.getClass('POST_CODE'), this.getClass('BILLING_FIELDSET'))[0],
                    validator: 'requiredValidator'
                }
            ]);
    };

    JoinPaid.prototype.addFormValidation = function () {
        var formElement = this.elem = this.getElem('STRIPE_FORM');

        this.form = new Form(formElement, '/subscription/subscribe', window.location.href.replace('payment', 'thankyou'));

        this.form.addValidation(
            [
                {
                    elem: this.getElem('NAME_FIRST'),
                    name: 'required'
                },
                {
                    elem: this.getElem('NAME_LAST'),
                    name: 'required'
                },
                {
                    elem: this.getElem('ADDRESS_LINE_ONE'),
                    name: 'required'
                },
                {
                    elem: this.getElem('TOWN'),
                    name: 'required'
                },
                {
                    elem: this.getElem('POST_CODE'),
                    name: 'required'
                },
                {
                    elem: $(this.getClass('ADDRESS_LINE_ONE'), this.getClass('BILLING_FIELDSET'))[0],
                    name: 'required'
                },
                {
                    elem: $(this.getClass('TOWN'), this.getClass('BILLING_FIELDSET'))[0],
                    name: 'required'
                },
                {
                    elem: $(this.getClass('POST_CODE'), this.getClass('BILLING_FIELDSET'))[0],
                    name: 'required'
                },
                {
                    elem: this.getElem('CREDIT_CARD_NUMBER'),
                    name: 'creditCardNumber'
                },
                {
                    elem: this.getElem('CREDIT_CARD_CVC'),
                    name: 'creditCardCVC'
                },
                {
                    elem: [
                        this.getElem('CREDIT_CARD_EXPIRY_MONTH'),
                        this.getElem('CREDIT_CARD_EXPIRY_YEAR')
                    ],
                    name: 'creditCardExpiry'
                }
            ]
        ).init();
    };

    return JoinPaid;
});
