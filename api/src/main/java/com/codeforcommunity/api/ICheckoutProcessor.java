package com.codeforcommunity.api;

import com.codeforcommunity.dto.checkout.PostCheckoutRequest;

public interface ICheckoutProcessor {
    String createCheckoutSession(PostCheckoutRequest request);
}
