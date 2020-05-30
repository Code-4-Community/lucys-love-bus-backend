package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.HandledException;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class LoginRequest implements ApiDto {

  private String email;
  private String password;

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

  @Override
  public void validate() throws HandledException {
    if (email == null) {
      throw new MalformedParameterException("Email");
    }
    if (password == null) {
      throw new MalformedParameterException("Password");
    }
  }
}
