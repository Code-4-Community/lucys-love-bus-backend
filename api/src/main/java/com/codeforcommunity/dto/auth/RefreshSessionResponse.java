package com.codeforcommunity.dto.auth;

/**
 * Representing the RefreshSessionResponse portion of the Auth DTO
 */
public class RefreshSessionResponse {

  private String freshAccessToken;

  /**
   * Retrieves the freshAccessToken field.
   *
   * @return the String freshAccessToken field.
   */
  public String getFreshAccessToken() {
    return freshAccessToken;
  }

  /**
   * Sets this RefreshSessionResponse's freshAccessToken field to the provided freshAccessToken.
   *
   * @param freshAccessToken the value to set this freshAccessToken field to.
   */
  public void setFreshAccessToken(String freshAccessToken) {
    this.freshAccessToken = freshAccessToken;
  }
}
