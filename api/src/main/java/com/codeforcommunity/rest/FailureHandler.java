package com.codeforcommunity.rest;

import com.codeforcommunity.exceptions.AlreadyRegisteredException;
import com.codeforcommunity.exceptions.CreateUserException;
import com.codeforcommunity.exceptions.EmailAlreadyInUseException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.exceptions.ExpiredSecretKeyException;
import com.codeforcommunity.exceptions.HandledException;
import com.codeforcommunity.exceptions.InsufficientEventCapacityException;
import com.codeforcommunity.exceptions.InvalidEventCapacityException;
import com.codeforcommunity.exceptions.InvalidSecretKeyException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.exceptions.MissingHeaderException;
import com.codeforcommunity.exceptions.MissingParameterException;
import com.codeforcommunity.exceptions.NotRegisteredException;
import com.codeforcommunity.exceptions.RequestDoesNotExistException;
import com.codeforcommunity.exceptions.ResourceNotOwnedException;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.exceptions.StripeInvalidWebhookSecretException;
import com.codeforcommunity.exceptions.TableNotMatchingUserException;
import com.codeforcommunity.exceptions.TokenInvalidException;
import com.codeforcommunity.exceptions.UsedSecretKeyException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.UsernameAlreadyInUseException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.codeforcommunity.logger.SLogger;
import io.vertx.ext.web.RoutingContext;

public class FailureHandler {
  private final SLogger logger = new SLogger(FailureHandler.class);

  public void handleFailure(RoutingContext ctx) {
    Throwable throwable = ctx.failure();

    if (throwable instanceof HandledException) {
      ((HandledException) throwable).callHandler(this, ctx);
    } else {
      this.handleUncaughtError(ctx, throwable);
    }
  }

  public void handleAuth(RoutingContext ctx) {
    end(ctx, "Unauthorized user", 401);
  }

  public void handleMissingParameter(RoutingContext ctx, MissingParameterException e) {
    String message =
        String.format("Missing required path parameter: %s", e.getMissingParameterName());
    end(ctx, message, 400);
  }

  public void handleEmailAlreadyInUse(RoutingContext ctx, EmailAlreadyInUseException exception) {
    String message =
        String.format("Error creating new user, given email %s already used", exception.getEmail());

    end(ctx, message, 409);
  }

  public void handleUsernameAlreadyInUse(
      RoutingContext ctx, UsernameAlreadyInUseException exception) {
    String message =
        String.format(
            "Error creating new user, given username %s already used", exception.getUsername());

    end(ctx, message, 409);
  }

  public void handleInvalidSecretKey(RoutingContext ctx, InvalidSecretKeyException exception) {
    String message = String.format("Given %s token is invalid", exception.getType());
    end(ctx, message, 401);
  }

  public void handleUsedSecretKey(RoutingContext ctx, UsedSecretKeyException exception) {
    String message = String.format("Given %s token has already been used", exception.getType());
    end(ctx, message, 401);
  }

  public void handleExpiredSecretKey(RoutingContext ctx, ExpiredSecretKeyException exception) {
    String message = String.format("Given %s token is expired", exception.getType());
    end(ctx, message, 401);
  }

  public void handleInvalidPassword(RoutingContext ctx) {
    String message = "Given password does not meet the security requirements";
    end(ctx, message, 400);
  }

  public void handleUserDoesNotExist(RoutingContext ctx, UserDoesNotExistException exception) {
    String message =
        String.format("No user with property <%s> exists", exception.getIdentifierMessage());
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

  public void handleCreateUser(RoutingContext ctx, CreateUserException exception) {
    CreateUserException.UsedField reason = exception.getUsedField();

    String reasonMessage =
        reason.equals(CreateUserException.UsedField.BOTH)
            ? "email and user name"
            : reason.toString();

    String message = String.format("Error creating new user, given %s already used", reasonMessage);

    end(ctx, message, 409);
  }

  public void handleTokenInvalid(RoutingContext ctx, TokenInvalidException e) {
    String message = String.format("Given %s token is expired or invalid", e.getTokenType());
    end(ctx, message, 401);
  }

  public void handleWrongPassword(RoutingContext ctx) {
    String message = "Given password is not correct";
    end(ctx, message, 401);
  }

  public void handleInvalidToken(RoutingContext ctx) {
    String message = "Given token is invalid";
    end(ctx, message, 401);
  }

  public void handleExpiredToken(RoutingContext ctx) {
    String message = "Given token is expired";
    end(ctx, message, 401);
  }

  public void handleMalformedParameter(RoutingContext ctx, MalformedParameterException exception) {
    String message =
        String.format("Given parameter(s) %s is (are) malformed", exception.getParameterName());
    end(ctx, message, 400);
  }

  public void handleBadImageRequest(RoutingContext ctx) {
    String message = "The uploaded file could not be processed as an image";
    end(ctx, message, 400);
  }

  public void handleS3FailedUpload(RoutingContext ctx, String exceptionMessage) {
    String message = "The given file could not be uploaded to AWS S3: " + exceptionMessage;
    end(ctx, message, 502);
  }

  public void handleRequestDoesNotExist(
      RoutingContext ctx, RequestDoesNotExistException exception) {
    String message = String.format("No request with id <%d> exists", exception.getRequestId());
    end(ctx, message, 400);
  }

  public void handleAdminOnlyRoute(RoutingContext ctx) {
    String message = "This route is only available to admin users";
    end(ctx, message, 401);
  }

  public void handleOutstandingRequestException(RoutingContext ctx) {
    String message =
        "This user cannot open another request until all pending requests are reviewed by an admin";
    end(ctx, message, 429);
  }

  public void handleResourceNotOwned(RoutingContext ctx, ResourceNotOwnedException exception) {
    String message =
        String.format(
            "The resource <%s> is not owned by the calling user and is thus not accessible",
            exception.getResource());
    end(ctx, message, 401);
  }

  public void handleTableNotMatchingUser(
      RoutingContext ctx, TableNotMatchingUserException exception) {
    String message =
        String.format(
            "The passed %s resource with id %d is not owned by the calling user",
            exception.getTableName(), exception.getTableId());
    end(ctx, message, 400);
  }

  public void handleWrongPrivilegeException(RoutingContext ctx, WrongPrivilegeException exception) {
    String message =
        "This route is only available to users with the privilege: "
            + exception.getRequiredPrivilegeLevel().name();
    end(ctx, message, 401);
  }

  public void handleInsufficientEventCapacityException(
      RoutingContext ctx, InsufficientEventCapacityException exception) {
    String message =
        "The user requested more tickets than are available for the event: "
            + exception.getEventTitle();
    end(ctx, message, 409);
  }

  public void handleNotRegisteredException(RoutingContext ctx, NotRegisteredException e) {
    String message = "You are not registered for the event " + e.getEventTitle();
    end(ctx, message, 409);
  }

  public void handleInvalidEventCapacityException(
      RoutingContext ctx, InvalidEventCapacityException e) {
    String message =
        String.format(
            "Cannot change the event capacity to %d because there are currently %d users signed up",
            e.getDesiredCapacity(), e.getCurrentParticipants());
    end(ctx, message, 409);
  }

  public void handleAlreadyRegisteredException(RoutingContext ctx, AlreadyRegisteredException e) {
    String message = "You are already registered for the event " + e.getEventTitle();
    end(ctx, message, 409);
  }

  public void handleStripeExternalException(RoutingContext ctx, StripeExternalException exception) {
    String message =
        "A call to Stripe's API returned an internal server error: " + exception.getMessage();
    end(ctx, message, 502);
  }

  public void handleStripeInvalidWebhookSecretException(
      RoutingContext ctx, StripeInvalidWebhookSecretException exception) {
    String message =
        "Incoming Stripe webhook request could not be verified: " + exception.getMessage();
    end(ctx, message, 400);
  }

  public void handleEventDoesNotExistException(
      RoutingContext ctx, EventDoesNotExistException exception) {
    String message = "There is no event with id: " + exception.getEventId();
    end(ctx, message, 404);
  }

  private void handleUncaughtError(RoutingContext ctx, Throwable throwable) {
    String message = String.format("Internal server error caused by: %s", throwable.getMessage());
    throwable.printStackTrace();
    end(ctx, message, 500);
  }

  private void end(RoutingContext ctx, String message, int statusCode) {
    logger.error(message);
    ctx.response().setStatusCode(statusCode).end(message);
  }
}
