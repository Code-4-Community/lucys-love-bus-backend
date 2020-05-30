package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.HandledException;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class ResetPasswordRequest implements ApiDto {

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
  public void validate() throws HandledException {
    if (secretKey == null) {
      throw new MalformedParameterException("Secret key");
    }
    if (newPassword == null) {
      throw new MalformedParameterException("New password");
    }
  }
}
