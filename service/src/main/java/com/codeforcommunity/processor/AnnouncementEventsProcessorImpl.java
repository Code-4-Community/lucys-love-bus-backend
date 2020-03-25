package com.codeforcommunity.processor;

import com.codeforcommunity.api.IAnnouncementEventsProcessor;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.dataaccess.AnnouncementEventsDatabaseOperations;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsRequest;
import org.jooq.DSLContext;

public class AnnouncementEventsProcessorImpl implements IAnnouncementEventsProcessor {

  private final AnnouncementEventsDatabaseOperations announcementEventsDatabaseOperations;

  public AnnouncementEventsProcessorImpl(DSLContext db) {
    this.announcementEventsDatabaseOperations = new AnnouncementEventsDatabaseOperations(db);
  }

  @Override
  public GetAnnouncementsResponse getAllAnnouncements(GetAnnouncementsRequest request) {
    return null;
  }

  @Override
  public void postAllAnnouncements(PostAnnouncementsRequest request) {

  }
}
