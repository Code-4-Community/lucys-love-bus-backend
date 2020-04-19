package com.codeforcommunity.dto.announcements;

public class PostEventSpecificAnnouncementResponse {

  private Announcement announcement;

  private PostEventSpecificAnnouncementResponse() {}

  public PostEventSpecificAnnouncementResponse(
      Announcement announcement) {
    this.announcement = announcement;
  }

  public Announcement getAnnouncement() {
    return announcement;
  }
}
