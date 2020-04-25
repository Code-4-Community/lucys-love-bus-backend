package com.codeforcommunity.dto.checkout;

import java.util.List;

public class PostCheckoutRequest {

    private List<Object> payment_method_types;
    private List<Object> line_items;
    private String cancel_url;
    private String success_url;

    public PostCheckoutRequest() {}

    public PostCheckoutRequest(List<Object> payment_method_types, List<Object> line_items, String cancel_url, String success_url) {
        this.payment_method_types = payment_method_types;
        this.line_items = line_items;
        this.cancel_url = cancel_url;
        this.success_url = success_url;
    }

    public List<Object> getPayment_method_types() { return this.payment_method_types; }
    public List<Object> getLine_items() { return this.line_items; }
    public String getCancel_url() { return this.cancel_url; }
    public String getSuccess_url() { return this.success_url; }

}
