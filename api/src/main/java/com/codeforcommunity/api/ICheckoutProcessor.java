package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.stripe.model.checkout.Session;

public interface ICheckoutProcessor {
    String createCheckoutSession(PostCheckoutRequest request, JWTData data) throws StripeExternalException;

    void createEventRegistration(PostCheckoutRequest request, JWTData data);

    void handleStripeCheckoutEventComplete(Session session);
}
