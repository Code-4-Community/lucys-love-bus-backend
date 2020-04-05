package com.codeforcommunity.dto.announcements;

public class PostAnnouncementRequest {
  private String title;
  private String description;

  private PostAnnouncementRequest() {}

  public PostAnnouncementRequest(String title, String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }
}
