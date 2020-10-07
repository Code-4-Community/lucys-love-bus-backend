package com.codeforcommunity.dto.pfrequests;

import java.util.List;

/**
 * Represents the action of getting a request response portion within participating family requests
 */
public class GetRequestsResponse {

  private List<RequestData> requests;

  /**
   * Creates a GetRequestsResponse object with a list of RequestData
   *
   * @param requests a list of RequestData
   */
  public GetRequestsResponse(List<RequestData> requests) {
    this.requests = requests;
  }

  /**
   * Gets the list of RequestData of this GetRequestsResponse
   *
   * @return the list of RequestData of this GetRequestsResponse
   */
  public List<RequestData> getRequests() {
    return requests;
  }
}
