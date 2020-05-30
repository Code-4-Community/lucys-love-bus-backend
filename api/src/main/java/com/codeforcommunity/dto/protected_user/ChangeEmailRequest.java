package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class ChangeEmailRequest implements ApiDto {

  private String newEmail;
  private String password;

  public ChangeEmailRequest(String newEmail, String password) {
    this.newEmail = newEmail;
    this.password = password;
  }

  private ChangeEmailRequest() {}

  public String getNewEmail() {
    return newEmail;
  }

  public void setNewEmail(String newEmail) {
    this.newEmail = newEmail;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public void validate() {
    if (newEmail == null) {
      throw new MalformedParameterException("New email");
    }
    if (password == null) {
      throw new MalformedParameterException("Password");
    }
  }
}
