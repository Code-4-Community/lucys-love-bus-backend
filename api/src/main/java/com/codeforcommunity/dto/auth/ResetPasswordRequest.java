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
    List<String> fields = new ArrayList<>();
    if (secretKey == null) {
      fields.add(fieldPrefix + "secret_key");
    }
    if (newPassword == null) {
      fields.add(fieldPrefix + "new_password");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "reset_password_request.";
  }
}
