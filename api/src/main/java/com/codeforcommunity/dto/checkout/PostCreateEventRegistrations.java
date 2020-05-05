package com.codeforcommunity.dto.checkout;

import java.util.List;

public class PostCreateEventRegistrations {

    private List<LineItemRequest> lineItems;

    public PostCreateEventRegistrations(List<LineItemRequest> lineItems) {
        this.lineItems = lineItems;
    }

    protected PostCreateEventRegistrations() {}

    public List<LineItemRequest> getLineItems() { return this.lineItems; }
}
