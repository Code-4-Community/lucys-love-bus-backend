package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.exceptions.StripeExternalException;
import java.util.Optional;

/** Processes routes related to registering for events and paying for event tickets. */
public interface ICheckoutProcessor {

  /**
   * Creates an event registration. If the user is a GP, this route will create a Stripe checkout
   * session. Otherwise, the registration will be completed automatically.
   *
   * @param request the request body
   * @param data the user's JWT authentication data
   * @return an Optional of the ID of the Stripe checkout session
   * @throws StripeExternalException if Stripe throws an exception while creating the session
   */
  Optional<String> createEventRegistration(PostCreateEventRegistrations request, JWTData data)
      throws StripeExternalException;

  /**
   * Updates the event registration database entry when Stripe has finished processing the payment.
   * This route should only be called by the Stripe webhook.
   *
   * @param payload the payload of the webhook
   * @param sigHeader the Stripe signature header
   */
  void handleStripeCheckoutEventComplete(String payload, String sigHeader);

  /**
   * Updates an event registration. If the user is a GP and the number of tickets is higher than
   * what they were previously registered for, this route will create a Stripe checkout session.
   * Otherwise, the registration will be completed automatically.
   *
   * @param eventId the event ID
   * @param quantity the new quantity of tickets
   * @param userData the user's JWT authentication data
   * @return an Optional of the ID of the Stripe checkout session
   * @throws StripeExternalException if Stripe throws an exception while creating the session
   */
  Optional<String> updateEventRegistration(int eventId, int quantity, JWTData userData)
      throws StripeExternalException;
}
