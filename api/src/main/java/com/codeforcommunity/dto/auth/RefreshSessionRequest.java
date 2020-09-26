package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Representing the RefreshSessionRequest portion of the Auth DTO
 */
public class RefreshSessionRequest extends ApiDto {

  private String refreshToken;

  public RefreshSessionRequest(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  /**
   * Retrieves the refreshToken field.
   *
   * @return the String refreshToken field.
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Sets this RefreshSessionRequest's refreshToken field to the provided refreshToken.
   *
   * @param refreshToken the value to set this refreshToken field to.
   */
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "refresh_session_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(refreshToken) || refreshToken.split("\\.").length < 3) {
      fields.add(fieldName + "refresh_token");
    }
    return fields;
  }
}
