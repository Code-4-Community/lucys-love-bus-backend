package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.codeforcommunity.JooqMock;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.responses.GetPublicEventsResponse;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PublicEventsProcessorImplTest {

  private PublicEventsProcessorImpl myPublicEventsProcessorImpl;
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
    this.myPublicEventsProcessorImpl = new PublicEventsProcessorImpl(myJooqMock.getContext());

    // mock Amazon S3
    AmazonS3Client mockS3Client = mock(AmazonS3Client.class);
    PutObjectResult mockPutObjectResult = mock(PutObjectResult.class);
    S3Requester.Externs mockExterns = mock(S3Requester.Externs.class);

    when(mockS3Client.putObject(any(PutObjectRequest.class))).thenReturn(mockPutObjectResult);
    when(mockExterns.getS3Client()).thenReturn(mockS3Client);

    S3Requester.setExterns(mockExterns);
  }

  // Test getting events when the given event ids don't exist
  @Test
  public void testGetEvents1() {
    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);

    myJooqMock.addEmptyReturn("SELECT");

    GetPublicEventsResponse res = myPublicEventsProcessorImpl.getPublicEvents(eventIds);
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
    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    GetPublicEventsResponse res = myPublicEventsProcessorImpl.getPublicEvents(eventIds);

    assertEquals(event1.getId(), res.getEvents().get(0).getId());
    assertEquals(event1.getTitle(), res.getEvents().get(0).getTitle());
    assertEquals(1, res.getTotalCount());
  }

  // Test getting multiple events by id
  @Test
  public void testGetEvents3() {
    EventsRecord event1 = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event1.setId(0);
    event1.setCapacity(10);
    event1.setPrice(10000);
    event1.setTitle("title 1");
    event1.setEndTime(new Timestamp(END_TIMESTAMP_TEST));
    EventsRecord event2 = myJooqMock.getContext().newRecord(Tables.EVENTS);
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
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(5);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    List<Integer> eventIds = new ArrayList<>();
    eventIds.add(0);
    eventIds.add(1);

    GetPublicEventsResponse res = myPublicEventsProcessorImpl.getPublicEvents(eventIds);

    assertEquals(event1.getId(), res.getEvents().get(0).getId());
    assertEquals(event1.getTitle(), res.getEvents().get(0).getTitle());
    assertEquals(2, res.getTotalCount());
  }
}
