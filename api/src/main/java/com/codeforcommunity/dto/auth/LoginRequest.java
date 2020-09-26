package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Representing the LoginRequest portion of the Auth DTO
 */
public class LoginRequest extends ApiDto {

  private String email;
  private String password;

  /**
   * Retrieves the email field.
   *
   * @return the String email field.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets this LoginRequest's email field to the provided email.
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
   * Sets this LoginRequest's password field to the provided password.
   *
   * @param password the value to set this password field to.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "login_request.";
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldName + "email");
    }
    if (password == null) {
      fields.add(fieldName + "password");
    }
    return fields;
  }
}
