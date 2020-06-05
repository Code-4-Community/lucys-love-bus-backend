package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class PostCreateEventRegistrations extends ApiDto {

  private List<LineItemRequest> lineItemRequests;

  public PostCreateEventRegistrations() {}

  public PostCreateEventRegistrations(List<LineItemRequest> lineItemRequests) {
    this.lineItemRequests = lineItemRequests;
  }

  public List<LineItemRequest> getLineItemRequests() {
    return lineItemRequests;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "post_create_event_registrations.";
    List<String> fields = new ArrayList<>();
    if (lineItemRequests == null) {
      fields.add(fieldName + "line_item_requests");
    } else {
      for (LineItemRequest req : lineItemRequests) {
        fields.addAll(req.validateFields(fieldName));
      }
    }
    return fields;
  }
}
