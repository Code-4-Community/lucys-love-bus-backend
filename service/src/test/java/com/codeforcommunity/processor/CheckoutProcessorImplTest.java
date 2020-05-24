package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.enums.EventRegistrationStatus;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.InsufficientEventCapacityException;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.stripe.exception.StripeException;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Record1;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventRegistrationsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Jack said don't worry about testing a properly working createCheckoutSessionAndEventRegistration
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

    PostCreateCheckoutSession req =
        new PostCreateCheckoutSession(
            new ArrayList<>(),
            "https://lucy.c4cneu.com/checkout",
            "https://lucy.c4cneu.com/checkout");

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

    PostCreateCheckoutSession req =
        new PostCreateCheckoutSession(
            new ArrayList<>(),
            "https://lucy.c4cneu.com/checkout",
            "https://lucy.c4cneu.com/checkout");

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

    myCheckoutProcessorImpl.createEventRegistration(req, myUserData);

    myJooqMock.addEmptyReturn("SELECT");
    myJooqMock.addEmptyReturn("INSERT");

    assertEquals(0, myJooqMock.getSqlBindings().get("INSERT").size());
    assertEquals(0, myJooqMock.getSqlBindings().get("SELECT").size());
  }

  // test creating an event registration a line item's quantity is beyond capacity
  @Test
  public void testCreateEventRegistration2() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    LineItemRequest lineItem1 =
        new LineItemRequest(
            "Jellybeans", "A jar of jellybeans of assorted colors", 5, "USD", 10, 40);

    List<LineItemRequest> lineItems = new ArrayList<>();
    lineItems.add(lineItem1);

    myJooqMock.addEmptyReturn("SELECT");

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItems);

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

    LineItemRequest lineItem1 =
        new LineItemRequest(
            "Jellybeans", "A jar of jellybeans of assorted colors", 3, "USD", 4, 40);

    List<LineItemRequest> lineItems = new ArrayList<>();
    lineItems.add(lineItem1);

    // for mocking getting spots left
    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);

    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(5);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    // for mocking the stored event
    EventRegistrationsRecord eventRegistrationFromLineItem =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS);
    eventRegistrationFromLineItem.setUserId(myUserData.getUserId());
    eventRegistrationFromLineItem.setEventId(lineItem1.getId());
    eventRegistrationFromLineItem.setRegistrationStatus(EventRegistrationStatus.PAYMENT_INCOMPLETE);
    eventRegistrationFromLineItem.setTicketQuantity(lineItem1.getQuantity().intValue());
    eventRegistrationFromLineItem.setStripeCheckoutSessionId(null);

    myJooqMock.addReturn("INSERT", eventRegistrationFromLineItem);

    PostCreateEventRegistrations req = new PostCreateEventRegistrations(lineItems);

    myCheckoutProcessorImpl.createEventRegistration(req, myUserData);

    Object[] insertBindings = myJooqMock.getSqlBindings().get("INSERT").get(0);

    assertEquals(5, insertBindings.length);
    assertEquals(myUserData.getUserId(), insertBindings[0]);
    assertEquals(lineItem1.getId(), insertBindings[1]);
    assertEquals(lineItem1.getQuantity(), Long.valueOf((int) insertBindings[2]));
    assertEquals(2, insertBindings[3]);
    assertNull(insertBindings[4]);
  }

  // TODO: test for adding multiple line items, figure out how to resolve the TooManyRowsException

  // test completing checkout event session fails with garbage webhook signature
  @Test
  public void testHandleStripeCheckoutEventComplete() {
    try {
      myCheckoutProcessorImpl.handleStripeCheckoutEventComplete(anyString(), anyString());
      fail();
    } catch (StripeExternalException e) {
      assertEquals("Error verifying signature of incoming webhook", e.getMessage());
    }
  }
}
