package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.PENDING_REGISTRATIONS;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.checkout.CreateCheckoutSessionData;
import com.codeforcommunity.dto.checkout.LineItem;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.exceptions.InsufficientEventCapacityException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.exceptions.NotRegisteredException;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.pojos.PendingRegistrations;
import org.jooq.generated.tables.records.EventRegistrationsRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.generated.tables.records.PendingRegistrationsRecord;

public class CheckoutProcessorImpl implements ICheckoutProcessor {

  public static final int TICKET_PRICE_CENTS = 500;
  public static final String CANCEL_URL = "https://llb.c4cneu.com/checkout";
  public static final String SUCCESS_URL = "https://llb.c4cneu.com/?session_id=%s";

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

  private String createCheckoutSessionAndEventRegistration(List<LineItem> lineItems, JWTData user)
      throws StripeExternalException {
    CreateCheckoutSessionData checkoutRequest =
        new CreateCheckoutSessionData(lineItems, CANCEL_URL, SUCCESS_URL);

    Stripe.apiKey = this.stripeAPISecretKey;

    SessionCreateParams params =
        new SessionCreateParams.Builder()
            .addAllLineItem(checkoutRequest.getStripeLineItems())
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setCancelUrl(checkoutRequest.getCancelUrl())
            .build();
    try {
      Session session = Session.create(params);
      String checkoutSessionId = session.getId();
      session.setSuccessUrl(String.format(checkoutRequest.getSuccessUrl(), checkoutSessionId));
      createPendingEventRegistration(lineItems, user, checkoutSessionId);
      return session.getId();
    } catch (StripeException e) {
      throw new StripeExternalException(e.getMessage());
    }
  }

  @Override
  public Optional<String> createEventRegistration(
      PostCreateEventRegistrations request, JWTData user) throws StripeExternalException {
    List<LineItem> lineItems = convertLineItems(request.getLineItemRequests());
    assertLineItems(lineItems); // assert that quantities are within event capacity
    if (user.getPrivilegeLevel() == PrivilegeLevel.GP) {
      return Optional.of(createCheckoutSessionAndEventRegistration(lineItems, user));
    } else {
      this.createEventRegistration(lineItems, user);
      return Optional.empty();
    }
  }

  private void validateUpdateEventRegistration(Events event, EventRegistrationsRecord registration, int quantity, int eventId) {
    if (event == null) {
      throw new EventDoesNotExistException(eventId);
    }
    if (registration == null) {
      throw new NotRegisteredException(event.getTitle());
    }
    if (quantity < 0) { // allow 0 so that users can un-signup
      throw new MalformedParameterException("Quantity");
    }
    if (quantity > eventDatabaseOperations.getSpotsLeft(eventId)) {
      throw new InsufficientEventCapacityException(event.getTitle());
    }
  }

  @Override
  public Optional<String> updateEventRegistration(int eventId, int quantity, JWTData userData)
      throws StripeExternalException {
    Events event = db.selectFrom(EVENTS).where(EVENTS.ID.eq(eventId)).fetchOneInto(Events.class);
    int userId = userData.getUserId();
    EventRegistrationsRecord registration = db.selectFrom(EVENT_REGISTRATIONS)
        .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId)).and(EVENT_REGISTRATIONS.USER_ID.eq(userId))
        .fetchOneInto(EventRegistrationsRecord.class);
    validateUpdateEventRegistration(event, registration, quantity, eventId);
    int currentQuantity = registration.getTicketQuantity();
    if (quantity > currentQuantity) {
      if (userData.getPrivilegeLevel() == PrivilegeLevel.GP) {
        List<LineItem> lineItems = convertLineItems(
            Collections.singletonList(new LineItemRequest(eventId, quantity - currentQuantity)));
        return Optional.of(createCheckoutSessionAndEventRegistration(lineItems, userData));
      } else {
        registration.setPaid(false);
      }
    } else if (quantity == 0) {
      db.delete(EVENT_REGISTRATIONS).where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
          .and(EVENT_REGISTRATIONS.USER_ID.eq(userId)).execute();
    } else {
      if (registration.getPaid()) {
        int refundedTickets = currentQuantity - quantity;
        // give back amount
      }
    }
    registration.setTicketQuantity(quantity);
    registration.store();
    return Optional.empty();
  }

  @Override
  public void handleStripeCheckoutEventComplete(String payload, String sigHeader) {
    try {
      Event event = Webhook.constructEvent(payload, sigHeader, this.stripeWebhookSigningSecret);
      if (event.getType().equals("checkout.session.completed")
          && event.getDataObjectDeserializer().getObject().isPresent()) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().get();
        String checkoutSessionId = session.getId();
        List<PendingRegistrations> pendingRegistrations =
            db.selectFrom(PENDING_REGISTRATIONS)
                .where(PENDING_REGISTRATIONS.STRIPE_CHECKOUT_SESSION_ID.eq(checkoutSessionId))
                .fetchInto(PendingRegistrations.class);
        for (PendingRegistrations registration : pendingRegistrations) {
          int userId = registration.getUserId();
          int eventId = registration.getEventId();
          EventRegistrationsRecord currentRegistration =
              db.selectFrom(EVENT_REGISTRATIONS)
                  .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
                  .and(EVENT_REGISTRATIONS.USER_ID.eq(userId))
                  .fetchOneInto(EventRegistrationsRecord.class);
          if (currentRegistration != null) {
            currentRegistration.setTicketQuantity(
                currentRegistration.getTicketQuantity() + registration.getTicketQuantityDelta());
            currentRegistration.setPaid(true);
            currentRegistration.store();
          } else {
            EventRegistrationsRecord record = db.newRecord(EVENT_REGISTRATIONS);
            record.setUserId(userId);
            record.setEventId(eventId);
            record.setTicketQuantity(registration.getTicketQuantityDelta());
            record.setPaid(true);
            record.store();
          }
        }
        db.delete(PENDING_REGISTRATIONS)
            .where(PENDING_REGISTRATIONS.STRIPE_CHECKOUT_SESSION_ID.eq(checkoutSessionId))
            .execute();
      }
    } catch (SignatureVerificationException e) {
      throw new StripeExternalException("Error verifying signature of incoming webhook");
    }
  }

  /**
   * Create event registration for a PF or admin.
   *
   * @param lineItems A list of {@link LineItem} objects to write to database
   * @param user The {@link JWTData} containing the user's privilege level
   */
  private void createEventRegistration(List<LineItem> lineItems, JWTData user) {
    for (LineItem lineItem : lineItems) {
      EventRegistrationsRecord newRecord = db.newRecord(EVENT_REGISTRATIONS);
      newRecord.setEventId(lineItem.getId());
      newRecord.setUserId(user.getUserId());
      newRecord.setTicketQuantity(lineItem.getQuantity());
      newRecord.setPaid(false);
      newRecord.store();
    }
  }

  /**
   * Create pending event registration for GP. Will be marked as active once the Stripe payment is
   * complete.
   *
   * @param lineItems list of line items to write to database
   * @param user the user's privilege level
   * @param checkoutSessionId A checkoutSessionId to associate with registrations which require
   *     payment
   */
  private void createPendingEventRegistration(
      List<LineItem> lineItems, JWTData user, String checkoutSessionId) {
    int userId = user.getUserId();
    List<Integer> eventIds = lineItems.stream().map(LineItem::getId).collect(Collectors.toList());

    // deletes any pre-existing pending event registration for this user/event
    db.delete(PENDING_REGISTRATIONS)
        .where(PENDING_REGISTRATIONS.EVENT_ID.in(eventIds))
        .and(PENDING_REGISTRATIONS.USER_ID.eq(userId))
        .execute();

    for (LineItem lineItem : lineItems) {
      int eventId = lineItem.getId();
      PendingRegistrationsRecord record = db.newRecord(PENDING_REGISTRATIONS);
      record.setUserId(userId);
      record.setEventId(eventId);
      record.setTicketQuantityDelta(lineItem.getQuantity());
      record.setStripeCheckoutSessionId(checkoutSessionId);
      record.store();
    }
  }

  private void assertLineItems(List<LineItem> lineItems) {
    for (LineItem lineItem : lineItems) {
      if (lineItem.getQuantity() < 1) {
        throw new MalformedParameterException("Quantity");
      }
      if (lineItem.getQuantity() > eventDatabaseOperations.getSpotsLeft(lineItem.getId())) {
        throw new InsufficientEventCapacityException(lineItem.getName());
      }
    }
  }

  /**
   * Converts the line item requests into line items.
   *
   * @throws EventDoesNotExistException if any of the events do not exist
   */
  private List<LineItem> convertLineItems(List<LineItemRequest> lineItemRequests) {
    List<Integer> eventIds =
        lineItemRequests.stream().map(LineItemRequest::getEventId).collect(Collectors.toList());

    Map<Integer, EventsRecord> retrievedEvents =
        db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchMap(EVENTS.ID);

    List<LineItem> lineItems = new ArrayList<>();

    for (LineItemRequest request : lineItemRequests) {
      if (retrievedEvents.containsKey(request.getEventId())) {
        EventsRecord event = retrievedEvents.get(request.getEventId());
        int ticketQuantity = request.getQuantity();
        lineItems.add(
            new LineItem(
                event.getTitle(),
                event.getDescription(),
                ticketQuantity * TICKET_PRICE_CENTS,
                ticketQuantity,
                request.getEventId()));
      } else {
        throw new EventDoesNotExistException(request.getEventId());
      }
    }
    return lineItems;
  }
}
