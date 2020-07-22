package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;
import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.Announcement;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.requester.Emailer;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.generated.tables.pojos.Announcements;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.AnnouncementsRecord;

public class AnnouncementsProcessorImpl implements IAnnouncementsProcessor {

  private final DSLContext db;
  private final Emailer emailer;

  public AnnouncementsProcessorImpl(DSLContext db, Emailer emailer) {
    this.db = db;
    this.emailer = emailer;
  }

  @Override
  public GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request) {
    int count = request.getCount();
    Timestamp start = request.getStartDate();
    Timestamp end = request.getEndDate();

    List<Announcements> announcements =
        db.selectFrom(ANNOUNCEMENTS)
            .where(ANNOUNCEMENTS.CREATED.between(start, end))
            .and(ANNOUNCEMENTS.EVENT_ID.isNull())
            .orderBy(ANNOUNCEMENTS.CREATED.desc())
            .fetchInto(Announcements.class);

    if (count < announcements.size()) {
      announcements = announcements.subList(0, count);
    }
    return new GetAnnouncementsResponse(
        announcements.size(),
        announcements.stream().map(this::convertAnnouncementObject).collect(Collectors.toList()));
  }

  /**
   * Converts a jOOQ POJO announcement into the announcement class defined in the API package.
   *
   * @param announcement the jOOQ POJO announcement
   * @return an object of type Announcement
   */
  private Announcement convertAnnouncementObject(Announcements announcement) {
    return new Announcement(
        announcement.getId(),
        announcement.getTitle(),
        announcement.getDescription(),
        announcement.getCreated(),
        announcement.getEventId());
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
  public GetAnnouncementsResponse getEventSpecificAnnouncements(
      GetEventSpecificAnnouncementsRequest request) {
    int eventId = request.getEventId();
    validateEventId(eventId);

    List<Announcements> announcements =
        db.selectFrom(ANNOUNCEMENTS)
            .where(ANNOUNCEMENTS.EVENT_ID.eq(eventId))
            .orderBy(ANNOUNCEMENTS.CREATED.desc())
            .fetchInto(Announcements.class);

    return new GetAnnouncementsResponse(
        announcements.size(),
        announcements.stream().map(this::convertAnnouncementObject).collect(Collectors.toList()));
  }

  @Override
  public void deleteAnnouncement(
      int announcementId, JWTData userData
  ) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    db.delete(ANNOUNCEMENTS).where(ANNOUNCEMENTS.ID.eq(announcementId)).execute();
  }

  private PostAnnouncementResponse announcementPojoToResponse(Announcements announcements) {
    return new PostAnnouncementResponse(convertAnnouncementObject(announcements));
  }

  private AnnouncementsRecord announcementRequestToRecord(PostAnnouncementRequest request) {
    AnnouncementsRecord newRecord = db.newRecord(ANNOUNCEMENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDescription());
    return newRecord;
  }

  private void validateEventId(int eventId) {
    List<Events> matchingEvents =
        db.selectFrom(EVENTS).where(EVENTS.ID.eq(eventId)).fetchInto(Events.class);
    if (matchingEvents.size() == 0) {
      throw new MalformedParameterException("event_id");
    }
  }
}
