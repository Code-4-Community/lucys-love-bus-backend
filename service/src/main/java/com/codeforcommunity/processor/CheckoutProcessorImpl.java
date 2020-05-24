package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.enums.EventRegistrationStatus;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.InsufficientEventCapacityException;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.List;
import java.util.Properties;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.EventRegistrationsRecord;

public class CheckoutProcessorImpl implements ICheckoutProcessor {

  private final DSLContext db;
  private final EventDatabaseOperations eventDatabaseOperations;
  private final String stripeAPISecretKey;
  private final String stripeWebhookSigningSecret;

  public CheckoutProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);

    Properties stripeProperties = PropertiesLoader.getStripeProperties();
    this.stripeAPISecretKey = stripeProperties.getProperty("stripe_api_secret_key");
    this.stripeWebhookSigningSecret = stripeProperties.getProperty("stripe_webhook_signing_secret");
  }

  @Override
  public String createCheckoutSessionAndEventRegistration(
      PostCreateCheckoutSession request, JWTData user) throws StripeExternalException {

    Stripe.apiKey = this.stripeAPISecretKey;

    if (user.getPrivilegeLevel() != PrivilegeLevel.GP) {
      throw new WrongPrivilegeException(PrivilegeLevel.GP);
    }

    SessionCreateParams params =
        new SessionCreateParams.Builder()
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

  @Override
  public void createEventRegistration(PostCreateEventRegistrations request, JWTData user) {
    this.createEventRegistrationUtil(request.getLineItems(), user, null);
  }

  @Override
  public void handleStripeCheckoutEventComplete(String payload, String sigHeader) {
    try {
      Event event = Webhook.constructEvent(payload, sigHeader, this.stripeWebhookSigningSecret);
      if (event.getType().equals("checkout.session.completed")
          && event.getDataObjectDeserializer().getObject().isPresent()) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().get();
        String checkoutSessionId = session.getId();
        this.db
            .update(EVENT_REGISTRATIONS)
            .set(EVENT_REGISTRATIONS.REGISTRATION_STATUS, EventRegistrationStatus.ACTIVE)
            .where(EVENT_REGISTRATIONS.STRIPE_CHECKOUT_SESSION_ID.eq(checkoutSessionId))
            .execute();
      }
    } catch (SignatureVerificationException e) {
      throw new StripeExternalException("Error verifying signature of incoming webhook");
    }
  }

  /**
   * A common utility function used to write a list of events to the database.
   *
   * @param lineItemRequests A list of {@link LineItemRequest} objects to write to database
   * @param user The {@link JWTData} containing the user's privilege level
   * @param checkoutSessionId A checkoutSessionId to associate with registrations which require
   *     payment
   */
  private void createEventRegistrationUtil(
      List<LineItemRequest> lineItemRequests, JWTData user, String checkoutSessionId) {
    for (LineItemRequest lineItem : lineItemRequests) {
      if (lineItem.getQuantity() > eventDatabaseOperations.getSpotsLeft(lineItem.getId())) {
        throw new InsufficientEventCapacityException(lineItem.getName());
      }
      EventRegistrationsRecord newRecord = db.newRecord(EVENT_REGISTRATIONS);
      newRecord.setEventId(lineItem.getId());
      newRecord.setUserId(user.getUserId());
      newRecord.setRegistrationStatus(
          this.getInitialEventRegistrationStatus(user.getPrivilegeLevel()));
      newRecord.setTicketQuantity(lineItem.getQuantity().intValue());
      newRecord.setStripeCheckoutSessionId(checkoutSessionId);
      newRecord.store();
    }
  }

  /**
   * For ADMIN and PF users, their Event Registrations are initially Active, because they don't have
   * to pay for events. For GP users, the Event Registration is marked as PAYMENT_INCOMPLETE until
   * we receive a checkout.session.completed event from Stripe in our webhook.
   *
   * @param privilegeLevel The privilege level of the user
   * @return EventRegistrationStatus for the PrivilegeLevel
   */
  private EventRegistrationStatus getInitialEventRegistrationStatus(PrivilegeLevel privilegeLevel) {
    switch (privilegeLevel) {
      case GP:
        return EventRegistrationStatus.PAYMENT_INCOMPLETE;
      case ADMIN:
      case PF:
        return EventRegistrationStatus.ACTIVE;
      default:
        throw new IllegalArgumentException("Unrecognized PrivilegeLevel: " + privilegeLevel.name());
    }
  }
}
