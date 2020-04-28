package com.codeforcommunity.dto.userEvents.components;

public class Event {
  private int id;
  private String title;
  private int spotsAvailable;
  private String thumbnail;
  private EventDetails details;

  public Event(int id, String title, int spotsAvailable, String thumbnail, EventDetails details) {
    this.id = id;
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getSpotsAvailable() {
    return spotsAvailable;
  }

  public void setSpotsAvailable(int spotsAvailable) {
    this.spotsAvailable = spotsAvailable;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public EventDetails getDetails() {
    return details;
  }

  public void setDetails(EventDetails details) {
    this.details = details;
  }

}

