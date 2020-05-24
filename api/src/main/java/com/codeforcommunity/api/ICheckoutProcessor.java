package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.exceptions.StripeExternalException;

/** Processes routes related to registering for events and paying for event tickets. */
public interface ICheckoutProcessor {

  /**
   * Creates an event registration for a participating family or an admin. Cannot be accessed by
   * GPs. This method does not deal with payment.
   *
   * @param request the request body
   * @param data the user's JWT authentication data
   */
  void createEventRegistration(PostCreateEventRegistrations request, JWTData data);

  /**
   * Creates a Stripe checkout session and an event registration for a GP. This method handles
   * payment. Can be accessed by GPs.
   *
   * @param request the request body
   * @param user the user's JWT authentication data
   * @return the ID of the Stripe checkout session
   * @throws StripeExternalException if Stripe throws an exception while creating the session
   */
  String createCheckoutSessionAndEventRegistration(
      PostCreateEventRegistrations request, JWTData user) throws StripeExternalException;

  /**
   * Updates the event registration database entry when Stripe has finished processing the payment.
   * This route should only be called by the Stripe webhook.
   *
   * @param payload the payload of the webhook
   * @param sigHeader the Stripe signature header
   */
  void handleStripeCheckoutEventComplete(String payload, String sigHeader);
}
