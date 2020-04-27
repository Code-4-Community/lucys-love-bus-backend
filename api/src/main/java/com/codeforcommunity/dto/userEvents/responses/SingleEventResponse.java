package com.codeforcommunity.dto.userEvents.responses;

import com.codeforcommunity.dto.userEvents.components.EventDetails;

public class SingleEventResponse {
  private int id;
  private String title;
  private int spotsAvailable;
  private int capacity;
  private String thumbnail;
  private EventDetails details;

  public SingleEventResponse(int id, String title, int spotsAvailable, int capacity, String thumbnail, EventDetails details) {
    this.id = id;
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.capacity = capacity;
    this.thumbnail = thumbnail;
    this.details = details;
  }

  private SingleEventResponse() {}

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public int getSpotsAvailable() {
    return spotsAvailable;
  }

  public int getCapacity() {
    return capacity;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public EventDetails getDetails() {
    return details;
  }
}
