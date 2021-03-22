package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.codeforcommunity.JooqMock.OperationType;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.requester.Emailer;
import com.codeforcommunity.requester.S3Requester;
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
  private Emailer mockEmailer;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int START_TIMESTAMP_TEST = 1587000000;
  // 04/17/2020 @ 5:06am (UTC)
  private final int END_TIMESTAMP_TEST = 1587100000;
  // 04/04/2020 @ 11:33am (UTC)
  private final int START_TIMESTAMP_TEST2 = 1586000000;

  // sample public bucket URL to be used in tests
  private final String BUCKET_PUBLIC_URL = "https://test-bucket.s3.us-east-2.amazonaws.com";
  // sample public bucket name to be used in tests
  private final String BUCKET_PUBLIC_NAME = "test-bucket";
  // sample directory name to be used in tests
  private final String DIR_NAME = "test-dir";

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.mockEmailer = mock(Emailer.class);
    this.myAnnouncementsProcessorImpl =
        new AnnouncementsProcessorImpl(myJooqMock.getContext(), this.mockEmailer);

    // mock Amazon S3
    AmazonS3Client mockS3Client = mock(AmazonS3Client.class);
    PutObjectResult mockPutObjectResult = mock(PutObjectResult.class);
    S3Requester.Externs mockExterns = mock(S3Requester.Externs.class);

    when(mockS3Client.putObject(any(PutObjectRequest.class))).thenReturn(mockPutObjectResult);
    when(mockExterns.getS3Client()).thenReturn(mockS3Client);
    when(mockExterns.getBucketPublic()).thenReturn(BUCKET_PUBLIC_NAME);
    when(mockExterns.getBucketPublicUrl()).thenReturn(BUCKET_PUBLIC_URL);
    when(mockExterns.getDirPublic()).thenReturn(DIR_NAME);

    S3Requester.setExterns(mockExterns);
  }

  // test getting announcements with range covering no events
  @Test
  public void testGetAnnouncements1() {
    // craft the get request
    GetAnnouncementsRequest req =
        new GetAnnouncementsRequest(new Timestamp(0), new Timestamp(1000), 2);

    myJooqMock.addEmptyReturn(OperationType.SELECT);
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
    myJooqMock.addReturn(OperationType.SELECT, announcements);

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
    myJooqMock.addReturn(OperationType.SELECT, announcement1);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 1);
    assertEquals(res.getAnnouncements().get(0).getId(), 0);
  }

  // test getting an announcement (with an image)
  @Test
  public void testGetSingleAnnouncementWithImageSrc() {
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
    announcement1.setImageSrc(
        "https://facts.net/wp-content/uploads/2020/07/monarch-butterfly-facts.jpg");
    myJooqMock.addReturn(OperationType.SELECT, announcement1);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 1);
    assertEquals(res.getAnnouncements().get(0).getId(), 0);
    assertEquals(
        res.getAnnouncements().get(0).getImageSrc(),
        "https://facts.net/wp-content/uploads/2020/07/monarch-butterfly-facts.jpg");
  }

  // test getting multiple announcements (with images)
  @Test
  public void testGetMultipleAnnouncementsWithImageSrc() {
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
    announcement1.setImageSrc(
        "https://facts.net/wp-content/uploads/2020/07/monarch-butterfly-facts.jpg");

    AnnouncementsRecord announcement2 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement2.setId(1);
    announcement2.setTitle("the second announcement title");
    announcement2.setCreated(new Timestamp(START_TIMESTAMP_TEST2));
    announcement2.setDescription("the second announcement description");

    List<AnnouncementsRecord> announcements = new ArrayList<>();
    announcements.add(announcement1);
    announcements.add(announcement2);
    myJooqMock.addReturn(OperationType.SELECT, announcements);

    GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 2);
    assertEquals(res.getAnnouncements().get(0).getId(), 0);
    assertEquals(res.getAnnouncements().get(1).getId(), 1);
    assertEquals(
        res.getAnnouncements().get(0).getImageSrc(),
        "https://facts.net/wp-content/uploads/2020/07/monarch-butterfly-facts.jpg");
    assertNull(res.getAnnouncements().get(1).getImageSrc());
  }

  // test posting an announcement without admin privileges
  @Test
  public void testPostAnnouncements1() {
    // make the request object
    PostAnnouncementRequest req = new PostAnnouncementRequest("sample title", "sample description");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.STANDARD);

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
    myJooqMock.addReturn(OperationType.SELECT, announcement);
    myJooqMock.addReturn(OperationType.INSERT, announcement);

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    PostAnnouncementResponse res = myAnnouncementsProcessorImpl.postAnnouncement(req, myUserData);

    assertEquals(res.getAnnouncement().getDescription(), "sample description");
    assertEquals(res.getAnnouncement().getTitle(), "sample title");
    assertEquals(res.getAnnouncement().getId(), 0);
    assertEquals(res.getAnnouncement().getCreated(), new Timestamp(START_TIMESTAMP_TEST));
  }

  // test posting an announcement with an image, without admin privileges
  @Test
  public void testTryPostAnnouncementWithImageSrcWithoutAdminPrivileges() {
    // make the request object
    PostAnnouncementRequest req =
        new PostAnnouncementRequest(
            "sample title", "sample description", Base64TestStrings.TEST_STRING_1);

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.STANDARD);

    try {
      myAnnouncementsProcessorImpl.postAnnouncement(req, myUserData);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test posting an announcement with an image
  @Test
  public void testPostSingleAnnouncementsWithImageSrc() {

    // make the request object
    PostAnnouncementRequest req =
        new PostAnnouncementRequest(
            "sample title", "sample description", Base64TestStrings.TEST_STRING_1);

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setId(0);
    announcement.setTitle("sample title");
    announcement.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement.setDescription("sample description");
    announcement.setImageSrc(BUCKET_PUBLIC_URL + "/" + DIR_NAME + "/sample_thumbnail.gif");
    myJooqMock.addReturn(OperationType.SELECT, announcement);
    myJooqMock.addReturn(OperationType.INSERT, announcement);

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    PostAnnouncementResponse res = myAnnouncementsProcessorImpl.postAnnouncement(req, myUserData);

    assertEquals(res.getAnnouncement().getDescription(), announcement.getDescription());
    assertEquals(res.getAnnouncement().getTitle(), announcement.getTitle());
    assertEquals(res.getAnnouncement().getId(), announcement.getId());
    assertEquals(res.getAnnouncement().getCreated(), new Timestamp(START_TIMESTAMP_TEST));
    assertEquals(res.getAnnouncement().getImageSrc(), announcement.getImageSrc());
  }

  // posting an event specific announcement fails if user isn't an admin
  @Test
  public void testPostEventSpecificAnnouncement1() {
    PostAnnouncementRequest req = new PostAnnouncementRequest("c4c", "code for community");

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.STANDARD);

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
    myJooqMock.addEmptyReturn(OperationType.SELECT);

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
    myJooqMock.addReturn(OperationType.SELECT, event);

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setId(1);
    announcement.setEventId(1);
    announcement.setTitle("c4c");
    announcement.setDescription("code for community");
    myJooqMock.addReturn(OperationType.SELECT, announcement);
    myJooqMock.addReturn(OperationType.INSERT, announcement);

    // mock sending event specific announcement email
    myJooqMock.addReturn(OperationType.SELECT, event);

    PostAnnouncementResponse res =
        myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
    assertEquals(res.getAnnouncement().getEventId(), announcement.getEventId());
    assertEquals((Integer) res.getAnnouncement().getId(), announcement.getId());
    assertEquals(res.getAnnouncement().getTitle(), req.getTitle());
    assertEquals(res.getAnnouncement().getDescription(), req.getDescription());
  }

  // posting an event specific announcement (with an image) succeeds with event with no
  // announcements yet
  @Test
  public void testPostEventSpecificAnnouncementWithImageSrc() {
    // make the request object
    PostAnnouncementRequest req =
        new PostAnnouncementRequest("c4c", "code for community", Base64TestStrings.TEST_STRING_1);

    // mock the user
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    // mock the specific event inside the DB
    EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
    event.setId(1);
    myJooqMock.addReturn(OperationType.SELECT, event);

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setEventId(1);
    announcement.setTitle("c4c");
    announcement.setDescription("code for community");
    myJooqMock.addReturn(OperationType.SELECT, announcement);
    myJooqMock.addReturn(OperationType.INSERT, announcement);

    // mock sending event specific announcement email
    myJooqMock.addReturn(OperationType.SELECT, event);

    PostAnnouncementResponse res =
        myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);

    assertEquals(res.getAnnouncement().getEventId(), announcement.getEventId());
    assertEquals(res.getAnnouncement().getDescription(), announcement.getDescription());
    assertEquals(res.getAnnouncement().getTitle(), announcement.getTitle());
    assertEquals(res.getAnnouncement().getId(), announcement.getId());
    // url will have random UUID so the best we can do is check the prefix containing the s3 bucket
    // url + directory name
    assertTrue(res.getAnnouncement().getImageSrc().startsWith(BUCKET_PUBLIC_URL + "/" + DIR_NAME));
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
    myJooqMock.addReturn(OperationType.SELECT, event);

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
    myJooqMock.addReturn(OperationType.SELECT, announcements);
    myJooqMock.addReturn(OperationType.INSERT, announcements);

    // mock sending event specific announcement email
    myJooqMock.addReturn(OperationType.SELECT, event);

    PostAnnouncementResponse res =
        myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
    assertEquals(res.getAnnouncement().getEventId(), announcement1.getEventId());
    assertEquals((Integer) res.getAnnouncement().getId(), announcement1.getId());
    assertEquals(res.getAnnouncement().getTitle(), announcement2.getTitle());
    assertEquals(res.getAnnouncement().getDescription(), announcement2.getDescription());
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

    myJooqMock.addEmptyReturn(OperationType.SELECT);

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
    myJooqMock.addReturn(OperationType.SELECT, event);

    // mock the announcement inside the DB
    AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcement.setEventId(1);
    announcement.setId(1);
    announcement.setTitle("sample title");
    announcement.setCreated(new Timestamp(START_TIMESTAMP_TEST));
    announcement.setDescription("sample description");
    myJooqMock.addReturn(OperationType.SELECT, announcement);

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
    myJooqMock.addReturn(OperationType.SELECT, event);

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
    myJooqMock.addReturn(OperationType.SELECT, announcements);

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

  // test deleting an announcement properly
  @Test
  public void testDeleteAnnouncement() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    int deletedAnnouncementId = 42;
    AnnouncementsRecord announcementToDelete =
        myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
    announcementToDelete.setId(deletedAnnouncementId);
    announcementToDelete.setTitle("sample title");
    announcementToDelete.setDescription("sample description");
    myJooqMock.addReturn(OperationType.DELETE, announcementToDelete);

    myAnnouncementsProcessorImpl.deleteAnnouncement(deletedAnnouncementId, myUserData);

    Object[] deleteBindings = myJooqMock.getSqlBindings(OperationType.DELETE).get(0);

    assertEquals(deletedAnnouncementId, deleteBindings[0]);
  }

  @Test
  public void testDeleteNonexistentAnnouncement() {
    JWTData myUserData = new JWTData(0, PrivilegeLevel.ADMIN);

    int deletedAnnouncementId = 42;
    myJooqMock.addEmptyReturn(OperationType.DELETE);

    myAnnouncementsProcessorImpl.deleteAnnouncement(deletedAnnouncementId, myUserData);

    assertEquals(1, myJooqMock.timesCalled(OperationType.DELETE));
    Object[] deleteBindings = myJooqMock.getSqlBindings(OperationType.DELETE).get(0);

    assertEquals(deletedAnnouncementId, deleteBindings[0]);
  }
}
