package com.codeforcommunity.dto.pfrequests;

/** Represents the user request portion within participating family requests */
public class RequestUser {

  private int id;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;

  /**
   * Creates a new RequestUser with the information necessary to profile a participating family
   *
   * @param id The ID of a participating family
   * @param email The participating family's email
   * @param firstName The first name of the representative of the participating family
   * @param lastName The participating family's last name
   * @param phoneNumber The participating family's phone number
   */
  public RequestUser(int id, String email, String firstName, String lastName, String phoneNumber) {
    this.id = id;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
  }

  /**
   * Gets the current ID of a participating family
   *
   * @return the current participating family's ID
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the current ID of the participating family to the given ID
   *
   * @param id the ID for the current ID to be changed to
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Gets the current email of the participating family
   *
   * @return The current email of the participating family
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the family's current email to the given email
   *
   * @param email The new email to set the current family's email to
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the first name of the representative of the participating family
   *
   * @return The first name of the participating family's representative
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the current family representative's first name to the given first name
   *
   * @param firstName the new first name to set the current first name to
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the current family last name
   *
   * @return The current last name of the family
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the current family last name to the given last name
   *
   * @param lastName The new family last name to set the current last name to
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the current family phone number
   *
   * @return the family's current phone number
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Sets the family's current phone number to the given phone number
   *
   * @param phoneNumber The new phone number to set the current phone number to
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }
}
