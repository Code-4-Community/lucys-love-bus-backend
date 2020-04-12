package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.Announcement;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Announcements;
import org.jooq.generated.tables.records.AnnouncementsRecord;

public class AnnouncementsProcessorImpl implements IAnnouncementsProcessor {

  private final DSLContext db;

  public AnnouncementsProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request) {
    request.validate();

    int count = request.getCount();
    Timestamp start = request.getStartDate();
    Timestamp end = request.getEndDate();

    List<Announcements> announcements = db.selectFrom(ANNOUNCEMENTS)
        .where(ANNOUNCEMENTS.CREATED.between(start, end))
        .orderBy(ANNOUNCEMENTS.CREATED.desc())
        .fetchInto(Announcements.class);

    if (count < announcements.size()) {
      announcements = announcements.subList(0, count);
    }
    return new GetAnnouncementsResponse(announcements.size(),
        announcements.stream()
            .map(this::convertAnnouncementObject)
            .collect(Collectors.toList()));
  }

  /**
   * Converts a jOOQ POJO announcement into the announcement class defined in the API package.
   *
   * @param announcement the jOOQ POJO announcement
   * @return an object of type Announcement
   */
  private Announcement convertAnnouncementObject(Announcements announcement) {
    return new Announcement(announcement.getId(),
        announcement.getTitle(),
        announcement.getDescription(),
        announcement.getCreated());
  }

  @Override
  public PostAnnouncementResponse postAnnouncements(PostAnnouncementRequest request, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    AnnouncementsRecord newAnnouncementsRecord = announcementRequestToRecord(request);
    newAnnouncementsRecord.store();
    return announcementPojoToResponse(
        newAnnouncementsRecord.into(Announcements.class));
  }

  private PostAnnouncementResponse announcementPojoToResponse(Announcements announcements) {
    return new PostAnnouncementResponse(announcements.getId(),
        announcements.getTitle(),
        announcements.getDescription(),
        announcements.getCreated());
  }

  private AnnouncementsRecord announcementRequestToRecord(PostAnnouncementRequest request) {
    AnnouncementsRecord newRecord = db.newRecord(ANNOUNCEMENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDescription());
    return newRecord;
  }
}
