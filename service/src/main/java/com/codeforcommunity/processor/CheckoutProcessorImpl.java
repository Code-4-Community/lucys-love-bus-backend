package com.codeforcommunity.processor;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.enums.EventRegistrationStatus;
import com.codeforcommunity.exceptions.EventRegistrationException;
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
import org.jooq.generated.tables.records.EventRegistrationsRecord;

import java.util.List;

import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;

public class CheckoutProcessorImpl implements ICheckoutProcessor {

    private DSLContext db;

    public CheckoutProcessorImpl(DSLContext db) {
        this.db = db;
    }

    public String createCheckoutSessionAndEventRegistration(PostCreateCheckoutSession request, JWTData user)
            throws StripeExternalException {

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
            String checkoutSessionId = session.getId();
            this.createEventRegistrationUtil(request.getLineItems(), user, checkoutSessionId);
            return session.getId();
        } catch (StripeException e) {
            throw new StripeExternalException(e.getMessage());
        }
    }

    public void createEventRegistration(PostCreateEventRegistrations request, JWTData user) {
        this.createEventRegistrationUtil(request.getLineItems(), user, null);
    }

    public void handleStripeCheckoutEventComplete(String payload, String sigHeader) {
        try {
            String endpointSecret = "whsec_TXciotTvq1luyU8wMppJAMJE9pBLDZ33"; // TODO: where does this go?
            Event event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
            if (event.getType().equals("checkout.session.completed")
                    && event.getDataObjectDeserializer().getObject().isPresent()) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();
                String checkoutSessionId = session.getId();
                this.db.update(EVENT_REGISTRATIONS)
                        .set(EVENT_REGISTRATIONS.REGISTRATION_STATUS, EventRegistrationStatus.ACTIVE)
                        .where(EVENT_REGISTRATIONS.STRIPE_CHECKOUT_SESSION_ID.eq(checkoutSessionId));
            }
        } catch (SignatureVerificationException e) {
            throw new StripeExternalException("Error verifying signature of incoming webhook");
        }
    }

    private void createEventRegistrationUtil(List<LineItemRequest> lineItemRequests,
                                             JWTData user, String checkoutSessionId) {
        for (LineItemRequest lineItem : lineItemRequests) {
            if (lineItem.getQuantity() > this.getSpotsLeft(lineItem.getId())) {
                throw new EventRegistrationException("There are insufficient remaining spots for the event "
                        + lineItem.getName());
            }
            EventRegistrationsRecord newRecord = db.newRecord(EVENT_REGISTRATIONS);
            newRecord.setEventId(lineItem.getId());
            newRecord.setUserId(user.getUserId());
            newRecord.setRegistrationStatus(
                    user.getPrivilegeLevel().getVal() >= PrivilegeLevel.PF.getVal()
                            ? EventRegistrationStatus.ACTIVE
                            : EventRegistrationStatus.PAYMENT_INCOMPLETE
            );
            newRecord.setTicketQuantity(lineItem.getQuantity().intValue());
            newRecord.setStripeCheckoutSessionId(checkoutSessionId);
            newRecord.store();
        }
    }

    /**
     * Queries the database to find the number of spots left for a given event by id.
     * @param eventId the event id
     * @return int the number of remaining spots for this event
     */
    private int getSpotsLeft(int eventId) {
        return db.execute("SELECT (capacity - (SELECT SUM(ticket_quantity)\n" +
                "        FROM event_registrations\n" +
                "        WHERE event_id = " + eventId + ")) as remainingCapacity\n" +
                "    FROM events\n" +
                "    WHERE id = " + eventId);
    }
}
