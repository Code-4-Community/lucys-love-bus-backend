package com.codeforcommunity.dto.userEvents.responses;

import com.codeforcommunity.dto.userEvents.components.EventDetails;

public class PublicSingleEventResponse {
  private int id;
  private String title;
  private int spotsAvailable;
  private int capacity;
  private String thumbnail;
  private EventDetails details;
  private int price;
  private boolean forPFOnly;

  public PublicSingleEventResponse(
      int id,
      String title,
      int spotsAvailable,
      int capacity,
      String thumbnail,
      EventDetails details,
      int price,
      boolean forPFOnly) {
    this.id = id;
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.capacity = capacity;
    this.thumbnail = thumbnail;
    this.details = details;
    this.price = price;
    this.forPFOnly = forPFOnly;
  }

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

  public int getPrice() {
    return price;
  }

  /**
   * Gets whether the event is intended for participating families only
   *
   * @return whether the event is intended for participating families only
   */
  public boolean getForPFOnly() {
    return forPFOnly;
  }
}
