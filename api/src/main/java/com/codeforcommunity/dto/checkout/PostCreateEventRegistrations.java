package com.codeforcommunity.dto.checkout;

import java.util.List;

public class PostCreateEventRegistrations {

  private List<LineItemRequest> lineItemRequests;

  public PostCreateEventRegistrations() {}

  public PostCreateEventRegistrations(
      List<LineItemRequest> lineItemRequests) {
    this.lineItemRequests = lineItemRequests;
  }

  public List<LineItemRequest> getLineItemRequests() {
    return lineItemRequests;
  }
}
