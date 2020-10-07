package com.codeforcommunity.dto.pfrequests;

import java.util.List;

/** Represents the status response portion within the participating family requests */
public class RequestStatusResponse {

  private List<RequestStatusData> requests;

  /**
   * Creates a RequestStatusResponse with the list of RequestStatusData of the participating family
   *
   * @param requests
   */
  public RequestStatusResponse(List<RequestStatusData> requests) {
    this.requests = requests;
  }

  /**
   * Gets the list of status data requests of the participating family
   *
   * @return the list of RequestStatusData
   */
  public List<RequestStatusData> getRequests() {
    return requests;
  }
}
