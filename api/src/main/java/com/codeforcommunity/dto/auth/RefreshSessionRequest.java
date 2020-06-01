package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class RefreshSessionRequest extends ApiDto {
  private String refreshToken;

  public RefreshSessionRequest(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (isEmpty(refreshToken) || refreshToken.split("\\.").length < 3) {
      fields.add(fieldPrefix + "refresh_token");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "refresh_session_request.";
  }
}
