package com.codeforcommunity.dto.checkout;

public class LineItemRequest {
    private String name;
    private String description;
    private Long amount;
    private String currency;
    private Long quantity;
    private Integer id;

    private LineItemRequest() {}

    public LineItemRequest(String name, String description, Integer amount, String currency, Integer quantity, Integer id) {
        this.name = name;
        this.description = description;
        this.amount = Long.valueOf(amount);
        this.currency = currency;
        this.quantity = Long.valueOf(quantity);
        this.id = id;
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

    public Integer getId() { return id; }
}
