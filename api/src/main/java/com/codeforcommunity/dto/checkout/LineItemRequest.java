package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class LineItemRequest extends ApiDto {

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

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (eventId == null || eventId < 1) {
      fields.add(fieldPrefix + "event_id");
    }
    if (quantity == null || quantity < 1) {
      fields.add(fieldPrefix + "quantity");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "line_item_request.";
  }
}
