package com.codeforcommunity.dto.checkout;

import java.util.List;

public class PostCreateEventRegistrations {

  private List<LineItemRequest> lineItems;

  public PostCreateEventRegistrations() {}

  public PostCreateEventRegistrations(List<LineItemRequest> lineItems) {
    this.lineItems = lineItems;
  }

  public List<LineItemRequest> getLineItems() {
    return lineItems;
  }
}
