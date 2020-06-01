package com.codeforcommunity.dto.user;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class ChangePasswordRequest extends ApiDto {

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
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "change_password_request.";
    List<String> fields = new ArrayList<>();
    if (currentPassword == null) {
      fields.add(fieldName + "current_password");
    }
    if (passwordInvalid(newPassword)) {
      fields.add(fieldName + "new_password");
    }
    return fields;
  }
}
