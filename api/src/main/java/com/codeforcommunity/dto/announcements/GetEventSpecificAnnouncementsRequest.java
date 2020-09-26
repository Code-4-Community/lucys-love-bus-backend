package com.codeforcommunity.dto.announcements;

/**
 * Represents an object containing the information for a get event-specific announcements request,
 * which is just the id of the event for which the announcements are being requested.
 */
public class GetEventSpecificAnnouncementsRequest {

  private Integer eventId;

  private GetEventSpecificAnnouncementsRequest() {}

  /**
   * Constructs a GetEventSpecificAnnouncementsRequest object, with the eventId of the relevant
   * event for which the announcements are being requested.
   * @param eventId id of the event for which the announcements are being requested
   */
  public GetEventSpecificAnnouncementsRequest(Integer eventId) {
    this.eventId = eventId;
  }

  /**
   * Gets the eventId being stored in this request object.
   * @return the eventId being stored in this request object
   */
  public Integer getEventId() {
    return eventId;
  }
}
