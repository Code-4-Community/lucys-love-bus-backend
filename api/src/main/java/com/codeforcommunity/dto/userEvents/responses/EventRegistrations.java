package com.codeforcommunity.dto.userEvents.responses;

import com.codeforcommunity.dto.userEvents.components.Registration;
import java.util.List;

public class EventRegistrations {
  List<Registration> registrations;

  public EventRegistrations(List<Registration> registrations) {
    this.registrations = registrations;
  }

  public List<Registration> getRegistrations() {
    return registrations;
  }
}
