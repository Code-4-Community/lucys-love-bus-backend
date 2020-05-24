package com.codeforcommunity.dto.checkout;

public class LineItemRequest {

  private Integer eventId;
  private Integer quantity;

  public LineItemRequest() {}

  public LineItemRequest(Integer eventId, Integer quantity) {
    this.eventId = eventId;
    this.quantity = quantity;
  }

  public Integer getEventId() {
    return eventId;
  }

  public Integer getQuantity() {
    return quantity;
  }
}
