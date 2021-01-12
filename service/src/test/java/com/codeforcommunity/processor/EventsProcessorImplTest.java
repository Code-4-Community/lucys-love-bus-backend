package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.codeforcommunity.Base64TestStrings;
import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.components.Registration;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

// Contains tests for EventsProcessorImpl.java in main
public class EventsProcessorImplTest {

  private EventsProcessorImpl myEventsProcessorImpl;
  private JooqMock myJooqMock;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int START_TIMESTAMP_TEST = 1587000000;
  // 04/17/2020 @ 5:06am (UTC)
  private final int END_TIMESTAMP_TEST = 1587100000;

  @BeforeEach
  private void setup() {
    this.myJooqMock = new JooqMock();
    EventDatabaseOperations myEventDatabaseOperations =
        new EventDatabaseOperations(myJooqMock.getContext());
    this.myEventsProcessorImpl = new EventsProcessorImpl(myJooqMock.getContext());

    // mock Amazon S3
    AmazonS3Client mockS3Client = mock(AmazonS3Client.class);
    PutObjectResult mockPutObjectResult = mock(PutObjectResult.class);
    S3Requester.Externs mockExterns = mock(S3Requester.Externs.class);

    when(mockS3Client.putObject(any(PutObjectRequest.class))).thenReturn(mockPutObjectResult);
    when(mockExterns.getS3Client()).thenReturn(mockS3Client);

    S3Requester.setExterns(mockExterns);
  }

  // test exception thrown for not being an admin
  @Test
  public void testCreateEvent1() {
    // make the event
    EventDetails myEventDetails =
        new EventDetails(
            "my event",
            "boston",
            new Timestamp(START_TIMESTAMP_TEST),
            new Timestamp(END_TIMESTAMP_TEST));
    CreateEventRequest myEventRequest =
        new CreateEventRequest("sample", 5, "sample thumbnail", myEventDetails, 500);

    // mock the DB
    JWTData badUser = mock(JWTData.class);
    when(badUser.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

    try {
      myEventsProcessorImpl.createEvent(myEventRequest, badUser);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test proper event creation
  @Test
  public void testCreateEvent2() {
    // make the event
    EventDetails myEventDetails =
        new EventDetails(
            "my event",
            "boston",
            new Timestamp(START_TIMESTAMP_TEST),
            new Timestamp(END_TIMESTAMP_TEST));

    CreateEventRequest req =
        new CreateEventRequest("sample", 5, Base64TestStrings.TEST_STRING_1, myEventDetails, 10000);

    JWTData goodUser = new JWTData(0, PrivilegeLevel.ADMIN);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    // mock the event
    EventsRecord record = myJooqMock.getContext().newRecord(EVENTS);
    record.setId(0);
    record.setCapacity(req.getSpotsAvailable());
    myJooqMock.addReturn("INSERT", record);
    myJooqMock.addReturn("SELECT", record);

    SingleEventResponse res = myEventsProcessorImpl.createEvent(req, goodUser);

    assertEquals(res.getId(), 0);
    assertEquals(res.getTitle(), "sample");
    assertEquals(res.getCapacity(), 5);
    assertEquals(
        res.getThumbnail(),
        "https://lucys-love-bus.s3.us-east-2.amazonaws.com/events/sample_thumbnail.gif");
    assertEquals(res.getDetails().getDescription(), myEventDetails.getDescription());
    assertEquals(res.getDetails().getLocation(), myEventDetails.getLocation());
    assertEquals(res.getDetails().getEnd(), myEventDetails.getEnd());
    assertEquals(res.getDetails().getStart(), myEventDetails.getStart());
  }

  // test getting an event id that's not there
  @Test
  public void testGetSingleEvent1() {
    // mock the DB
    myJooqMock.addEmptyReturn("SELECT");

    JWTData userData = new JWTData(0, PrivilegeLevel.GP);

    try {
      myEventsProcessorImpl.getSingleEvent(5, userData);
      fail();
    } catch (EventDoesNotExistException e) {
      assertEquals(e.getEventId(), 5);
    }
  }

  // test getting an event id that's indeed there
  @Test
  public void testGetSingleEvent2() {
    // create the event
    EventDetails myEventDetails =
        new EventDetails(
            "my event",
            "boston",
            new Timestamp(START_TIMESTAMP_TEST),
            new Timestamp(END_TIMESTAMP_TEST));
    CreateEventRequest myEventRequest =
        new CreateEventRequest("sample", 5, "sample thumbnail", myEventDetails, 2000);

    // mock the DB for events
    EventsRecord eventRecord = myJooqMock.getContext().newRecord(EVENTS);
    eventRecord.setId(1);
    eventRecord.setThumbnail(myEventRequest.getThumbnail());
    eventRecord.setTitle(myEventRequest.getTitle());
    eventRecord.setCapacity(myEventRequest.getSpotsAvailable());
    eventRecord.setLocation(myEventDetails.getLocation());
    eventRecord.setDescription(myEventDetails.getDescription());
    eventRecord.setStartTime(myEventDetails.getStart());
    eventRecord.setEndTime(myEventDetails.getEnd());
    eventRecord.setPrice(myEventRequest.getPrice());
    myJooqMock.addReturn("SELECT", eventRecord);

    // mock the DB for getting ticket counts
    Record2<Integer, Integer> registrationRecord =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord.values(1, 2);
    myJooqMock.addReturn("SELECT", registrationRecord);
    myJooqMock.addReturn("INSERT", registrationRecord);

    // mock the DB for getting spots left
    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(5);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    // mock pending registrations
    myJooqMock.addEmptyReturn("SELECT");

    JWTData userData = new JWTData(0, PrivilegeLevel.GP);
    SingleEventResponse res = myEventsProcessorImpl.getSingleEvent(1, userData);

    assertEquals(res.getId(), 1);
    assertEquals(res.getSpotsAvailable(), 4);
    assertEquals(res.getCapacity(), 5);
    assertEquals(res.getThumbnail(), myEventRequest.getThumbnail());
    assertEquals(res.getTitle(), myEventRequest.getTitle());
    assertEquals(res.getPrice(), myEventRequest.getPrice());

    assertEquals(res.getDetails().getStart(), myEventDetails.getStart());
    assertEquals(res.getDetails().getEnd(), myEventDetails.getEnd());
    assertEquals(res.getDetails().getLocation(), myEventDetails.getLocation());
    assertEquals(res.getDetails().getDescription(), myEventDetails.getDescription());
  }

  // Test getting events when the given event ids don't exist
  @Test
  public void testGetEvents1() {
    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);

    myJooqMock.addEmptyReturn("SELECT");

    JWTData userData = new JWTData(0, PrivilegeLevel.GP);
    GetEventsResponse res = myEventsProcessorImpl.getEvents(eventIds, userData);
    assertEquals(0, res.getEvents().size());
    assertEquals(0, res.getTotalCount());
  }

  // Test getting exactly one event by id
  @Test
  public void testGetEvents2() {
    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);

    // mock the event
    EventsRecord event1 = myJooqMock.getContext().newRecord(EVENTS);
    event1.setId(0);
    event1.setTitle("title 1");
    event1.setCapacity(10);
    event1.setEndTime(new Timestamp(System.currentTimeMillis() + 10000));
    event1.setStartTime(new Timestamp(System.currentTimeMillis() - 10000));
    event1.setPrice(2000);
    myJooqMock.addReturn("SELECT", event1);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount1 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount1.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount1);

    // prime the DB for getSpotsLeft()
    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(4);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    JWTData userData = new JWTData(0, PrivilegeLevel.GP);
    GetEventsResponse res = myEventsProcessorImpl.getEvents(eventIds, userData);

    assertEquals(event1.getId(), res.getEvents().get(0).getId());
    assertEquals(event1.getTitle(), res.getEvents().get(0).getTitle());
    assertEquals(1, res.getTotalCount());
  }

  // Test getting multiple events by id
  @Test
  public void testGetEvents3() {
    EventsRecord event1 = myJooqMock.getContext().newRecord(EVENTS);
    event1.setId(0);
    event1.setCapacity(10);
    event1.setPrice(10000);
    event1.setTitle("title 1");
    event1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));
    EventsRecord event2 = myJooqMock.getContext().newRecord(EVENTS);
    event2.setId(1);
    event2.setCapacity(50);
    event2.setTitle("title 2");
    event2.setPrice(500);
    event2.setEndTime(new Timestamp(END_TIMESTAMP_TEST + 100000));

    List<EventsRecord> eventRecords = new ArrayList<>();
    eventRecords.add(event1);
    eventRecords.add(event2);
    myJooqMock.addReturn("SELECT", eventRecords);
    myJooqMock.addReturn("INSERT", eventRecords);

    Record2<Integer, Integer> registrationRecord =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord.values(2, 3);
    myJooqMock.addReturn("SELECT", registrationRecord);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(5);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);
    eventIds.add(1);

    JWTData userData = new JWTData(0, PrivilegeLevel.GP);
    GetEventsResponse res = myEventsProcessorImpl.getEvents(eventIds, userData);

    assertEquals(event1.getId(), res.getEvents().get(0).getId());
    assertEquals(event1.getTitle(), res.getEvents().get(0).getTitle());
    assertEquals(2, res.getTotalCount());
  }

  // test getting events user signed up for if there are none
  @Test
  public void testGetEventsSignedUp1() {
    GetUserEventsRequest req =
        new GetUserEventsRequest(
            Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
            Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
            Optional.of(1));

    myJooqMock.addEmptyReturn("SELECT");

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(0, res.getTotalCount());
    assertEquals(0, res.getEvents().size());
  }

  // test getting one event user signed up for
  @Test
  public void testGetEventsSignedUp2() {
    GetUserEventsRequest req =
        new GetUserEventsRequest(
            Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
            Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
            Optional.of(1));

    // mock the event
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent1.setId(0);
    myEvent1.setTitle("Event 1");
    myEvent1.setDescription("Description 1");
    myEvent1.setCapacity(10);
    myEvent1.setPrice(4000);
    myEvent1.setStartTime(new Timestamp(START_TIMESTAMP_TEST));
    myEvent1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));
    myJooqMock.addReturn("SELECT", myEvent1);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(1, res.getTotalCount());

    SingleEventResponse actualEvent = res.getEvents().get(0);

    assertEquals(myEvent1.getId(), actualEvent.getId());
    assertEquals(myEvent1.getTitle(), actualEvent.getTitle());
    assertEquals(myEvent1.getDescription(), actualEvent.getDetails().getDescription());
  }

  // test getting multiple events user signed up for with no limit
  @Test
  public void testGetEventsSignedUp3() {
    GetUserEventsRequest req =
        new GetUserEventsRequest(
            Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
            Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
            Optional.of(3));

    // add three events to the mock DB
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent1.setId(0);
    myEvent1.setCapacity(10);
    myEvent1.setTitle("Event 1");
    myEvent1.setDescription("Description 1");
    myEvent1.setStartTime(new Timestamp(START_TIMESTAMP_TEST));
    myEvent1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));
    myEvent1.setPrice(5000);

    EventsRecord myEvent2 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent2.setId(1);
    myEvent2.setCapacity(5);
    myEvent2.setTitle("Event 2");
    myEvent2.setDescription("Description 2");
    myEvent2.setStartTime(new Timestamp(START_TIMESTAMP_TEST - 100000));
    myEvent2.setEndTime(new Timestamp(END_TIMESTAMP_TEST + 100000));
    myEvent2.setPrice(100000);

    EventsRecord myEvent3 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent3.setId(2);
    myEvent3.setCapacity(50);
    myEvent3.setTitle("Event 2");
    myEvent3.setDescription("Description 2");
    myEvent3.setStartTime(new Timestamp(0));
    myEvent3.setEndTime(new Timestamp(100000));
    myEvent3.setPrice(40000);

    List<EventsRecord> events = new ArrayList<>();
    events.add(myEvent1);
    events.add(myEvent2);
    events.add(myEvent3);
    myJooqMock.addReturn("SELECT", events);

    // prime the DB for getRegistrationStatus
    Record2<Integer, Integer> registrationRecord1 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord1.values(0, 3);
    Record2<Integer, Integer> registrationRecord2 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord2.values(1, 5);
    Record2<Integer, Integer> registrationRecord3 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord3.values(2, 10);

    List<Record2<Integer, Integer>> registrationRecords = new ArrayList<>();
    registrationRecords.add(registrationRecord1);
    registrationRecords.add(registrationRecord2);
    registrationRecords.add(registrationRecord3);
    myJooqMock.addReturn("SELECT", registrationRecords);

    // prime the DB for getSpotsLeft()
    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(4);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addEmptyReturn("SELECT");

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(3, res.getTotalCount());

    SingleEventResponse actualEvent1 = res.getEvents().get(0);
    SingleEventResponse actualEvent2 = res.getEvents().get(1);
    SingleEventResponse actualEvent3 = res.getEvents().get(2);

    assertEquals(myEvent1.getId(), actualEvent1.getId());
    assertEquals(myEvent1.getTitle(), actualEvent1.getTitle());
    assertEquals(myEvent1.getDescription(), actualEvent1.getDetails().getDescription());
    assertEquals(myEvent2.getId(), actualEvent2.getId());
    assertEquals(myEvent2.getTitle(), actualEvent2.getTitle());
    assertEquals(myEvent2.getDescription(), actualEvent2.getDetails().getDescription());
    assertEquals(myEvent3.getId(), actualEvent3.getId());
    assertEquals(myEvent3.getTitle(), actualEvent3.getTitle());
    assertEquals(myEvent3.getDescription(), actualEvent3.getDetails().getDescription());
  }

  // test getting multiple events user signed up for with a limit
  @Test
  public void testGetEventsSignedUp4() {
    GetUserEventsRequest req =
        new GetUserEventsRequest(
            Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
            Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
            Optional.of(1));

    // add three events to the mock DB
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent1.setId(0);
    myEvent1.setCapacity(10);
    myEvent1.setTitle("Event 1");
    myEvent1.setDescription("Description 1");
    myEvent1.setStartTime(new Timestamp(START_TIMESTAMP_TEST));
    myEvent1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));
    myEvent1.setPrice(50000);

    EventsRecord myEvent2 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent2.setId(1);
    myEvent2.setCapacity(5);
    myEvent2.setTitle("Event 2");
    myEvent2.setDescription("Description 2");
    myEvent2.setStartTime(new Timestamp(START_TIMESTAMP_TEST - 100000));
    myEvent2.setEndTime(new Timestamp(END_TIMESTAMP_TEST + 100000));
    myEvent2.setPrice(100000);

    EventsRecord myEvent3 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent3.setId(2);
    myEvent3.setCapacity(50);
    myEvent3.setTitle("Event 2");
    myEvent3.setDescription("Description 2");
    myEvent3.setStartTime(new Timestamp(0));
    myEvent3.setEndTime(new Timestamp(100000));
    myEvent3.setPrice(8000);

    myJooqMock.addReturn("SELECT", myEvent1);

    // prime the DB for getRegistrationStatus
    Record2<Integer, Integer> registrationRecord1 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord1.values(0, 3);
    Record2<Integer, Integer> registrationRecord2 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord2.values(1, 5);
    Record2<Integer, Integer> registrationRecord3 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord3.values(2, 10);

    List<Record2<Integer, Integer>> registrationRecords = new ArrayList<>();
    registrationRecords.add(registrationRecord1);
    registrationRecords.add(registrationRecord2);
    registrationRecords.add(registrationRecord3);
    myJooqMock.addReturn("SELECT", registrationRecords);

    // prime the DB for getSpotsLeft()
    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);

    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(1, res.getTotalCount());

    SingleEventResponse actualEvent1 = res.getEvents().get(0);

    assertEquals(myEvent1.getId(), actualEvent1.getId());
    assertEquals(myEvent1.getTitle(), actualEvent1.getTitle());
    assertEquals(myEvent1.getDescription(), actualEvent1.getDetails().getDescription());
  }

  // method gives the correct response if user doesn't qualify for any events
  @ParameterizedTest
  @EnumSource(PrivilegeLevel.class)
  public void testGetEventsQualified1(PrivilegeLevel pl) {
    // write tests for both an admin and gp user
    JWTData myUserData = new JWTData(0, pl);

    myJooqMock.addEmptyReturn("SELECT");

    GetEventsResponse resGP = myEventsProcessorImpl.getEventsQualified(myUserData);
    assertEquals(0, resGP.getTotalCount());
  }

  // user qualifies for one event
  @ParameterizedTest
  @EnumSource(PrivilegeLevel.class)
  public void testGetEventsQualified2(PrivilegeLevel pl) {
    JWTData myUserData = new JWTData(0, pl);

    // mock the event
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent1.setId(0);
    myEvent1.setTitle("Title 1");
    myEvent1.setDescription("Description 1");
    myEvent1.setCapacity(10);
    myEvent1.setPrice(4000);
    myEvent1.setStartTime(new Timestamp(START_TIMESTAMP_TEST));
    myEvent1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));
    myJooqMock.addReturn("SELECT", myEvent1);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount1 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount1.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount1);

    GetEventsResponse res = myEventsProcessorImpl.getEventsQualified(myUserData);
    assertEquals(1, res.getTotalCount());
    SingleEventResponse resGPActualEvent = res.getEvents().get(0);

    assertEquals(0, resGPActualEvent.getId());
    assertEquals("Title 1", resGPActualEvent.getTitle());
    assertEquals("Description 1", resGPActualEvent.getDetails().getDescription());
    assertEquals(new Timestamp(START_TIMESTAMP_TEST), resGPActualEvent.getDetails().getStart());
  }

  // user qualifies for multiple events
  @Test
  public void testGetEventsQualified3() {
    // write tests for both an admin and gp user
    JWTData myGPUserData = new JWTData(0, PrivilegeLevel.GP);

    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent1.setId(0);
    myEvent1.setTitle("Title 1");
    myEvent1.setDescription("Description 1");
    myEvent1.setCapacity(10);
    myEvent1.setPrice(200);
    myEvent1.setStartTime(new Timestamp(START_TIMESTAMP_TEST));
    myEvent1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));

    EventsRecord myEvent2 = myJooqMock.getContext().newRecord(EVENTS);
    myEvent2.setId(1);
    myEvent2.setTitle("Title 2");
    myEvent2.setDescription("Description 2");
    myEvent2.setCapacity(20);
    myEvent2.setPrice(10000);
    myEvent2.setStartTime(new Timestamp(0));
    myEvent2.setEndTime(new Timestamp(100000));

    List<EventsRecord> eventsGP = new ArrayList<>();
    eventsGP.add(myEvent1);
    eventsGP.add(myEvent2);
    myJooqMock.addReturn("SELECT", eventsGP);

    // prime the DB for getRegistrationStatus
    Record2<Integer, Integer> registrationRecord1 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord1.values(0, 3);

    Record2<Integer, Integer> registrationRecord2 =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    registrationRecord2.values(1, 5);

    List<Record2<Integer, Integer>> registrationRecordsGP = new ArrayList<>();
    registrationRecordsGP.add(registrationRecord1);
    registrationRecordsGP.add(registrationRecord2);
    myJooqMock.addReturn("SELECT", registrationRecordsGP);

    // prime the DB for getSpotsLeft()
    Record1<Integer> myEventRegistrationGP = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistrationGP.values(4);
    myJooqMock.addReturn("SELECT", myEventRegistrationGP);

    Record1<Integer> myTicketsRecordGP =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecordGP.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecordGP);
    myJooqMock.addEmptyReturn("SELECT");

    GetEventsResponse resGP = myEventsProcessorImpl.getEventsQualified(myGPUserData);
    assertEquals(2, resGP.getTotalCount());

    SingleEventResponse gpActualEvent1 = resGP.getEvents().get(0);
    assertEquals(0, gpActualEvent1.getId());
    assertEquals("Title 1", gpActualEvent1.getTitle());
    assertEquals("Description 1", gpActualEvent1.getDetails().getDescription());
    assertEquals(new Timestamp(START_TIMESTAMP_TEST), gpActualEvent1.getDetails().getStart());

    SingleEventResponse gpActualEvent2 = resGP.getEvents().get(1);
    assertEquals(1, gpActualEvent2.getId());
    assertEquals("Title 2", gpActualEvent2.getTitle());
    assertEquals("Description 2", gpActualEvent2.getDetails().getDescription());
    assertEquals(new Timestamp(0), gpActualEvent2.getDetails().getStart());
  }

  // modifying an event fails if the user isn't an admin
  @Test
  public void testModifyEvent1() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    ModifyEventRequest req = new ModifyEventRequest(null, null, null, null, null);

    try {
      myEventsProcessorImpl.modifyEvent(0, req, myUserData);
      fail();
    } catch (AdminOnlyRouteException ignored) {
    }
  }

  // modifying an event with all fields filled in
  @Test
  public void testModifyEvent2() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    ModifyEventRequest req =
        new ModifyEventRequest(
            "edited title",
            10,
            "edited thumbnail",
            new EventDetails(
                "new description",
                "new location",
                new Timestamp(START_TIMESTAMP_TEST),
                new Timestamp(END_TIMESTAMP_TEST)),
            500);

    // mock the event
    EventsRecord myEvent = myJooqMock.getContext().newRecord(EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("old title");
    myEvent.setCapacity(5);
    myEvent.setThumbnail("old thumbnail");
    myEvent.setDescription("old description");
    myEvent.setLocation("old location");
    myEvent.setPrice(500);
    myEvent.setStartTime(new Timestamp(0));
    myEvent.setEndTime(new Timestamp(0));
    myJooqMock.addReturn("SELECT", myEvent);

    // mock event database operations
    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(4);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);

    myJooqMock.addReturn("SELECT", myEventRegistration);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    myJooqMock.addReturn("UPDATE", myEvent);
    myJooqMock.addReturn("SELECT", myEvent);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    myEventsProcessorImpl.modifyEvent(0, req, myUserData);

    Object[] updateBindings = myJooqMock.getSqlBindings().get("UPDATE").get(0);

    assertEquals(9, updateBindings.length);
    assertEquals(req.getTitle(), updateBindings[0]);
    assertEquals(req.getDetails().getDescription(), updateBindings[1]);
    assertEquals(req.getSpotsAvailable(), updateBindings[2]);
    assertEquals(req.getDetails().getLocation(), updateBindings[3]);
    assertEquals(req.getDetails().getStart(), updateBindings[4]);
    assertEquals(req.getDetails().getEnd(), updateBindings[5]);
    assertEquals(req.getThumbnail(), updateBindings[6]);
    assertEquals(myEvent.getPrice(), updateBindings[7]);
    assertEquals(myEvent.getId(), updateBindings[8]);
  }

  // modifying an event with the event details null
  @Test
  public void testModifyEvent3() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    ModifyEventRequest req =
        new ModifyEventRequest("edited title", 10, "edited thumbnail", null, 10);

    // mock the events
    EventsRecord myEvent = myJooqMock.getContext().newRecord(EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("old title");
    myEvent.setCapacity(5);
    myEvent.setThumbnail("old thumbnail");
    myEvent.setDescription("old description");
    myEvent.setLocation("old location");
    myEvent.setStartTime(new Timestamp(0));
    myEvent.setEndTime(new Timestamp(0));
    myEvent.setPrice(10);
    myJooqMock.addReturn("SELECT", myEvent);

    // mock event database operations
    Record1<Integer> myEventRegistration = myJooqMock.getContext().newRecord(EVENTS.CAPACITY);
    myEventRegistration.values(5);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(3);

    myJooqMock.addReturn("SELECT", myEventRegistration);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    myJooqMock.addReturn("UPDATE", myEvent);
    myJooqMock.addReturn("SELECT", myEvent);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    myEventsProcessorImpl.modifyEvent(0, req, myUserData);

    Object[] updateBindings = myJooqMock.getSqlBindings().get("UPDATE").get(0);

    assertEquals(5, updateBindings.length);
    assertEquals(req.getTitle(), updateBindings[0]);
    assertEquals(req.getSpotsAvailable(), updateBindings[1]);
    assertEquals(req.getThumbnail(), updateBindings[2]);
    assertEquals(myEvent.getPrice(), updateBindings[3]);
    assertEquals(myEvent.getId(), updateBindings[4]);
  }

  // modifying an event with the event details null and some other fields null
  @Test
  public void testModifyEvent4() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    ModifyEventRequest req =
        new ModifyEventRequest("edited title", null, "edited thumbnail", null, 20);

    // mock the event
    EventsRecord myEvent = myJooqMock.getContext().newRecord(EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("old title");
    myEvent.setCapacity(5);
    myEvent.setThumbnail("old thumbnail");
    myEvent.setDescription("old description");
    myEvent.setLocation("old location");
    myEvent.setPrice(20);
    myEvent.setStartTime(new Timestamp(0));
    myEvent.setEndTime(new Timestamp(0));
    myJooqMock.addReturn("SELECT", myEvent);
    myJooqMock.addReturn("SELECT", myEvent);
    myJooqMock.addReturn("UPDATE", myEvent);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    myEventsProcessorImpl.modifyEvent(0, req, myUserData);

    Object[] updateBindings = myJooqMock.getSqlBindings().get("UPDATE").get(0);

    assertEquals(4, updateBindings.length);
    assertEquals(req.getTitle(), updateBindings[0]);
    assertEquals(req.getThumbnail(), updateBindings[1]);
    assertEquals(myEvent.getPrice(), updateBindings[2]);
    assertEquals(myEvent.getId(), updateBindings[3]);
  }

  // modifying an event with the every field null
  @Test
  public void testModifyEvent5() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    ModifyEventRequest req = new ModifyEventRequest(null, null, null, null, null);

    // mock the event
    EventsRecord myEvent = myJooqMock.getContext().newRecord(EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("old title");
    myEvent.setCapacity(5);
    myEvent.setThumbnail("old thumbnail");
    myEvent.setDescription("old description");
    myEvent.setLocation("old location");
    myEvent.setPrice(65);
    myEvent.setStartTime(new Timestamp(0));
    myEvent.setEndTime(new Timestamp(0));
    myJooqMock.addReturn("SELECT", myEvent);
    myJooqMock.addReturn("SELECT", myEvent);
    myJooqMock.addReturn("UPDATE", myEvent);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    myEventsProcessorImpl.modifyEvent(0, req, myUserData);

    List<Object[]> updateBindings = myJooqMock.getSqlBindings().get("UPDATE");

    assertTrue(updateBindings.isEmpty());
  }

  // modifying an event with some details fields non-null and some non-details fields non-null
  @Test
  public void testModifyEvent6() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    ModifyEventRequest req =
        new ModifyEventRequest(
            "edited title",
            null,
            "edited thumbnail",
            new EventDetails("new description", "new location", null, null),
            50);

    EventsRecord myEvent = myJooqMock.getContext().newRecord(EVENTS);
    myEvent.setId(0);
    myEvent.setTitle("old title");
    myEvent.setCapacity(5);
    myEvent.setThumbnail("old thumbnail");
    myEvent.setDescription("old description");
    myEvent.setLocation("old location");
    myEvent.setPrice(50);
    myEvent.setStartTime(new Timestamp(0));
    myEvent.setEndTime(new Timestamp(0));
    myJooqMock.addReturn("SELECT", myEvent);
    myJooqMock.addReturn("SELECT", myEvent);
    myJooqMock.addReturn("UPDATE", myEvent);

    // mock the ticket count
    Record2<Integer, Integer> ticketCount =
        myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    ticketCount.values(0, 5);
    myJooqMock.addReturn("SELECT", ticketCount);

    myEventsProcessorImpl.modifyEvent(0, req, myUserData);

    Object[] updateBindings = myJooqMock.getSqlBindings().get("UPDATE").get(0);

    assertEquals(6, updateBindings.length);
    assertEquals(req.getTitle(), updateBindings[0]);
    assertEquals(req.getDetails().getDescription(), updateBindings[1]);
    assertEquals(req.getDetails().getLocation(), updateBindings[2]);
    assertEquals(req.getThumbnail(), updateBindings[3]);
    assertEquals(req.getPrice(), updateBindings[4]);
    assertEquals(myEvent.getId(), updateBindings[5]);
  }

  // deleting an event fails if the user isn't an admin
  @Test
  public void testDeleteEvent1() {
    JWTData nonAdmin = new JWTData(0, PrivilegeLevel.GP);

    try {
      myEventsProcessorImpl.deleteEvent(0, nonAdmin);
      fail();
    } catch (AdminOnlyRouteException ignored) {
    }
  }

  // test deleting an event properly
  @Test
  public void testDeleteEvent2() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    int deletedEventId = 42;
    EventsRecord eventToDelete = myJooqMock.getContext().newRecord(EVENTS);
    eventToDelete.setId(deletedEventId);
    eventToDelete.setTitle("sample title");
    eventToDelete.setDescription("sample description");
    myJooqMock.addReturn("DELETE", eventToDelete);

    myEventsProcessorImpl.deleteEvent(42, myUserData);

    Object[] deleteBindings = myJooqMock.getSqlBindings().get("DELETE").get(0);

    assertEquals(deletedEventId, deleteBindings[0]);
  }

  /**
   * NOTE: The following tests for {@code getEventRegisteredUsers} were written by Conner, not
   * Brandon.
   */
  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void testGetEventRegisteredUsersIncorrectPrivilegeLevel(int privLevel) {
    PrivilegeLevel level = PrivilegeLevel.from(privLevel);
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(level);

    try {
      myEventsProcessorImpl.getEventRegisteredUsers(1, jwtData);
    } catch (AdminOnlyRouteException ignored) {
    }
  }

  @Test
  public void testGetEventsRegisteredUsersNoEvent() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addEmptyReturn("SELECT");

    try {
      myEventsProcessorImpl.getEventRegisteredUsers(125, jwtData);
    } catch (EventDoesNotExistException e) {
      assertEquals(125, e.getEventId());
    }
  }

  @Test
  public void testGetEventsRegisteredUsersEmptyReturn() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("SELECT", new EventsRecord());

    myJooqMock.addEmptyReturn("SELECT");

    EventRegistrations regs1 = myEventsProcessorImpl.getEventRegisteredUsers(1, jwtData);
    assertEquals(0, regs1.getRegistrations().size());
  }

  @Test
  public void testGetEventsRegisteredUsersSingleReturn() {
    int ticketCount = 5;
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("SELECT", new EventsRecord());

    Record4<String, String, String, Integer> result =
        myJooqMock
            .getContext()
            .newRecord(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                EVENT_REGISTRATIONS.TICKET_QUANTITY);
    result.values("Conner", "Nilsen", "connernilsen@gmail.com", ticketCount);
    myJooqMock.addReturn("SELECT", result);

    EventRegistrations regs = myEventsProcessorImpl.getEventRegisteredUsers(1, jwtData);

    assertEquals(1, regs.getRegistrations().size());
    Registration reg0 = regs.getRegistrations().get(0);
    assertEquals("Conner", reg0.getFirstName());
    assertEquals("Nilsen", reg0.getLastName());
    assertEquals("connernilsen@gmail.com", reg0.getEmail());
    assertEquals(ticketCount, reg0.getTicketCount());
  }

  @Test
  public void testGetEventsRegisteredUsersMultiReturn() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("SELECT", new EventsRecord());

    Result<Record4<String, String, String, Integer>> result =
        myJooqMock
            .getContext()
            .newResult(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                EVENT_REGISTRATIONS.TICKET_QUANTITY);

    for (int i = 1; i < 6; i++) {
      Record4<String, String, String, Integer> tempRes =
          myJooqMock
              .getContext()
              .newRecord(
                  CONTACTS.FIRST_NAME,
                  CONTACTS.LAST_NAME,
                  CONTACTS.EMAIL,
                  EVENT_REGISTRATIONS.TICKET_QUANTITY);
      tempRes.values("Conner" + i, "Nilsen" + i, "connernilsen@gmail.com" + i, i);
      result.add(tempRes);
    }
    myJooqMock.addReturn("SELECT", result);

    EventRegistrations regs = myEventsProcessorImpl.getEventRegisteredUsers(1, jwtData);

    assertEquals(5, regs.getRegistrations().size());
    for (int i = 1; i < 6; i++) {
      Registration reg = regs.getRegistrations().get(i - 1);
      assertEquals("Conner" + i, reg.getFirstName());
      assertEquals("Nilsen" + i, reg.getLastName());
      assertEquals("connernilsen@gmail.com" + i, reg.getEmail());
      assertEquals(i, reg.getTicketCount());
    }
  }

  private void prepSignedUp(int count) {
    EventsRecord eventResult = myJooqMock.getContext().newRecord(EVENTS);
    eventResult.setStartTime(Timestamp.from(Instant.now()));
    eventResult.setEndTime(Timestamp.from(Instant.now()));
    eventResult.setLocation("HERE");
    eventResult.setCapacity(5);
    eventResult.setDescription("DESC");
    eventResult.setTitle("TITLE");
    eventResult.setId(1);
    eventResult.setPrice(500);
    myJooqMock.addReturn("SELECT", eventResult);
    if (count >= 0) {
      Record2<Integer, Integer> record =
          myJooqMock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
      record.values(1, count);
      myJooqMock.addReturn("SELECT", record);
    } else {
      myJooqMock.addEmptyReturn("SELECT");
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 1, 2, 3})
  public void testGetEventsSignedUpUserSignedUp(int ticketCount) {
    prepSignedUp(ticketCount);

    GetUserEventsRequest req =
        new GetUserEventsRequest(Optional.empty(), Optional.empty(), Optional.empty());
    JWTData data = new JWTData(1, PrivilegeLevel.GP);
    GetEventsResponse resp = myEventsProcessorImpl.getEventsSignedUp(req, data);

    assertEquals(ticketCount == -1 ? 0 : ticketCount, resp.getEvents().get(0).getTicketCount());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 1, 2})
  public void testGetEventsQualifiedUserSignedUp(int count) {
    JWTData data = new JWTData(1, PrivilegeLevel.GP);

    prepSignedUp(count);

    GetEventsResponse resp = myEventsProcessorImpl.getEventsQualified(data);
    assertEquals(count == -1 ? 0 : count, resp.getEvents().get(0).getTicketCount());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 1, 2})
  public void testGetSingleEventUserSignedUp(int count) {
    JWTData data = new JWTData(1, PrivilegeLevel.GP);

    prepSignedUp(count);
    GetEventsResponse resp = myEventsProcessorImpl.getEventsQualified(data);
    assertEquals(count == -1 ? 0 : count, resp.getEvents().get(0).getTicketCount());
  }
}
