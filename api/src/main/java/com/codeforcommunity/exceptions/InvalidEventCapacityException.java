package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class InvalidEventCapacityException extends RuntimeException implements HandledException {

  private int desiredCapacity;
  private int currentParticipants;

  public InvalidEventCapacityException(int desiredCapacity, int currentParticipants) {
    this.desiredCapacity = desiredCapacity;
    this.currentParticipants = currentParticipants;
  }

  public int getDesiredCapacity() {
    return desiredCapacity;
  }

  public int getCurrentParticipants() {
    return currentParticipants;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleInvalidEventCapacityException(ctx, this);
  }
}
