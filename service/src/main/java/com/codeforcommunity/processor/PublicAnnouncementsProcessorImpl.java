package com.codeforcommunity.processor;

import com.codeforcommunity.api.IPublicAnnouncementsProcessor;
import com.codeforcommunity.dto.announcements.Announcement;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.requester.Emailer;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Announcements;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;

public class PublicAnnouncementsProcessorImpl implements IPublicAnnouncementsProcessor {

  private final DSLContext db;
  private final Emailer emailer;

  public PublicAnnouncementsProcessorImpl(DSLContext db, Emailer emailer) {
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
}
