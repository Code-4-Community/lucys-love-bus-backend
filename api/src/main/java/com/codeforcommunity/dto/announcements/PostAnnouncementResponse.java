package com.codeforcommunity.dto.announcements;

import java.sql.Timestamp;

public class PostAnnouncementResponse {

  private int id;
  private String title;
  private String description;
  private Timestamp created;

  private PostAnnouncementResponse() {}

  public PostAnnouncementResponse(int id, String title, String description,
      Timestamp created) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.created = created;
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Timestamp getCreated() {
    return created;
  }

  @Override
  public String toString() {
    return "PostAnnouncementsResponse{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", created=" + created +
        '}';
  }
}
