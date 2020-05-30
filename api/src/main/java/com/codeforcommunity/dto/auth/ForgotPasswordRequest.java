package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class ForgotPasswordRequest implements ApiDto {

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
  public void validate() {
    if (email == null) {
      throw new MalformedParameterException("Email");
    }
  }
}
