package com.codeforcommunity.auth;

import java.util.Optional;

public class JWTCreator {
  private final JWTHandler handler;

  public JWTCreator(JWTHandler handler) {
    this.handler = handler;
  }

  public String createNewRefreshToken(String email) {
    return handler.createNewRefreshToken(email);
  }

  public Optional<String> getNewAccessToken(String refreshToken) {
    return handler.getNewAccessToken(refreshToken);
  }


}
