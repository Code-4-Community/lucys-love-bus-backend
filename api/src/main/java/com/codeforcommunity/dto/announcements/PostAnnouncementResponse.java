package com.codeforcommunity.dto.announcements;

import java.sql.Timestamp;

public class PostAnnouncementResponse {

  private Announcement announcement;

  private PostAnnouncementResponse() {}

  public PostAnnouncementResponse(Announcement announcement) {
    this.announcement = announcement;
  }

  public Announcement getAnnouncement() {
    return announcement;
  }
}
