package com.codeforcommunity.dto.userEvents.components;

public class EventCheckout {
  private int eventId;
  private int tickets;

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public int getTickets() {
    return tickets;
  }

  public void setTickets(int tickets) {
    this.tickets = tickets;
  }

  public EventCheckout(int eventId, int tickets) {
    this.eventId = eventId;
    this.tickets = tickets;
  }
}
