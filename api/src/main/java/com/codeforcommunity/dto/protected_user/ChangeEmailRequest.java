package com.codeforcommunity.dto.protected_user;

public class ChangeEmailRequest {

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
}
