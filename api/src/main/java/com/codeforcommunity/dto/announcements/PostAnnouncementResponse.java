package com.codeforcommunity.dto.announcements;

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
