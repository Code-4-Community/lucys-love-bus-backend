package com.codeforcommunity.dto.announcements;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;

public class Announcement {

  private int id;
  private String title;
  private String description;
  private Integer eventId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
  private Timestamp created;

  private Announcement() {}

  public Announcement(
      int id, String title, String description, Timestamp created, Integer eventId) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.created = created;
    this.eventId = eventId;
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

  public Integer getEventId() {
    return eventId;
  }

  @Override
  public String toString() {
    return "Announcement{"
        + "id="
        + id
        + ", title='"
        + title
        + '\''
        + ", description='"
        + description
        + '\''
        + ", eventId="
        + eventId
        + ", created="
        + created
        + '}';
  }
}
