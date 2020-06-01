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
    String fieldName = fieldPrefix + "line_item_request.";
    List<String> fields = new ArrayList<>();
    if (eventId == null || eventId < 1) {
      fields.add(fieldName + "event_id");
    }
    if (quantity == null || quantity < 1) {
      fields.add(fieldName + "quantity");
    }
    return fields;
  }
}
