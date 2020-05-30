package com.codeforcommunity.exceptions;

import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class WrongPrivilegeException extends HandledException {

  private PrivilegeLevel requiredPrivilegeLevel;

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleWrongPrivilegeException(ctx, this);
  }

  public WrongPrivilegeException(PrivilegeLevel requiredPrivilegeLevel) {
    super();
    this.requiredPrivilegeLevel = requiredPrivilegeLevel;
  }

  public PrivilegeLevel getRequiredPrivilegeLevel() {
    return requiredPrivilegeLevel;
  }
}
