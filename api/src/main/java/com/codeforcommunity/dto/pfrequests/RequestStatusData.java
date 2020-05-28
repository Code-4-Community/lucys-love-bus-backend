package com.codeforcommunity.dto.pfrequests;

import com.codeforcommunity.enums.RequestStatus;
import java.sql.Timestamp;

public class RequestStatusData {

  private int id;
  private RequestStatus status;
  private Timestamp created;

  public RequestStatusData(int id, RequestStatus status, Timestamp created) {
    this.id = id;
    this.status = status;
    this.created = created;
  }

  public int getId() {
    return id;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public Timestamp getCreated() {
    return created;
  }
}
