package com.codeforcommunity.dto.protected_user.components;

import com.codeforcommunity.enums.PrivilegeLevel;

public class UserSummary {
  private final String firstName;
  private final String lastName;
  private final String email;
  private final int userId;
  private final PrivilegeLevel privilegeLevel;
  private final String phoneNumber;
  private final String profilePicture;
  private final boolean photoRelease;

  public UserSummary(
      String firstName,
      String lastName,
      String email,
      int userId,
      PrivilegeLevel privilegeLevel,
      String phoneNumber,
      String profilePicture,
      boolean photoRelease) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.userId = userId;
    this.privilegeLevel = privilegeLevel;
    this.phoneNumber = phoneNumber;
    this.profilePicture = profilePicture;
    this.photoRelease = photoRelease;
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
   * Gets the first name of the user
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Gets the last name of the user
   *
   * @return the last names
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Gets the ID of the user
   *
   * @return the user ID
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Gets the privilege level for the user
   *
   * @return the privilege level
   */
  public PrivilegeLevel getPrivilegeLevel() {
    return privilegeLevel;
  }

  /**
   * Gets the phone number of the user
   *
   * @return the phone number
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Gets the profile picture of the user
   *
   * @return the profile picture
   */
  public String getProfilePicture() {
    return profilePicture;
  }

  /**
   * Gets whether the user has consented to photo and video release
   *
   * @return whether the user has consented to photo and video release
   */
  public boolean getPhotoRelease() {
    return photoRelease;
  }
}
