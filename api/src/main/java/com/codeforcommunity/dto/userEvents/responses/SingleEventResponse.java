package com.codeforcommunity.dto.userEvents.responses;

import com.codeforcommunity.dto.userEvents.components.EventDetails;

/** A class to represent the response to a single event */
public class SingleEventResponse {
  private int id;
  private String title;
  private int spotsAvailable;
  private int capacity;
  private String thumbnail;
  private EventDetails details;
  private int ticketCount;
  private boolean canRegister;
  private int price;
  private boolean forPFOnly;

  public SingleEventResponse(
      int id,
      String title,
      int spotsAvailable,
      int capacity,
      String thumbnail,
      EventDetails details,
      int ticketCount,
      boolean canRegister,
      int price,
      boolean forPFOnly) {
    this.id = id;
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.capacity = capacity;
    this.thumbnail = thumbnail;
    this.details = details;
    this.ticketCount = ticketCount;
    this.canRegister = canRegister;
    this.price = price;
    this.forPFOnly = forPFOnly;
  }

  private SingleEventResponse() {}

  /**
   * Gets the id of the event
   *
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the title of the event
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the spots available at the event.
   *
   * @return the number of available spots
   */
  public int getSpotsAvailable() {
    return spotsAvailable;
  }

  /**
   * Gets the event capacity.
   *
   * @return the capacity
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Gets the event thumbnail.
   *
   * @return the thumbnail
   */
  public String getThumbnail() {
    return thumbnail;
  }

  /**
   * Gets the event details.
   *
   * @return the details
   */
  public EventDetails getDetails() {
    return details;
  }

  /**
   * Gets the ticket count of the event.
   *
   * @return the count
   */
  public int getTicketCount() {
    return ticketCount;
  }

  /**
   * Get whether the event is open for registration.
   *
   * @return if you can register or not
   */
  public boolean isCanRegister() {
    return canRegister;
  }

  /**
   * Gets the price of the event.
   *
   * @return the price s
   */
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
