package com.codeforcommunity.dto.checkout;

public class LineItemRequest {
    private String name;
    private String description;
    private Long amount;
    private String currency;
    private Long quantity;

    private LineItemRequest() {}

    public LineItemRequest(String name, String description, Integer amount, String currency, Integer quantity) {
        this.name = name;
        this.description = description;
        this.amount = Long.valueOf(amount);
        this.currency = currency;
        this.quantity = Long.valueOf(quantity);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getAmount() { return amount; }

    public String getCurrency() {
        return currency;
    }

    public Long getQuantity() { return quantity; }
}
