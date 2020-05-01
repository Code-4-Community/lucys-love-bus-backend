package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.exceptions.EmailAlreadyInUseException;
import com.codeforcommunity.exceptions.EventRegistrationException;
import com.codeforcommunity.exceptions.HandledException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.exceptions.MissingHeaderException;
import com.codeforcommunity.exceptions.MissingParameterException;
import com.codeforcommunity.exceptions.ResourceNotOwnedException;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import io.vertx.ext.web.RoutingContext;

public class FailureHandler {

   public void handleFailure(RoutingContext ctx) {
    Throwable throwable = ctx.failure();

    if(throwable instanceof HandledException) {
      ((HandledException) throwable).callHandler(this, ctx);
    } else {
      this.handleUncaughtError(ctx, throwable);
    }
  }

  public void handleAuth(RoutingContext ctx) {
    end(ctx, "Unauthorized user", 401);
  }

  public void handleAccessTokenInvalid(RoutingContext ctx) {
     String message = "Given access token is expired or invalid";
     end(ctx, message, 401);
  }

  public void handleMissingParameter(RoutingContext ctx, MissingParameterException e) {
    String message = String.format("Missing required path parameter: %s", e.getMissingParameterName());
    end(ctx, message, 400);
  }

  public void handleMissingHeader(RoutingContext ctx, MissingHeaderException e) {
    String message = String.format("Missing required request header: %s", e.getMissingHeaderName());
    end(ctx, message, 400);
  }

  public void handleRequestBodyMapping(RoutingContext ctx) {
    String message = "Malformed json request body";
    end(ctx, message, 400);
  }

  public void handleMissingBody(RoutingContext ctx) {
    String message = "Missing required request body";
    end(ctx, message, 400);
  }

  public void handleEmailAlreadyInUse(RoutingContext ctx, EmailAlreadyInUseException exception) {
    String message = String.format("Error creating new user, given email %s already used", exception.getEmail());

    end(ctx, message, 409);
  }

  public void handleUserDoesNotExist(RoutingContext ctx, UserDoesNotExistException exception) {
    String message = String.format("No user with property <%s> exists", exception.getIdentifierMessage());
    end(ctx, message, 400);
  }

  public void handleInvalidEmailVerificationToken(RoutingContext ctx) {
    String message = "Given token is invalid";
    end(ctx, message, 401);
  }

  public void handleExpiredEmailVerificationToken(RoutingContext ctx) {
    String message = "Given token is expired";
    end(ctx, message, 401);
  }

  public void handleMalformedParameter(RoutingContext ctx, MalformedParameterException exception) {
     String message = String.format("Given parameter %s is malformed", exception.getParameterName());
     end(ctx, message, 400);
  }

  public void handleAdminOnlyRoute(RoutingContext ctx) {
     String message = "This route is only available to admin users";
     end(ctx, message, 401);
  }

  public void handleOutstandingRequestException(RoutingContext ctx) {
     String message = "This user cannot open another request until all pending requests are reviewed by an admin";
     end(ctx, message, 429);
  }

  public void handleResourceNotOwned(RoutingContext ctx, ResourceNotOwnedException exception) {
     String message = String.format("The resource <%s> is not owned by the calling user and is thus not accessible", exception.getResource());
     end(ctx, message, 401);
  }

  public void handleWrongPrivilegeException(RoutingContext ctx, WrongPrivilegeException exception) {
     String message = "This route is only available to users with the privilege: " + exception.getRequiredPrivilegeLevel().name();
     end(ctx, message, 401);
  }

  public void handleEventRegistrationException(RoutingContext ctx, EventRegistrationException exception) {
       String message = "Error registering for event: " + exception.getMessage();
       end(ctx, message, 400);
  }

  public void handleStripeExternalException(RoutingContext ctx, StripeExternalException exception) {
       String message = "A call to Stripe's API returned an internal server error: " + exception.getMessage();
       end(ctx, message, 502);
  }

  private void handleUncaughtError(RoutingContext ctx, Throwable throwable){
    String message = String.format("Internal server error caused by: %s", throwable.getMessage());
    end(ctx, message, 500);
  }

  private void end(RoutingContext ctx, String message, int statusCode) {
    ctx.response().setStatusCode(statusCode).end(message);
  }

}
