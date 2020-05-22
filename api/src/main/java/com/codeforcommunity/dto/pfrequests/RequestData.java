package com.codeforcommunity.dto.pfrequests;

public class RequestData {
  private int id;
  private RequestUser user;

  public RequestData(int id, RequestUser user) {
    this.id = id;
    this.user = user;
  }

  public int getId() {
    return id;
  }

  public RequestUser getUser() {
    return user;
  }
}
