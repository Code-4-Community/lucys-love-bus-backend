package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProtectedAnnouncementsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.requester.Emailer;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.generated.tables.pojos.Announcements;
import org.jooq.generated.tables.records.AnnouncementsRecord;

import java.util.List;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;
import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;

public class ProtectedAnnouncementsProcessorImpl extends AnnouncementsProcessorImpl implements IProtectedAnnouncementsProcessor {

    public ProtectedAnnouncementsProcessorImpl(DSLContext db, Emailer emailer) {
        super(db, emailer);
    }

    @Override
    public PostAnnouncementResponse postAnnouncement(
            PostAnnouncementRequest request, JWTData userData) {
        if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
            throw new AdminOnlyRouteException();
        }
        AnnouncementsRecord newAnnouncementsRecord = announcementRequestToRecord(request);
        newAnnouncementsRecord.store();
        // the timestamp wasn't showing correctly, so just
        // get the announcement directly from the database
        return announcementPojoToResponse(
                db.selectFrom(ANNOUNCEMENTS)
                        .where(ANNOUNCEMENTS.ID.eq(newAnnouncementsRecord.getId()))
                        .fetchInto(Announcements.class)
                        .get(0));
    }

    @Override
    public PostAnnouncementResponse postEventSpecificAnnouncement(
            PostAnnouncementRequest request, JWTData userData, int eventId) {
        if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
            throw new AdminOnlyRouteException();
        }
        validateEventId(eventId);
        AnnouncementsRecord newAnnouncementsRecord = announcementRequestToRecord(request);
        newAnnouncementsRecord.setEventId(eventId);
        newAnnouncementsRecord.store();

        // Send event specific announcement email
        List<Record3<String, String, String>> receivers =
                db.select(CONTACTS.EMAIL, CONTACTS.FIRST_NAME, CONTACTS.LAST_NAME)
                        .from(
                                EVENT_REGISTRATIONS
                                        .join(CONTACTS)
                                        .on(EVENT_REGISTRATIONS.USER_ID.eq(CONTACTS.USER_ID)))
                        .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
                        .and(CONTACTS.SHOULD_SEND_EMAILS.isTrue())
                        .fetch();
        String eventName = db.selectFrom(EVENTS).where(EVENTS.ID.eq(eventId)).fetchOne(EVENTS.TITLE);
        receivers.forEach(
                record -> {
                    String email = record.component1();
                    String name = String.format("%s %s", record.component2(), record.component3());
                    emailer.sendEventSpecificAnnouncement(
                            email, name, eventName, request.getTitle(), request.getDescription());
                });

        return announcementPojoToResponse(newAnnouncementsRecord.into(Announcements.class));
    }

    @Override
    public void deleteAnnouncement(int announcementId, JWTData userData) {
        if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
            throw new AdminOnlyRouteException();
        }
        db.delete(ANNOUNCEMENTS).where(ANNOUNCEMENTS.ID.eq(announcementId)).execute();
    }
}
