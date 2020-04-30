package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.exceptions.StripeExternalException;

public interface ICheckoutProcessor {

    String createCheckoutSession(PostCreateCheckoutSession request, JWTData data) throws StripeExternalException;

    void createEventRegistration(PostCreateEventRegistrations request, JWTData data);

    void handleStripeCheckoutEventComplete(String payload, String sigHeader);

}
