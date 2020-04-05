package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;

import com.codeforcommunity.api.IAnnouncementEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcement_event.Announcement;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Announcements;
import org.jooq.generated.tables.records.AnnouncementsRecord;

public class AnnouncementEventsProcessorImpl implements IAnnouncementEventsProcessor {

  private final DSLContext db;

  public AnnouncementEventsProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request) {
    int count = request.getCount();
    Timestamp start = request.getStartDate();
    Timestamp end = request.getEndDate();
    if (count < 1) {
      throw new MalformedParameterException("count");
    }
    if (end.before(start) || end.after(new Date())) {
      throw new MalformedParameterException("end");
    }
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

  private Announcement convertAnnouncementObject(Announcements announcement) {
    return new Announcement(announcement.getId(),
        announcement.getTitle(),
        announcement.getDescription(),
        announcement.getCreated());
  }

  @Override
  public PostAnnouncementsResponse postAnnouncements(PostAnnouncementsRequest request, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    announcementRequestToRecord(request);
    AnnouncementsRecord newAnnouncementsRecord = announcementRequestToRecord(request);
    System.out.println("AnnouncementsRecord: " + newAnnouncementsRecord);
    newAnnouncementsRecord.store();
    System.out.println("Updated AnnouncementsRecord: " + newAnnouncementsRecord);
    PostAnnouncementsResponse response = announcementPojoToResponse(
        newAnnouncementsRecord.into(Announcements.class));
    System.out.println("response: " + response);
    return response;
  }

  private PostAnnouncementsResponse announcementPojoToResponse(Announcements announcements) {
    return new PostAnnouncementsResponse(announcements.getId(),
        announcements.getTitle(),
        announcements.getDescription(),
        announcements.getCreated());
  }

  private AnnouncementsRecord announcementRequestToRecord(PostAnnouncementsRequest request) {
    AnnouncementsRecord newRecord = db.newRecord(ANNOUNCEMENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDescription());
    return newRecord;
  }
}
