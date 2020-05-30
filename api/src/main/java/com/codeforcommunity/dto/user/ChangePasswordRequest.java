package com.codeforcommunity.dto.user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class ChangePasswordRequest implements ApiDto {

  private String currentPassword;
  private String newPassword;

  public ChangePasswordRequest(String currentPassword, String newPassword) {
    this.currentPassword = currentPassword;
    this.newPassword = newPassword;
  }

  private ChangePasswordRequest() {}

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  @Override
  public void validate() {
    if (currentPassword == null) {
      throw new MalformedParameterException("Current password");
    }
    if (newPassword == null) {
      throw new MalformedParameterException("New password");
    }
  }
}
