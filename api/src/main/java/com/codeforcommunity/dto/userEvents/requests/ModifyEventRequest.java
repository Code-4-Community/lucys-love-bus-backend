package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.dto.userEvents.components.EventDetails;
import java.util.Optional;

public class ModifyEventRequest {
  private Optional<String> title;
  private Optional<Integer> spotsAvailable;
  private Optional<String> thumbnail;
  private Optional<EventDetails> details;

  public ModifyEventRequest(Optional<String> title,
      Optional<Integer> spotsAvailable, Optional<String> thumbnail,
      Optional<EventDetails> details) {
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
  }

  public Optional<String> getTitle() {
    return title;
  }

  public Optional<Integer> getSpotsAvailable() {
    return spotsAvailable;
  }

  public Optional<String> getThumbnail() {
    return thumbnail;
  }

  public Optional<EventDetails> getDetails() {
    return details;
  }
}
