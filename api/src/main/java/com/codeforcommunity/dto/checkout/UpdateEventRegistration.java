package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class UpdateEventRegistration extends ApiDto {

  private Integer quantity;

  public UpdateEventRegistration() {}

  public UpdateEventRegistration(Integer quantity) {
    this.quantity = quantity;
  }

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
