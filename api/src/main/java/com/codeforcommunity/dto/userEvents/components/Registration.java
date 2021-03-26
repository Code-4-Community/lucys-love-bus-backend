package com.codeforcommunity.dto.userEvents.components;

import com.codeforcommunity.enums.PrivilegeLevel;

/** A class to represent an event registration */
public class Registration {
  private final String firstName;
  private final String lastName;
  private final String email;
  private final int ticketCount;
  private final int userId;
  private final PrivilegeLevel privilegeLevel;
  private final String phoneNumber;
  private final String profilePicture;
  private final boolean photoRelease;

  public Registration(
      String firstName,
      String lastName,
      String email,
      int ticketCount,
      int userId,
      PrivilegeLevel privilegeLevel,
      String phoneNumber,
      String profilePicture,
      boolean photoRelease) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.ticketCount = ticketCount;
    this.userId = userId;
    this.privilegeLevel = privilegeLevel;
    this.phoneNumber = phoneNumber;
    this.profilePicture = profilePicture;
    this.photoRelease = photoRelease;
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

  /**
   * Gets the ID of the registrant
   *
   * @return the user ID
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Gets the privilege level for the registrant
   *
   * @return the privilege level
   */
  public PrivilegeLevel getPrivilegeLevel() {
    return privilegeLevel;
  }

  /**
   * Gets the phone number of the registrant
   *
   * @return the phone number
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Gets the profile picture of the registrant
   *
   * @return the profile picture
   */
  public String getProfilePicture() {
    return profilePicture;
  }

  /**
   * Gets whether the registrant has consented to photo and video release
   *
   * @return whether the registrant has consented to photo and video release
   */
  public boolean getPhotoRelease() {
    return photoRelease;
  }
}
