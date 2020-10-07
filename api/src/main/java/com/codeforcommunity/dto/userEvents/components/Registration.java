package com.codeforcommunity.dto.userEvents.components;

/** A class to represent an event registration */
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

  /**
   * Gets the number of tickets requested in the registration
   *
   * @return the ticket count
   */
  public int getTicketCount() {
    return ticketCount;
  }

  /**
   * Gets the email associated with the registration
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Gets the first name of the registrant
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Gets the last name of the registrant
   *
   * @return the last names
   */
  public String getLastName() {
    return lastName;
  }
}
