package com.codeforcommunity.dto.announcement_event;

import java.sql.Timestamp;

public class Announcement {

  private int id;
  private String title;
  private String description;
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
}