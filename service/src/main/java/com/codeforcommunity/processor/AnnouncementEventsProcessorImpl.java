package com.codeforcommunity.processor;

import com.codeforcommunity.api.IAnnouncementEventsProcessor;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.dataaccess.AnnouncementEventsDatabaseOperations;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;
import com.codeforcommunity.dto.announcement_event.Announcement;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsRequest;
import java.util.List;
import org.jooq.DSLContext;

public class AnnouncementEventsProcessorImpl implements IAnnouncementEventsProcessor {

  private final AnnouncementEventsDatabaseOperations announcementEventsDatabaseOperations;

  public AnnouncementEventsProcessorImpl(DSLContext db) {
    this.announcementEventsDatabaseOperations = new AnnouncementEventsDatabaseOperations(db);
  }

  @Override
  public GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request) {
    List<Announcement> announcements = announcementEventsDatabaseOperations
        .getAnnouncements(request.getStartDate(), request.getEndDate(), request.getCount());

    return new GetAnnouncementsResponse(announcements.size(), announcements);
  }

  @Override
  public void postAnnouncements(PostAnnouncementsRequest request) {
//    announcementEventsDatabaseOperations.createNewUser(request.getEmail(), request.getPassword(),
//        request.getFirstName(), request.getLastName());
  }
}
