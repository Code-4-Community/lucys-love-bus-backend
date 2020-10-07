package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.requester.Emailer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.AnnouncementsRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains tests for AnnouncementsProcessorImpl.java in main
public class AnnouncementsProcessorImplTest {

  private JooqMock myJooqMock;
  private AnnouncementsProcessorImpl myAnnouncementsProcessorImpl;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int START_TIMESTAMP_TEST = 1587000000;
  // 04/17/2020 @ 5:06am (UTC)
  private final int END_TIMESTAMP_TEST = 1587100000;
  // 04/04/2020 @ 11:33am (UTC)
  private final int START_TIMESTAMP_TEST2 = 1586000000;

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myAnnouncementsProcessorImpl =
        new AnnouncementsProcessorImpl(
            myJooqMock.getContext(), new Emailer(myJooqMock.getContext()));
  }

  // test getting announcements with range covering no events
  @Test
  public void testGetAnnouncements1() {
    // craft the get request
    GetAnnouncementsRequest req =
        new GetAnnouncementsRequest(new Timestamp(0), new Timestamp(1000), 2);

    myJooqMock.addEmptyReturn("SELECT");
    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 0);
  }

  // test getting announcements with range covering all events
  @Test
  public void testGetAnnouncements2() {
    // craft the get request
    GetAnnouncementsRequest req =
        new GetAnnouncementsRequest(
            new Timestamp(START_TIMESTAMP_TEST2 - 1000),
            new Timestamp(END_TIMESTAMP_TEST + 1000),
            2);

    // mock the announcements inside the DB
    AnnouncementsRecord announcement1 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement1.setId(0);
    announcement1.setTitle("the first announcement title");
    announcement1.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement1.setDescription("the first announcement description");

    AnnouncementsRecord announcement2 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement2.setId(1);
    announcement2.setTitle("the second announcement title");
    announcement2.setCreated(new Timestamp(START_TIMESTAMP_TEST2));
    announcement2.setDescription("the second announcement description");

    List<AnnouncementsRecord> announcements = new ArrayList<>();
    announcements.add(announcement1);
    announcements.add(announcement2);
    myJooqMock.addReturn("SELECT", announcements);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 2);
    assertEquals(res.getAnnouncements().get(0).getId(), 0);
    assertEquals(res.getAnnouncements().get(1).getId(), 1);
  }

  // test getting announcements with range covering some events
  @Test
  public void testGetAnnouncements3() {
    // craft the get request
    GetAnnouncementsRequest req =
        new GetAnnouncementsRequest(
            new Timestamp(START_TIMESTAMP_TEST - 1000),
            new Timestamp(END_TIMESTAMP_TEST + 1000),
            2);

    // mock the announcements inside the DB
    AnnouncementsRecord announcement1 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement1.setId(0);
    announcement1.setTitle("the first announcement title");
    announcement1.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement1.setDescription("the first announcement description");
    myJooqMock.addReturn("SELECT", announcement1);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 1);
    assertEquals(res.getAnnouncements().get(0).getId(), 0);
  }

  // getting an event specific announcement fails if event id is non-positive
  @Test
  public void testGetEventSpecificAnnouncements1() {
    GetEventSpecificAnnouncementsRequest req = new GetEventSpecificAnnouncementsRequest(0);

    try {
      myAnnouncementsProcessorImpl.getEventSpecificAnnouncements(req);
      fail();
    } catch (MalformedParameterException e) {
      assertEquals(e.getParameterName(), "event_id");
    }
  }

  // getting an event specific announcement fails with no matching event ids
  @Test
  public void testGetEventSpecificAnnouncements2() {
    GetEventSpecificAnnouncementsRequest req = new GetEventSpecificAnnouncementsRequest(3);

    myJooqMock.addEmptyReturn("SELECT");

    try {
      myAnnouncementsProcessorImpl.getEventSpecificAnnouncements(req);
      fail();
    } catch (MalformedParameterException e) {
      assertEquals(e.getParameterName(), "event_id");
    }
  }

  // getting an event specific announcement succeeds with event with one announcement in database
  @Test
  public void testGetEventSpecificAnnouncements3() {
    GetEventSpecificAnnouncementsRequest req = new GetEventSpecificAnnouncementsRequest(1);

    // mock the specific event inside the DB
    EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event.setId(1);
    myJooqMock.addReturn("SELECT", event);

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setEventId(1);
    announcement.setId(1);
    announcement.setTitle("sample title");
    announcement.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement.setDescription("sample description");
    myJooqMock.addReturn("SELECT", announcement);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getEventSpecificAnnouncements(req);

    assertEquals(res.getTotalCount(), 1);
    assertEquals(res.getAnnouncements().get(0).getEventId(), (Integer) 1);
    assertEquals(res.getAnnouncements().get(0).getId(), 1);
    assertEquals(res.getAnnouncements().get(0).getTitle(), "sample title");
    assertEquals(res.getAnnouncements().get(0).getCreated(), new Timestamp(START_TIMESTAMP_TEST));
    assertEquals(res.getAnnouncements().get(0).getDescription(), "sample description");
  }

  // getting an event specific announcement succeeds with event with multiple announcements in
  // database
  @Test
  public void testGetEventSpecificAnnouncements4() {
    GetEventSpecificAnnouncementsRequest req = new GetEventSpecificAnnouncementsRequest(1);

    // mock the specific event inside the DB
    EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event.setId(1);
    myJooqMock.addReturn("SELECT", event);

    // mock the announcements inside the DB
    AnnouncementsRecord announcement1 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement1.setEventId(1);
    announcement1.setId(1);
    announcement1.setTitle("sample title");
    announcement1.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement1.setDescription("sample description");

    AnnouncementsRecord announcement2 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement2.setEventId(1);
    announcement2.setId(2);
    announcement2.setTitle("lucy's love bus");
    announcement2.setCreated(new Timestamp(START_TIMESTAMP_TEST2));
    announcement2.setDescription("code for community");

    List<AnnouncementsRecord> announcements = new ArrayList<>();
    announcements.add(announcement1);
    announcements.add(announcement2);
    myJooqMock.addReturn("SELECT", announcements);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getEventSpecificAnnouncements(req);

    assertEquals(res.getTotalCount(), 2);
    assertEquals(res.getAnnouncements().get(0).getEventId(), (Integer) 1);
    assertEquals(res.getAnnouncements().get(0).getId(), 1);
    assertEquals(res.getAnnouncements().get(0).getTitle(), "sample title");
    assertEquals(res.getAnnouncements().get(0).getCreated(), new Timestamp(START_TIMESTAMP_TEST));
    assertEquals(res.getAnnouncements().get(0).getDescription(), "sample description");
    assertEquals(res.getAnnouncements().get(1).getEventId(), (Integer) 1);
    assertEquals(res.getAnnouncements().get(1).getId(), 2);
    assertEquals(res.getAnnouncements().get(1).getTitle(), "lucy's love bus");
    assertEquals(res.getAnnouncements().get(1).getCreated(), new Timestamp(START_TIMESTAMP_TEST2));
    assertEquals(res.getAnnouncements().get(1).getDescription(), "code for community");
  }
}
