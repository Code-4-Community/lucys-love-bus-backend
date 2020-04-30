package com.codeforcommunity.processor;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.UserEventsRecord;

import static org.jooq.generated.Tables.USER_EVENTS;

public class CheckoutProcessorImpl implements ICheckoutProcessor {

    private DSLContext db;

    public CheckoutProcessorImpl(DSLContext db) {
        this.db = db;
    }

    public String createCheckoutSession(PostCheckoutRequest request, JWTData user) throws StripeExternalException {

        this.createEventRegistration(request, user);

        // TODO: Move to properties file
        Stripe.apiKey = "sk_test_Q2wTkIY5Z3h9pjtgkksJULj200M84LsI3q";

        if (user.getPrivilegeLevel() != PrivilegeLevel.GP) {
            throw new WrongPrivilegeException(PrivilegeLevel.GP);
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

    public void createEventRegistration(PostCheckoutRequest request, JWTData data) {
        for (LineItemRequest lineItem : request.getLineItems()) {
            UserEventsRecord newRecord = db.newRecord(USER_EVENTS);
            newRecord.setEventId(lineItem.getId());
            newRecord.setUsersId(data.getUserId());
            newRecord.store();
        }
    }

    public void handleStripeCheckoutEventComplete(String payload, String sigHeader) {
        try {
            String endpointSecret = "whsec_TXciotTvq1luyU8wMppJAMJE9pBLDZ33"; // TODO: where does this go?
            Event event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
            if (event.getType().equals("checkout.session.completed")) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();
                // do things
            }
        } catch (SignatureVerificationException e) {
            throw new StripeExternalException("Error verifying signature of incoming webhook");
        }
    }
}
