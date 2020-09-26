package com.codeforcommunity.dto.auth;

/**
 * Representing the SessionResponse portion of the Auth DTO
 */
public class SessionResponse {

  private String accessToken;
  private String refreshToken;

  /**
   * Retrieves the accessToken field.
   *
   * @return the String accessToken field.
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets this SessionResponse's accessToken field to the provided accessToken.
   *
   * @param accessToken the value to set this accessToken field to.
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
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
   * Sets this SessionResponse's refreshToken field to the provided refreshToken.
   *
   * @param refreshToken the value to set this refreshToken field to.
   */
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
