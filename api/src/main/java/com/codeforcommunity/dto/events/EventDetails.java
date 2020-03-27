package com.codeforcommunity.dto.events;

import java.sql.Timestamp;

public class EventDetails {
  private String description;
  private String location;
  private Timestamp start;
  private Timestamp end;

  public EventDetails(String description, String location, Timestamp start, Timestamp end) {
    this.description = description;
    this.location = location;
    this.start = start;
    this.end = end;
  }

  private EventDetails() {}

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public Timestamp getStart() {
    return start;
  }

  public Timestamp getEnd() {
    return end;
  }
}
