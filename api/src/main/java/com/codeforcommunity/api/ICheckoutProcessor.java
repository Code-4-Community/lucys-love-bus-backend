package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.exceptions.StripeExternalException;

public interface ICheckoutProcessor {
    String createCheckoutSession(PostCheckoutRequest request, JWTData data) throws StripeExternalException;
}
