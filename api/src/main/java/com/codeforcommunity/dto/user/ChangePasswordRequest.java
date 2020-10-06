package com.codeforcommunity.dto.user;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/** Representing the change password request portion of the User DTO */
public class ChangePasswordRequest extends ApiDto {

  private String currentPassword;
  private String newPassword;

  public ChangePasswordRequest(String currentPassword, String newPassword) {
    this.currentPassword = currentPassword;
    this.newPassword = newPassword;
  }

  private ChangePasswordRequest() {}

  /**
   * Gets the user's current password
   *
   * @return the current String password
   */
  public String getCurrentPassword() {
    return currentPassword;
  }

  /**
   * Sets the user's current password to the given current password
   *
   * @param currentPassword the password the user is changing the current one to
   */
  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  /**
   * Gets the user's new password
   *
   * @return the new String password
   */
  public String getNewPassword() {
    return newPassword;
  }

  /**
   * Sets the user's new password to the given new password
   *
   * @param newPassword the password the user is changing the new one to
   */
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
