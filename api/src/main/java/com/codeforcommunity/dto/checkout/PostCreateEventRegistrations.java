package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.List;

public class PostCreateEventRegistrations implements ApiDto {

  private List<LineItemRequest> lineItemRequests;

  public PostCreateEventRegistrations() {}

  public PostCreateEventRegistrations(List<LineItemRequest> lineItemRequests) {
    this.lineItemRequests = lineItemRequests;
  }

  public List<LineItemRequest> getLineItemRequests() {
    return lineItemRequests;
  }

  @Override
  public void validate() {}
}
