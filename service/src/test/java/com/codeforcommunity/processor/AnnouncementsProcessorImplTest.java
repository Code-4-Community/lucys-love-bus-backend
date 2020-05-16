package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.MalformedParameterException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

import org.jooq.generated.tables.records.AnnouncementsRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.generated.Tables;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Contains tests for AnnouncementsProcessorImpl.java in main
public class AnnouncementsProcessorImplTest {
    JooqMock myJooqMock;
    AnnouncementsProcessorImpl myAnnouncementsProcessorImpl;

    // use UNIX time for ease of testing
    // 04/16/2020 @ 1:20am (UTC)
    private final int START_TIMESTAMP_TEST = 1587000000;
    // 04/17/2020 @ 5:06am (UTC)
    private final int END_TIMESTAMP_TEST = 1587100000;
    // 04/04/2020 @ 11:33am (UTC)
    private final int START_TIMESTAMP_TEST2 = 1586000000;

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

        myJooqMock.addEmptyReturn("SELECT");
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

    // posting an event specific announcement fails if user isn't an admin
    @Test
    public void testPostEventSpecificAnnouncement1() {
        PostAnnouncementRequest req = new PostAnnouncementRequest("c4c", "code for community");

        // mock the user
        JWTData myUserData = mock(JWTData.class);
        when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

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
        JWTData myUserData = mock(JWTData.class);
        when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

        // return no events
        myJooqMock.addReturn("SELECT", new ArrayList<EventsRecord>());

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
        JWTData myUserData = mock(JWTData.class);
        when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

        // mock the specific event inside the DB
        EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
        event.setId(1);
        myJooqMock.addReturn("SELECT", event);

        // mock the announcement inside the DB
        AnnouncementsRecord announcement = new AnnouncementsRecord();
        announcement.setId(1);
        announcement.setEventId(1);
        announcement.setTitle("c4c");
        announcement.setDescription("code for community");
        myJooqMock.addReturn("SELECT", announcement);
        myJooqMock.addReturn("INSERT", announcement);

        PostAnnouncementResponse res = myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
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
        JWTData myUserData = mock(JWTData.class);
        when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

        // mock the specific event inside the DB
        EventsRecord event = myJooqMock.getContext().newRecord(Tables.EVENTS);
        event.setId(1);
        myJooqMock.addReturn("SELECT", event);

        // mock the announcements inside the DB
        AnnouncementsRecord announcement1 = new AnnouncementsRecord();
        announcement1.setId(1);
        announcement1.setEventId(1);
        announcement1.setTitle("c4c");
        announcement1.setDescription("code for community");

        AnnouncementsRecord announcement2 = new AnnouncementsRecord();
        announcement2.setId(2);
        announcement2.setEventId(1);
        announcement2.setTitle("LLB");
        announcement2.setDescription("Lucy's Love Bus");

        List<AnnouncementsRecord> announcements = new ArrayList<>();
        announcements.add(announcement1);
        announcements.add(announcement2);
        myJooqMock.addReturn("SELECT", announcements);
        myJooqMock.addReturn("INSERT", announcements);

        PostAnnouncementResponse res = myAnnouncementsProcessorImpl.postEventSpecificAnnouncement(req, myUserData, 1);
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

    // getting an event specific announcement succeeds with event with multiple announcements in database
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