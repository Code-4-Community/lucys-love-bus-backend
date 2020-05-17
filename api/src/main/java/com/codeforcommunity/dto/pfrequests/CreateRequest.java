package com.codeforcommunity.dto.pfrequests;

public class CreateRequest {
  private String description;

  public CreateRequest(String description) {
    this.description = description;
  }

  /** This constructor exists so that Jackson can map JSON directly to it. */
  private CreateRequest() {}

  public String getDescription() {
    return description;
  }
}
