package com.codeforcommunity.processor;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.jooq.DSLContext;

public class CheckoutProcessorImpl implements ICheckoutProcessor {

    private DSLContext db;

    public CheckoutProcessorImpl(DSLContext db) {
        this.db = db;
    }

    public String createCheckoutSession(PostCheckoutRequest request, JWTData data) throws StripeExternalException {

        // TODO: Move to properties file
        Stripe.apiKey = "sk_test_Q2wTkIY5Z3h9pjtgkksJULj200M84LsI3q";


        if (data.getPrivilegeLevel() == PrivilegeLevel.PF) {
            // TODO: Call endpoint to persist data immediately
        }

        SessionCreateParams params = new SessionCreateParams
                .Builder()
                .addAllLineItem(request.getStripeLineItems())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setSuccessUrl(request.getSuccessUrl())
                .setCancelUrl(request.getCancelUrl())
                .build();
        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            throw new StripeExternalException(e.getMessage());
        }
    }
}
