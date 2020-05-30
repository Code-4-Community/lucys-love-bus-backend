package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class NewUserRequest implements ApiDto {

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
  public void validate() {
    if (email == null) {
      throw new MalformedParameterException("Email");
    }
    if (password == null) {
      throw new MalformedParameterException("Password");
    }
    if (firstName == null) {
      throw new MalformedParameterException("First name");
    }
    if (lastName == null) {
      throw new MalformedParameterException("Last name");
    }
    if (phoneNumber == null) {
      throw new MalformedParameterException("Phone number");
    }
    if (location == null) {
      throw new MalformedParameterException("Location");
    }
    location.validate();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public AddressData getLocation() {
    return location;
  }

  public void setLocation(AddressData location) {
    this.location = location;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getAllergies() {
    return allergies;
  }

  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }
}
