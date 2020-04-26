package com.codeforcommunity.processor;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.Map;

public class CheckoutProcessorImpl implements ICheckoutProcessor {

    private DSLContext db;

    public CheckoutProcessorImpl(DSLContext db) {
        this.db = db;
    }

    public String createCheckoutSession(PostCheckoutRequest request) {

        // TODO: Move to properties file
        Stripe.apiKey = "sk_test_Q2wTkIY5Z3h9pjtgkksJULj200M84LsI3q";

        // TODO: Add if (isParticipatingFamily) and immediately give 200 code for true

        Map<String, Object> params = new HashMap<>();
        params.put(
                "success_url",
                request.getSuccess_url()
        );
        params.put(
                "cancel_url",
                request.getCancel_url()
        );
        params.put(
                "payment_method_types",
                request.getPayment_method_types()
        );
        params.put(
                "line_items",
                request.getLine_items()
        );
        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            throw new StripeExternalException(e.getMessage());
        }
    }
}
