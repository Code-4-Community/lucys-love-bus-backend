package com.codeforcommunity.dto.pfrequests;

public class CreateRequest {
  private String description;

  public CreateRequest(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
