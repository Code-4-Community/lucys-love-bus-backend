package com.codeforcommunity.dto.pfrequests;

/** Represents the request data portion of the participating family requests */
public class RequestData {

  private int id;
  private RequestUser user;

  /**
   * Creates a RequestData object represented with an ID and a user
   *
   * @param id The ID of the RequestData object
   * @param user The user associated with this RequestData
   */
  public RequestData(int id, RequestUser user) {
    this.id = id;
    this.user = user;
  }

  /**
   * Gets the ID of this RequestData
   *
   * @return the ID of this RequestData
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the user associated with this RequestData
   *
   * @return the RequestUser of this RequestData
   */
  public RequestUser getUser() {
    return user;
  }
}
