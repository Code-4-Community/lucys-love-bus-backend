package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/** Representing the NewUserRequest portion of the Auth DTO */
public class NewUserRequest extends ApiDto {

  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private AddressData location;
  private String phoneNumber;
  private String allergies;

  public NewUserRequest(
      String email,
      String password,
      String firstName,
      String lastName,
      AddressData location,
      String phoneNumber,
      String allergies) {
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.location = location;
    this.phoneNumber = phoneNumber;
    this.allergies = allergies;
  }

  private NewUserRequest() {}

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "new_user_request.";
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldName + "email");
    }
    if (passwordInvalid(password)) {
      fields.add(fieldName + "password");
    }
    if (isEmpty(firstName)) {
      fields.add(fieldName + "first_name");
    }
    if (isEmpty(lastName)) {
      fields.add(fieldName + "last_name");
    }
    if (location == null) {
      fields.add(fieldName + "location");
    } else {
      fields.addAll(location.validateFields(fieldName));
    }
    return fields;
  }

  /**
   * Retrieves the email field.
   *
   * @return the String email field.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets this NewUserRequest's email field to the provided email.
   *
   * @param email the value to set this email field to.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Retrieves the password field.
   *
   * @return the String password field.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets this NewUserRequest's password field to the provided password.
   *
   * @param password the value to set this password field to.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Retrieves the firstName field.
   *
   * @return the String firstName field.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets this NewUserRequest's firstName field to the provided firstName.
   *
   * @param firstName the value to set this firstName field to.
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Retrieves the lastName field.
   *
   * @return the String lastName field.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets this NewUserRequest's lastName field to the provided lastName.
   *
   * @param lastName the value to set this lastName field to.
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Retrieves the location field.
   *
   * @return the AddressData location field.
   */
  public AddressData getLocation() {
    return location;
  }

  /**
   * Sets this NewUserRequest's location field to the provided location.
   *
   * @param location the value to set this location field to.
   */
  public void setLocation(AddressData location) {
    this.location = location;
  }

  /**
   * Retrieves the phoneNumber field.
   *
   * @return the String phoneNumber field.
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Sets this NewUserRequest's phoneNumber field to the provided phoneNumber.
   *
   * @param phoneNumber the value to set this phoneNumber field to.
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Retrieves the allergies field.
   *
   * @return the String allergies field.
   */
  public String getAllergies() {
    return allergies;
  }

  /**
   * Sets this NewUserRequest's allergies field to the provided allergies.
   *
   * @param allergies the value to set this allergies field to.
   */
  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }
}
