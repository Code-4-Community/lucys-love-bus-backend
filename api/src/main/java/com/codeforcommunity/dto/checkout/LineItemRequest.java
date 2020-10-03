package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a request for a line item (set of tickets to be purchased for an event), containing
 * the data of the eventId of the event the tickets are being purchased for, and the quantity of
 * tickets being purchased.
 */
public class LineItemRequest extends ApiDto {

  private Integer eventId;
  private Integer quantity;

  public LineItemRequest() {}

  /**
   * Constructs a line item request containing the data of the eventId of the event the tickets are
   * being purchased for in this line item, and the quantity of tickets being purchased in this line
   * item.
   *
   * @param eventId eventId of the event the tickets are being purchased for in this line item
   * @param quantity quantity of tickets being purchased in this line item
   */
  public LineItemRequest(Integer eventId, Integer quantity) {
    this.eventId = eventId;
    this.quantity = quantity;
  }

  /**
   * Gets the eventID stored in this line item request object.
   *
   * @return the eventID stored in this line item request object
   */
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
