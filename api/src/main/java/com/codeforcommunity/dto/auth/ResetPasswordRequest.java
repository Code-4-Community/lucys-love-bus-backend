package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class ResetPasswordRequest extends ApiDto {

  private String secretKey;
  private String newPassword;

  public ResetPasswordRequest(String secretKey, String newPassword) {
    this.secretKey = secretKey;
    this.newPassword = newPassword;
  }

  private ResetPasswordRequest() {}

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getNewPassword() {
    return newPassword;
  }

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
