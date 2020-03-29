package com.codeforcommunity.dto.announcement_event;

public class PostAnnouncementsRequest {
  private String title;
  private String description;

  private PostAnnouncementsRequest() {}

  public PostAnnouncementsRequest(String title, String description) {
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
