package com.codeforcommunity.dto.userEvents.components;

public class Registration {
  private final String firstName;
  private final String lastName;
  private final String email;
  private final int ticketCount;

  public Registration(String firstName, String lastName, String email, int ticketCount) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.ticketCount = ticketCount;
  }

  public int getTicketCount() {
    return ticketCount;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
