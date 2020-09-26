package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Representing the ResetPasswordRequest portion of the Auth DTO
 */
public class ResetPasswordRequest extends ApiDto {

  private String secretKey;
  private String newPassword;

  public ResetPasswordRequest(String secretKey, String newPassword) {
    this.secretKey = secretKey;
    this.newPassword = newPassword;
  }

  private ResetPasswordRequest() {}

  /**
   * Retrieves the secretKey field.
   *
   * @return the String secretKey field.
   */
  public String getSecretKey() {
    return secretKey;
  }

  /**
   * Sets this ResetPasswordRequest's secretKey field to the provided secretKey.
   *
   * @param secretKey the value to set this secretKey field to.
   */
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  /**
   * Retrieves the newPassword field.
   *
   * @return the String newPassword field.
   */
  public String getNewPassword() {
    return newPassword;
  }

  /**
   * Sets this ResetPasswordRequest's newPassword field to the provided newPassword.
   *
   * @param newPassword the value to set this newPassword field to.
   */
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "reset_password_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(secretKey)) {
      fields.add(fieldName + "secret_key");
    }
    if (passwordInvalid(newPassword)) {
      fields.add(fieldName + "new_password");
    }
    return fields;
  }
}
