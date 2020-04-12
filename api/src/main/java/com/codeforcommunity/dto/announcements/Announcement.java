package com.codeforcommunity.dto.announcements;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;

public class Announcement {

  private int id;
  private String title;
  private String description;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
  private Timestamp created;

  private Announcement() {}

  public Announcement(int id, String title, String description, Timestamp created) {
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
    return "Announcement{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", created=" + created +
        '}';
  }
}
