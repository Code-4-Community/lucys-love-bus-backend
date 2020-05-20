package com.codeforcommunity.dto.userEvents.components;

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

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Timestamp getStart() {
    return start;
  }

  public void setStart(Timestamp start) {
    this.start = start;
  }

  public Timestamp getEnd() {
    return end;
  }

  public void setEnd(Timestamp end) {
    this.end = end;
  }
}
