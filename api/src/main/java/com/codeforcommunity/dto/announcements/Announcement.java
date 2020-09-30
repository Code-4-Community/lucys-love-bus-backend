package com.codeforcommunity.dto.announcements;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;

/**
 * Represents the data about an announcement, including announcement's id, title, description, id of
 * relevant event, and timestamp of when the announcement was created.
 */
public class Announcement {

  private int id;
  private String title;
  private String description;
  private Integer eventId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
  private Timestamp created;

  private Announcement() {}

  /**
   * Constructs an announcement with the given data.
   *
   * @param id id of the announcement being created
   * @param title title of the announcement
   * @param description description of the announcement
   * @param created timestamp of the announcement created in format yyyy-MM-dd HH:mm:ss Z
   * @param eventId id of the event this announcement is relevant to
   */
  public Announcement(
      int id, String title, String description, Timestamp created, Integer eventId) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.created = created;
    this.eventId = eventId;
  }

  /**
   * Gets the id of this announcement.
   *
   * @return id of this announcement
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the title of this announcement.
   *
   * @return title of this announcement
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the description of this announcement.
   *
   * @return description of this announcement
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the timestamp of when this announcement was created.
   *
   * @return timestamp of when this announcement was created
   */
  public Timestamp getCreated() {
    return created;
  }

  /**
   * Gets the id of the event this announcement is relevant to.
   *
   * @return id of the event this announcement is relevant to
   */
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
