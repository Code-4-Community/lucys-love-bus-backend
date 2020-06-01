package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class ForgotPasswordRequest extends ApiDto {

  private String email;

  public ForgotPasswordRequest(String email) {
    this.email = email;
  }

  private ForgotPasswordRequest() {}

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldPrefix + "email");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "forgot_password_request.";
  }
}
