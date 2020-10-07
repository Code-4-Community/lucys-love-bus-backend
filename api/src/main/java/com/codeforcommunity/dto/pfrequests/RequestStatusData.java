package com.codeforcommunity.dto.pfrequests;

import com.codeforcommunity.enums.RequestStatus;
import java.sql.Timestamp;

/** Represents the status data request portion within the participating family requests */
public class RequestStatusData {

  private int id;
  private RequestStatus status;
  private Timestamp created;

  /**
   * Creates a RequestStatusData which includes data on a status request
   *
   * @param id The ID of the RequestStatusData
   * @param status The RequestStatus of this RequestStatusData
   * @param created The Timestamp of this RequestStatusData
   */
  public RequestStatusData(int id, RequestStatus status, Timestamp created) {
    this.id = id;
    this.status = status;
    this.created = created;
  }

  /**
   * Gets the ID of this RequestStatusData
   *
   * @return the ID of this RequestStatusData
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the status of this RequestStatusData
   *
   * @return the RequestStatus of this RequestStatusData
   */
  public RequestStatus getStatus() {
    return status;
  }

  /**
   * Gets the time stamp of this RequestStatusData
   *
   * @return the Timestamp of this RequestStatusData
   */
  public Timestamp getCreated() {
    return created;
  }
}
