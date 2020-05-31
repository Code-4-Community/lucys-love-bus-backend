package com.codeforcommunity.dto.checkout;

public class UpdateEventRegistration {

  private Integer quantity;

  public UpdateEventRegistration() {}

  public UpdateEventRegistration(Integer quantity) {
    this.quantity = quantity;
  }

  public Integer getQuantity() {
    return quantity;
  }
}
