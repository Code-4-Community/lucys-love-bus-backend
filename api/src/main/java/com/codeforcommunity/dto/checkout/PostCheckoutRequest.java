package com.codeforcommunity.dto.checkout;

import com.stripe.param.checkout.SessionCreateParams;

import java.util.List;
import java.util.ArrayList;

public class PostCheckoutRequest {

    private List<LineItemRequest> lineItems;
    private String cancelUrl;
    private String successUrl;

    private PostCheckoutRequest() {}

    public PostCheckoutRequest(List<LineItemRequest> lineItems, String cancelUrl, String successUrl) {
        this.lineItems = lineItems;
        this.cancelUrl = cancelUrl;
        this.successUrl = successUrl;
    }

    public List<LineItemRequest> getLineItems() { return this.lineItems; }
    public String getCancelUrl() { return this.cancelUrl; }
    public String getSuccessUrl() { return this.successUrl; }

    public List<SessionCreateParams.LineItem> getStripeLineItems() {
        List<SessionCreateParams.LineItem> out = new ArrayList<>();
        for (LineItemRequest item: this.lineItems) {
            SessionCreateParams.LineItem stripe_line_item = new SessionCreateParams.LineItem
                    .Builder()
                    .setName(item.getName())
                    .setAmount(item.getAmount())
                    .setCurrency(item.getCurrency())
                    .setQuantity(item.getQuantity())
                    .setDescription(item.getDescription())
                    .build();
            out.add(stripe_line_item);
        }
        return out;
    }

}
