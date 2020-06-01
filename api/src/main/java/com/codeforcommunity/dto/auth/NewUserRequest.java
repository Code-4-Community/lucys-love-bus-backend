package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

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
