package com.codeforcommunity.dto.pfrequests;

public class RequestData {
  private int id;
  private String description;
  private String userEmail;

  public RequestData(int id, String description, String userEmail) {
    this.id = id;
    this.description = description;
    this.userEmail = userEmail;
  }

  public int getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getUserEmail() {
    return userEmail;
  }
}
