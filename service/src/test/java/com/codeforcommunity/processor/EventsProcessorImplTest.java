package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.components.Registration;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Contains tests for EventsProcessorImpl.java in main
public class EventsProcessorImplTest {

  private EventsProcessorImpl myEventsProcessorImpl;
  private EventDatabaseOperations myEventDatabaseOperations;
  private JooqMock myJooqMock;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int START_TIMESTAMP_TEST = 1587000000;
  // 04/17/2020 @ 5:06am (UTC)
  private final int END_TIMESTAMP_TEST = 1587100000;

  @BeforeEach
  private void setup() {
    this.myJooqMock = new JooqMock();
    this.myEventDatabaseOperations = new EventDatabaseOperations(myJooqMock.getContext());
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
        new CreateEventRequest("sample", 5, "sample thumbnail", myEventDetails);

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

    CreateEventRequest req = new CreateEventRequest(
        "sample",
        5,
        Base64TestStrings.TEST_STRING_1,
        myEventDetails);

    // mock the DB
    JWTData goodUser = mock(JWTData.class);
    when(goodUser.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    EventsRecord record = myJooqMock.getContext().newRecord(Tables.EVENTS);
    record.setId(0);
    record.setCapacity(req.getSpotsAvailable());
    myJooqMock.addReturn("INSERT", record);
    myJooqMock.addReturn("SELECT", record);

    SingleEventResponse res = myEventsProcessorImpl.createEvent(req, goodUser);

    assertEquals(res.getId(), 0);
    assertEquals(res.getTitle(), "sample");
    assertEquals(res.getCapacity(), 5);
    // TODO: please verify if this is intended
    assertEquals(res.getThumbnail(),
        "https://lucys-love-bus-public.s3.us-east-2.amazonaws.com/events/sample_thumbnail.gif");
    assertEquals(res.getDetails().getDescription(), myEventDetails.getDescription());
    assertEquals(res.getDetails().getLocation(), myEventDetails.getLocation());
    assertEquals(res.getDetails().getEnd(), myEventDetails.getEnd());
    assertEquals(res.getDetails().getStart(), myEventDetails.getStart());
  }

  // test getting an event id that's not there
  @Test
  public void testGetSingleEvent1() {
    // mock the DB
    List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
    myJooqMock.addReturn("SELECT", emptySelectStatement);

    try {
      myEventsProcessorImpl.getSingleEvent(5);
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
        new CreateEventRequest("sample", 5, "sample thumbnail", myEventDetails);

    // mock the DB for events
    EventsRecord record = myJooqMock.getContext().newRecord(Tables.EVENTS);
    record.setId(1);
    record.setThumbnail(myEventRequest.getThumbnail());
    record.setTitle(myEventRequest.getTitle());
    record.setCapacity(myEventRequest.getSpotsAvailable());
    record.setLocation(myEventDetails.getLocation());
    record.setDescription(myEventDetails.getDescription());
    record.setStartTime(myEventDetails.getStart());
    record.setEndTime(myEventDetails.getEnd());
    myJooqMock.addReturn("SELECT", record);
    myJooqMock.addReturn("INSERT", record);

    // mock the DB for event registrations
    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);

    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(5);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    SingleEventResponse res = myEventsProcessorImpl.getSingleEvent(1);

    assertEquals(res.getId(), 1);
    assertEquals(res.getSpotsAvailable(), 4);
    assertEquals(res.getCapacity(), 5);
    assertEquals(res.getThumbnail(), myEventRequest.getThumbnail());
    assertEquals(res.getTitle(), myEventRequest.getTitle());

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

    GetEventsResponse res = myEventsProcessorImpl.getEvents(eventIds);
    assertEquals(0, res.getEvents().size());
    assertEquals(0, res.getTotalCount());
  }

  // Test getting exactly one event by id
  @Test
  public void testGetEvents2() {
    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);

    EventsRecord event1 = new EventsRecord();
    event1.setId(0);
    event1.setTitle("title 1");
    myJooqMock.addReturn("SELECT", event1);

    GetEventsResponse res = myEventsProcessorImpl.getEvents(eventIds);
    assertEquals(event1.getId(), res.getEvents().get(0).getId());
    assertEquals(event1.getTitle(), res.getEvents().get(0).getTitle());
    assertEquals(1, res.getTotalCount());
  }

  // Test getting multiple events by id
  @Test
  public void testGetEvents3() {
    EventsRecord event1 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event1.setId(0);
    event1.setTitle("title 1");
    EventsRecord event2 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event1.setId(1);
    event1.setTitle("title 2");

    List<UpdatableRecordImpl> eventRecords = new ArrayList<>();
    eventRecords.add(event1);
    eventRecords.add(event2);
    myJooqMock.addReturn("SELECT", eventRecords);
    myJooqMock.addReturn("INSERT", eventRecords);

    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);
    eventIds.add(1);

    GetEventsResponse res = myEventsProcessorImpl.getEvents(eventIds);

    // TODO: fix org.jooq.exception.TooManyRowsException: Cursor returned more than one result
    assertEquals(event1.getId(), res.getEvents().get(0).getId());
    assertEquals(event1.getTitle(), res.getEvents().get(0).getTitle());
    assertEquals(2, res.getTotalCount());
  }

  // test getting events user signed up for if there are none
  @Test
  public void testGetEventsSignedUp1() {
    GetUserEventsRequest req = new GetUserEventsRequest(
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
    GetUserEventsRequest req = new GetUserEventsRequest(
        Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
        Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
        Optional.of(1));

    EventsRecord myEvent1 = new EventsRecord();
    myEvent1.setId(0);
    myEvent1.setTitle("Event 1");
    myEvent1.setDescription("Description 1");
    myJooqMock.addReturn("SELECT", myEvent1);

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(1, res.getTotalCount());

    Event actualEvent = res.getEvents().get(0);

    assertEquals(myEvent1.getId(), actualEvent.getId());
    assertEquals(myEvent1.getTitle(), actualEvent.getTitle());
    assertEquals(myEvent1.getDescription(), actualEvent.getDetails().getDescription());
  }

  // test getting multiple events user signed up for with no limit
  @Test
  public void testGetEventsSignedUp3() {
    GetUserEventsRequest req = new GetUserEventsRequest(
        Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
        Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
        Optional.of(3));

    // add three events to the mock DB
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(0);
    myEvent1.setTitle("Event 1");
    myEvent1.setDescription("Description 1");

    EventsRecord myEvent2 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(1);
    myEvent1.setTitle("Event 2");
    myEvent1.setDescription("Description 2");

    EventsRecord myEvent3 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(2);
    myEvent1.setTitle("Event 2");
    myEvent1.setDescription("Description 2");


    List<UpdatableRecordImpl> events = new ArrayList<>();
    events.add(myEvent1);
    events.add(myEvent2);
    events.add(myEvent3);
    myJooqMock.addReturn("SELECT", events);

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    // TODO: fix org.jooq.exception.TooManyRowsException: Cursor returned more than one result
    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(3, res.getTotalCount());

    Event actualEvent1 = res.getEvents().get(0);
    Event actualEvent2 = res.getEvents().get(1);
    Event actualEvent3 = res.getEvents().get(2);

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
    GetUserEventsRequest req = new GetUserEventsRequest(
        Optional.of(new Timestamp(END_TIMESTAMP_TEST)),
        Optional.of(new Timestamp(START_TIMESTAMP_TEST)),
        Optional.of(1));

    // add three events to the mock DB
    EventsRecord myEvent1 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(0);
    myEvent1.setTitle("Event 1");
    myEvent1.setDescription("Description 1");

    EventsRecord myEvent2 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(1);
    myEvent1.setTitle("Event 2");
    myEvent1.setDescription("Description 2");

    EventsRecord myEvent3 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    myEvent1.setId(2);
    myEvent1.setTitle("Event 2");
    myEvent1.setDescription("Description 2");


    List<UpdatableRecordImpl> events = new ArrayList<>();
    events.add(myEvent1);
    events.add(myEvent2);
    events.add(myEvent3);
    myJooqMock.addReturn("SELECT", events);

    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    // TODO: fix org.jooq.exception.TooManyRowsException: Cursor returned more than one result
    GetEventsResponse res = myEventsProcessorImpl.getEventsSignedUp(req, myUserData);

    assertEquals(1, res.getTotalCount());

    Event actualEvent1 = res.getEvents().get(0);

    assertEquals(myEvent1.getId(), actualEvent1.getId());
    assertEquals(myEvent1.getTitle(), actualEvent1.getTitle());
    assertEquals(myEvent1.getDescription(), actualEvent1.getDetails().getDescription());
  }

  // TODO
  @Test
  public void testGetEventsQualified() {
    fail("TODO!!!");
  }

  // TODO
  @Test
  public void testModifyEvent() {
    fail("TODO!!!");
  }

  // TODO
  @Test
  public void testDeleteEvent() {
    fail("TODO!!!");
  }

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
}
