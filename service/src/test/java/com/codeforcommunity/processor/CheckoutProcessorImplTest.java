package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.enums.EventRegistrationStatus;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.InsufficientEventCapacityException;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.stripe.exception.StripeException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Record1;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventRegistrationsRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Jack said don't worry about testing createCheckoutSessionAndEventRegistration
// or handleStripeCheckoutEventComplete "cause it has to do with another api"

// Contains unit tests for CheckoutProcessorImpl.java in the service module
public class CheckoutProcessorImplTest {
  private JooqMock myJooqMock;
  private CheckoutProcessorImpl myCheckoutProcessorImpl;

  // set up all the mocks
  @BeforeEach
  public void setup() throws StripeException {
    this.myJooqMock = new JooqMock();
    this.myCheckoutProcessorImpl = new CheckoutProcessorImpl(myJooqMock.getContext());
  }

  // test creating checkout session and event registration fails if not a general user
  @Test
  public void testCreateCheckoutSessionAndEventRegistration1() {
    JWTData myUser1 = new JWTData(1, PrivilegeLevel.PF);
    JWTData myUser2 = new JWTData(2, PrivilegeLevel.ADMIN);

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(new ArrayList<>());

    myJooqMock.addEmptyReturn("SELECT");

    try {
      myCheckoutProcessorImpl.createCheckoutSessionAndEventRegistration(req, myUser1);
      fail();
    } catch (WrongPrivilegeException e) {
      assertEquals(PrivilegeLevel.GP, e.getRequiredPrivilegeLevel());
    }

    try {
      myCheckoutProcessorImpl.createCheckoutSessionAndEventRegistration(req, myUser2);
      fail();
    } catch (WrongPrivilegeException e) {
      assertEquals(PrivilegeLevel.GP, e.getRequiredPrivilegeLevel());
    }
  }

  // test creating checkout session and event registration fails if params are malformed
  @Test
  public void testCreateCheckoutSessionAndEventRegistration2() {
    JWTData myUser1 = new JWTData(0, PrivilegeLevel.GP);

    List<LineItemRequest> lineItemRequests = new ArrayList<>();

    myJooqMock.addEmptyReturn("SELECT");
    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItemRequests);

    try {
      myCheckoutProcessorImpl.createCheckoutSessionAndEventRegistration(req, myUser1);
      fail();
    } catch (StripeExternalException e) {
      assertTrue(e.getMessage().contains("Invalid API Key provided: "));
    }
  }

  // test that creating an event registration if the list of registrations is empty does nothing
  @Test
  public void testCreateEventRegistration1() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    List<LineItemRequest> lineItems = new ArrayList<>();

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItems);

    myJooqMock.addEmptyReturn("SELECT");
    myJooqMock.addEmptyReturn("INSERT");

    myCheckoutProcessorImpl.createEventRegistration(req, myUserData);

    assertEquals(0, myJooqMock.getSqlBindings().get("INSERT").size());
    assertEquals(1, myJooqMock.getSqlBindings().get("SELECT").size());
  }

  // test creating an event registration a line item's quantity is beyond capacity
  @Test
  public void testCreateEventRegistration2() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    LineItemRequest lineItem1 = new LineItemRequest(0, 50);

    List<LineItemRequest> lineItems = new ArrayList<>();
    lineItems.add(lineItem1);

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItems);

    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("Jellybeans");
    myJooqMock.addReturn("SELECT", myEvent);

    try {
      myCheckoutProcessorImpl.createEventRegistration(req, myUserData);
      fail();
    } catch (InsufficientEventCapacityException e) {
      assertEquals("Jellybeans", e.getEventTitle());
    }
  }

  // test creating an event registration with proper line items
  @Test
  public void testCreateEventRegistration3() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    LineItemRequest lineItem1 = new LineItemRequest(0, 1);

    List<LineItemRequest> lineItems = new ArrayList<>();
    lineItems.add(lineItem1);

    // for mocking the event
    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("Example title");
    myEvent.setDescription("Example description");
    myEvent.setCapacity(10);
    myEvent.setLocation("Boston");
    myEvent.setStartTime(new Timestamp(0));
    myEvent.setEndTime(new Timestamp(10000));
    myEvent.setThumbnail("random url");
    myJooqMock.addReturn("SELECT", myEvent);

    // for mocking getting spots left
    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(5);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    // for mocking the stored event
    EventRegistrationsRecord eventRegistrationFromLineItem =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS);
    eventRegistrationFromLineItem.setUserId(myUserData.getUserId());
    eventRegistrationFromLineItem.setRegistrationStatus(EventRegistrationStatus.PAYMENT_INCOMPLETE);
    eventRegistrationFromLineItem.setTicketQuantity(lineItem1.getQuantity());
    eventRegistrationFromLineItem.setStripeCheckoutSessionId(null);
    myJooqMock.addReturn("INSERT", eventRegistrationFromLineItem);

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItems);

    myCheckoutProcessorImpl.createEventRegistration(req, myUserData);

    List<Object[]> insertBindings = myJooqMock.getSqlBindings().get("INSERT");
    List<Object[]> selectBindings = myJooqMock.getSqlBindings().get("SELECT");

    assertEquals(1, insertBindings.size());
    assertEquals(3, selectBindings.size());
    assertEquals(myEvent.getId(), selectBindings.get(0)[0]);
    assertEquals(myEvent.getId(), selectBindings.get(1)[0]);
    assertEquals(myEvent.getId(), selectBindings.get(2)[0]);
  }

  // test for adding multiple line items
  @Test
  public void testCreateEventRegistration4() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    // for mocking the events
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(0);
    myEvent1.setTitle("Example title");
    myEvent1.setDescription("Example description");
    myEvent1.setCapacity(10);
    myEvent1.setLocation("Boston");
    myEvent1.setStartTime(new Timestamp(0));
    myEvent1.setEndTime(new Timestamp(10000));
    myEvent1.setThumbnail("random url");

    EventsRecord myEvent2 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent2.setId(1);
    myEvent2.setTitle("Join us for community outreach");
    myEvent2.setDescription("Community outreach");
    myEvent2.setCapacity(15);
    myEvent2.setLocation("Los Angeles");

    List<EventsRecord> myEvents = new ArrayList<>();
    myEvents.add(myEvent1);
    myEvents.add(myEvent2);
    myJooqMock.addReturn("SELECT", myEvents);

    // for mocking getting spots left
    Record1<Integer> myTicketsRecord1 =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord1.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord1);

    Record1<Integer> myEventRegistration1 =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration1.values(6);
    myJooqMock.addReturn("SELECT", myEventRegistration1);

    Record1<Integer> myTicketsRecord2 =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord2.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord2);

    Record1<Integer> myEventRegistration2 =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration2.values(5);
    myJooqMock.addReturn("SELECT", myEventRegistration2);

    LineItemRequest lineItem1 = new LineItemRequest(0, 1);
    LineItemRequest lineItem2 = new LineItemRequest(1, 3);

    List<LineItemRequest> lineItems = new ArrayList<>();
    lineItems.add(lineItem1);
    lineItems.add(lineItem2);

    // for mocking the stored event
    EventRegistrationsRecord eventRegistrationFromLineItem =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS);
    eventRegistrationFromLineItem.setUserId(myUserData.getUserId());
    eventRegistrationFromLineItem.setRegistrationStatus(EventRegistrationStatus.PAYMENT_INCOMPLETE);
    eventRegistrationFromLineItem.setTicketQuantity(lineItem1.getQuantity());
    eventRegistrationFromLineItem.setStripeCheckoutSessionId(null);
    myJooqMock.addReturn("INSERT", eventRegistrationFromLineItem);

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItems);

    myCheckoutProcessorImpl.createEventRegistration(req, myUserData);

    List<Object[]> insertBindings = myJooqMock.getSqlBindings().get("INSERT");
    List<Object[]> selectBindings = myJooqMock.getSqlBindings().get("SELECT");

    assertEquals(2, insertBindings.size());
    assertEquals(5, selectBindings.size());
    assertEquals(myEvent1.getId(), selectBindings.get(0)[0]);
    assertEquals(myEvent2.getId(), selectBindings.get(0)[1]);
    assertEquals(myEvent1.getId(), selectBindings.get(1)[0]);
    assertEquals(myEvent1.getId(), selectBindings.get(2)[0]);
    assertEquals(myEvent2.getId(), selectBindings.get(3)[0]);
    assertEquals(myEvent2.getId(), selectBindings.get(4)[0]);
  }
}
