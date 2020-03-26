package com.codeforcommunity.dto.pfrequests;

import com.codeforcommunity.enums.RequestStatus;

public class RequestStatusResponse {
  private RequestStatus status;

  public RequestStatusResponse(RequestStatus status) {
    this.status = status;
  }

  public RequestStatus getStatus() {
    return status;
  }
}
