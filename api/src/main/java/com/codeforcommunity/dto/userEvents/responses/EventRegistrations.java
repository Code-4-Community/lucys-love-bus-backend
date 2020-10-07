package com.codeforcommunity.dto.userEvents.responses;

import com.codeforcommunity.dto.userEvents.components.Registration;
import java.util.List;

/** A class to represent event registrations. */
public class EventRegistrations {
  List<Registration> registrations;

  public EventRegistrations(List<Registration> registrations) {
    this.registrations = registrations;
  }

  /**
   * Gets the registrations for an event.
   *
   * @return the list of registrationss
   */
  public List<Registration> getRegistrations() {
    return registrations;
  }
}
