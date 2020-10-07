package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.*;
import static org.jooq.generated.Tables.CONTACTS;
import static org.junit.jupiter.api.Assertions.*;
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
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Timestamp;
import java.util.List;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ProtectedEventsProcessorImplTest {

  private ProtectedEventsProcessorImpl myEventsProcessorImpl;
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
    this.myEventsProcessorImpl = new ProtectedEventsProcessorImpl(myJooqMock.getContext());

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
    EventsRecord record = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
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
    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(5);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
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
    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
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

    EventsRecord myEvent = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
    EventsRecord eventToDelete = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
}
