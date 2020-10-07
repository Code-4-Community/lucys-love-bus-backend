package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
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

// Contains tests for ProtectedAnnouncementsProcessorImpl.java in main
public class ProtectedAnnouncementsProcessorImplTest {

  private JooqMock myJooqMock;
  private ProtectedAnnouncementsProcessorImpl myAnnouncementsProcessorImpl;

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
        new ProtectedAnnouncementsProcessorImpl(
            myJooqMock.getContext(), new Emailer(myJooqMock.getContext()));
  }

  // test posting an announcement without admin privileges
  @Test
  public void testPostAnnouncements1() {
    // make the request object
    PostAnnouncementRequest req = new PostAnnouncementRequest("sample title", "sample description");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    try {
      myAnnouncementsProcessorImpl.postAnnouncement(req, myUserData);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test posting an announcement normally
  @Test
  public void testPostAnnouncements2() {
    // make the request object
    PostAnnouncementRequest req = new PostAnnouncementRequest("sample title", "sample description");

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setId(0);
    announcement.setTitle("sample title");
    announcement.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement.setDescription("sample description");
    myJooqMock.addReturn("SELECT", announcement);
    myJooqMock.addReturn("INSERT", announcement);

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    PostAnnouncementResponse res = myAnnouncementsProcessorImpl.postAnnouncement(req, myUserData);

    assertEquals(res.getAnnouncement().getDescription(), "sample description");
    assertEquals(res.getAnnouncement().getTitle(), "sample title");
    assertEquals(res.getAnnouncement().getId(), 0);
    assertEquals(res.getAnnouncement().getCreated(), new Timestamp(START_TIMESTAMP_TEST));
  }

  // posting an event specific announcement fails if user isn't an admin
  @Test
  public void testPostEventSpecificAnnouncement1() {
    PostAnnouncementRequest req = new PostAnnouncementRequest("c4c", "code for community");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.GP);

    try {
      myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // posting an event specific announcement fails if no matching events
  @Test
  public void testPostEventSpecificAnnouncement2() {
    PostAnnouncementRequest req = new PostAnnouncementRequest("c4c", "code for community");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    // return no events
    myJooqMock.addEmptyReturn("SELECT");

    try {
      myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, -1);
      fail();
    } catch (MalformedParameterException e) {
      assertEquals(e.getParameterName(), "event_id");
    }
  }

  // posting an event specific announcement succeeds with event with no announcements yet
  @Test
  public void testPostEventSpecificAnnouncement3() {
    PostAnnouncementRequest req = new PostAnnouncementRequest("c4c", "code for community");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    // mock the specific event inside the DB
    EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event.setId(1);
    myJooqMock.addReturn("SELECT", event);

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setId(1);
    announcement.setEventId(1);
    announcement.setTitle("c4c");
    announcement.setDescription("code for community");
    myJooqMock.addReturn("SELECT", announcement);
    myJooqMock.addReturn("INSERT", announcement);

    // mock sending event specific announcement email
    myJooqMock.addReturn("SELECT", event);

    PostAnnouncementResponse res =
        myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
    assertEquals(res.getAnnouncement().getEventId(), announcement.getEventId());
    assertEquals((Integer) res.getAnnouncement().getId(), announcement.getId());
    assertEquals(res.getAnnouncement().getTitle(), req.getTitle());
    assertEquals(res.getAnnouncement().getDescription(), req.getDescription());
  }

  // posting an event specific announcement succeeds with event with one announcement already
  @Test
  public void testPostEventSpecificAnnouncement4() {
    PostAnnouncementRequest req = new PostAnnouncementRequest("LLB", "Lucy's Love Bus");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    // mock the specific event inside the DB
    EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event.setId(1);
    myJooqMock.addReturn("SELECT", event);

    // mock the announcements inside the DB
    AnnouncementsRecord announcement1 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement1.setId(1);
    announcement1.setEventId(1);
    announcement1.setTitle("c4c");
    announcement1.setDescription("code for community");

    AnnouncementsRecord announcement2 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement2.setId(2);
    announcement2.setEventId(1);
    announcement2.setTitle("LLB");
    announcement2.setDescription("Lucy's Love Bus");

    List<AnnouncementsRecord> announcements = new ArrayList<>();
    announcements.add(announcement1);
    announcements.add(announcement2);
    myJooqMock.addReturn("SELECT", announcements);
    myJooqMock.addReturn("INSERT", announcements);

    // mock sending event specific announcement email
    myJooqMock.addReturn("SELECT", event);

    PostAnnouncementResponse res =
        myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
    assertEquals(res.getAnnouncement().getEventId(), announcement1.getEventId());
    assertEquals((Integer) res.getAnnouncement().getId(), announcement1.getId());
    assertEquals(res.getAnnouncement().getTitle(), announcement2.getTitle());
    assertEquals(res.getAnnouncement().getDescription(), announcement2.getDescription());
  }
}
