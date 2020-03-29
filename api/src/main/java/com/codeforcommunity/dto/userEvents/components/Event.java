package com.codeforcommunity.dto.userEvents.components;

import java.net.URL;

public class Event {
  private int id;
  private String title;
  private int spotsAvailable;
  private URL thumbnail;
  private EventDetails details;

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

  public URL getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(URL thumbnail) {
    this.thumbnail = thumbnail;
  }

  public EventDetails getDetails() {
    return details;
  }

  public void setDetails(EventDetails details) {
    this.details = details;
  }

  public Event(int id, String title, int spotsAvailable, URL thumbnail, EventDetails details) {
    this.id = id;
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
  }
}

