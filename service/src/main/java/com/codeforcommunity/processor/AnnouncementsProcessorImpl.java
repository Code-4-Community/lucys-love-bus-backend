package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;
import static org.jooq.generated.Tables.EVENTS;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.dto.announcements.Announcement;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.requester.Emailer;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Announcements;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.AnnouncementsRecord;

public class AnnouncementsProcessorImpl implements IAnnouncementsProcessor {

  protected DSLContext db;
  protected final Emailer emailer;

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

  protected PostAnnouncementResponse announcementPojoToResponse(Announcements announcements) {
    return new PostAnnouncementResponse(convertAnnouncementObject(announcements));
  }

  protected AnnouncementsRecord announcementRequestToRecord(PostAnnouncementRequest request) {
    AnnouncementsRecord newRecord = db.newRecord(ANNOUNCEMENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDescription());
    return newRecord;
  }

  protected void validateEventId(int eventId) {
    List<Events> matchingEvents =
        db.selectFrom(EVENTS).where(EVENTS.ID.eq(eventId)).fetchInto(Events.class);
    if (matchingEvents.size() == 0) {
      throw new MalformedParameterException("event_id");
    }
  }
}
