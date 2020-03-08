package com.codeforcommunity.dto.pfrequests;

import java.util.List;

public class GetRequestsResponse {
  private List<RequestData> requests;

  public GetRequestsResponse(List<RequestData> requests) {
    this.requests = requests;
  }

  public List<RequestData> getRequests() {
    return requests;
  }
}
