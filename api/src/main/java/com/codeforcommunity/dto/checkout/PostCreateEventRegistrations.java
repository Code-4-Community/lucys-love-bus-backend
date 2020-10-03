package com.codeforcommunity.dto.checkout;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the information to be sent in a post request for creating event registrations.
 * Contains a single field, lineItemRequests, which is a list of LineItemRequest objects, each of
 * which contains an eventId and number of tickets being purchased for that specific event.
 */
public class PostCreateEventRegistrations extends ApiDto {

  private List<LineItemRequest> lineItemRequests;

  public PostCreateEventRegistrations() {}

  /**
   * Constructs a PostCreateEventRegistrations object, which contains a list of LineItemRequests,
   * each of which contains the information for a ticket/set of tickets being purchased for a
   * specific event.
   *
   * @param lineItemRequests the list of LineItemRequests to be included in this create event
   *     registrations post request; each LineItemRequest contains the eventId and number of tickets
   *     being purchased
   */
  public PostCreateEventRegistrations(List<LineItemRequest> lineItemRequests) {
    this.lineItemRequests = lineItemRequests;
  }

  /**
   * Gets the line item requests object stored in this post request object, which in turn contains
   * the eventId and number of tickets being purchased in that line item.
   *
   * @return the line item requests object stored in this post request object (containing the
   *     eventId and number of tickets being purchased)
   */
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
