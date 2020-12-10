package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class AccessTokenInvalidException extends HandledException {

  public AccessTokenInvalidException() {
    super();
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleAccessTokenInvalid(ctx);
  }
}
