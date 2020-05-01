package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import org.jooq.generated.tables.records.AnnouncementsRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jooq.generated.Tables;
import static org.mockito.Mockito.*;

import java.util.*;

import java.sql.Timestamp;

// Contains tests for AnnouncementsProcessorImpl.java in main
public class AnnouncementsProcessorImplTest {
    JooqMock myJooqMock;
    AnnouncementsProcessorImpl myAnnouncementsProcessorImpl;

    // use UNIX time for ease of testing
    private final int START_TIMESTAMP_TEST = 1587000000;
    private final int END_TIMESTAMP_TEST = 1587100000;
    private final int START_TIMESTAMP_TEST2 = 1586000000;
    private final int END_TIMESTAMP_TEST2 = 1586100000;

    // set up all the mocks
    @Before
    public void setup() {
        this.myJooqMock = new JooqMock();
        this.myAnnouncementsProcessorImpl = new AnnouncementsProcessorImpl(myJooqMock.getContext());
    }

    // test getting announcements that fails validation due to non-positive count
    @Test
    public void testGetAnnouncements1() {
        GetAnnouncementsRequest req = new GetAnnouncementsRequest(
                new Timestamp(START_TIMESTAMP_TEST),
                new Timestamp(END_TIMESTAMP_TEST),
                0);

        try {
            myAnnouncementsProcessorImpl.getAnnouncements(req);
            fail();
        } catch (MalformedParameterException e) {
            assertEquals(e.getParameterName(), "count");
        }
    }

    // test getting announcements that fails validation due to invalid start/end dates
    @Test
    public void testGetAnnouncements2() {
        GetAnnouncementsRequest req1 = new GetAnnouncementsRequest(
                new Timestamp(END_TIMESTAMP_TEST),
                new Timestamp(START_TIMESTAMP_TEST),
                1);
        GetAnnouncementsRequest req2 = new GetAnnouncementsRequest(
                new Timestamp(START_TIMESTAMP_TEST),
                new Timestamp((new Date()).getTime() + 100000),
                1);

        try {
            myAnnouncementsProcessorImpl.getAnnouncements(req1);
            fail();
        } catch (MalformedParameterException e) {
            assertEquals(e.getParameterName(), "end");
        }

        try {
            myAnnouncementsProcessorImpl.getAnnouncements(req2);
            fail();
        } catch (MalformedParameterException e) {
            assertEquals(e.getParameterName(), "end");
        }
    }

    // test getting announcements with range covering no events
    @Test
    public void testGetAnnouncements3() {
        // craft the get request
        GetAnnouncementsRequest req = new GetAnnouncementsRequest(
                new Timestamp(0),
                new Timestamp(1000),
                2);

        myJooqMock.addReturn("SELECT", new ArrayList<UpdatableRecordImpl>());
        GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

        assertEquals(res.getTotalCount(), 0);
    }

    // test getting announcements with range covering all events
    @Test
    public void testGetAnnouncements4() {
        // craft the get request
        GetAnnouncementsRequest req = new GetAnnouncementsRequest(
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

        List<UpdatableRecordImpl> announcements = new ArrayList<>();
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
    public void testGetAnnouncements5() {
        // craft the get request
        GetAnnouncementsRequest req = new GetAnnouncementsRequest(
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

        AnnouncementsRecord announcement2 = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
        announcement2.setId(1);
        announcement2.setTitle("the second announcement title");
        announcement2.setCreated(new Timestamp(START_TIMESTAMP_TEST2));
        announcement2.setDescription("the second announcement description");
        myJooqMock.addReturn("SELECT", announcement2);

        GetAnnouncementsResponse res = myAnnouncementsProcessorImpl.getAnnouncements(req);

        assertEquals(res.getTotalCount(), 1);
        assertEquals(res.getAnnouncements().get(0).getId(), 0);
    }

    // test posting an announcement without admin privileges
    @Test
    public void testPostAnnouncements1() {
        // make the request object
        PostAnnouncementRequest req = new PostAnnouncementRequest(
                "sample title",
                "sample description");

        // mock the user
        JWTData myUserData = mock(JWTData.class);
        when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

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
        PostAnnouncementRequest req = new PostAnnouncementRequest(
                "sample title",
                "sample description");

        // mock the announcement inside the DB
        AnnouncementsRecord announcement = myJooqMock.getContext().newRecord(Tables.ANNOUNCEMENTS);
        announcement.setId(0);
        announcement.setTitle("sample title");
        announcement.setCreated(new Timestamp(START_TIMESTAMP_TEST));
        announcement.setDescription("sample description");
        myJooqMock.addReturn("SELECT", announcement);
        myJooqMock.addReturn("INSERT", announcement);

        // mock the user
        JWTData myUserData = mock(JWTData.class);
        when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

        PostAnnouncementResponse res = myAnnouncementsProcessorImpl.postAnnouncement(req, myUserData);

        assertEquals(res.getAnnouncement().getDescription(), "sample description");
        assertEquals(res.getAnnouncement().getTitle(), "sample title");
        assertEquals(res.getAnnouncement().getId(), 0);
        assertEquals(res.getAnnouncement().getCreated(), new Timestamp(START_TIMESTAMP_TEST));
    }

    // TODO
    @Test
    public void testPostEventSpecificAnnouncement1() {
        fail();
    }

    // TODO
    @Test
    public void testPostEventSpecificAnnouncement2() {
        fail();
    }

    // TODO
    @Test
    public void testGetEventSpecificAnnouncements1() {
        fail();
    }

    // TODO
    @Test
    public void testGetEventSpecificAnnouncements2() {
        fail();
    }
}