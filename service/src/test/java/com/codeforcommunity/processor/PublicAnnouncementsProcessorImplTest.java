package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.requester.Emailer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.AnnouncementsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PublicAnnouncementsProcessorImplTest {

  private JooqMock myJooqMock;
  private PublicAnnouncementsProcessorImpl myPublicAnnouncementsProcessorImpl;
  private Emailer mockEmailer;

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
    this.mockEmailer = mock(Emailer.class);
    this.myPublicAnnouncementsProcessorImpl =
        new PublicAnnouncementsProcessorImpl(myJooqMock.getContext(), this.mockEmailer);
  }

  // test getting announcements with range covering no events
  @Test
  public void testGetAnnouncements1() {
    // craft the get request
    GetAnnouncementsRequest req =
        new GetAnnouncementsRequest(new Timestamp(0), new Timestamp(1000), 2);

    myJooqMock.addEmptyReturn("SELECT");
    GetAnnouncementsResponse res = myPublicAnnouncementsProcessorImpl.getAnnouncements(req);

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

    GetAnnouncementsResponse res = myPublicAnnouncementsProcessorImpl.getAnnouncements(req);

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

    GetAnnouncementsResponse res = myPublicAnnouncementsProcessorImpl.getAnnouncements(req);

    assertEquals(res.getTotalCount(), 1);
    assertEquals(res.getAnnouncements().get(0).getId(), 0);
  }
}
