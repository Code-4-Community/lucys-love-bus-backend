package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/** Representing the ForgotPasswordRequest portion of the Auth DTO */
public class ForgotPasswordRequest extends ApiDto {

  private String email;

  public ForgotPasswordRequest(String email) {
    this.email = email;
  }

  private ForgotPasswordRequest() {}

  /**
   * Retrieves the email field.
   *
   * @return the String email field.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets this ForgotPasswordRequest's email field to the provided email.
   *
   * @param email the value to set this email field to.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "forgot_password_request.";
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldName + "email");
    }
    return fields;
  }
}
