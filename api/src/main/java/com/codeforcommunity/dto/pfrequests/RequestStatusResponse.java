package com.codeforcommunity.dto.pfrequests;

import java.util.List;

public class RequestStatusResponse {
  private List<RequestStatusData> requests;

  public RequestStatusResponse(List<RequestStatusData> requests) {
    this.requests = requests;
  }

  public List<RequestStatusData> getRequests() {
    return requests;
  }
}
