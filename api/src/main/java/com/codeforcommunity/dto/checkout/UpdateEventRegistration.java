package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/** Stores a single quantity value for which to updating an event registration. */
public class UpdateEventRegistration extends ApiDto {

  private Integer quantity;

  public UpdateEventRegistration() {}

  /**
   * Constructs an UpdateEventRegistration object containing a single given value of the quantity
   * for which to update the event registration.
   *
   * @param quantity
   */
  public UpdateEventRegistration(Integer quantity) {
    this.quantity = quantity;
  }

  /**
   * Gets the quantity stored in this UpdateEventRegistration object.
   *
   * @return quantity stored in this UpdateEventRegistration object
   */
  public Integer getQuantity() {
    return quantity;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "update_event_registration.";
    List<String> fields = new ArrayList<>();
    if (quantity == null || quantity < 0) {
      fields.add(fieldName + "quantity");
    }
    return fields;
  }
}
