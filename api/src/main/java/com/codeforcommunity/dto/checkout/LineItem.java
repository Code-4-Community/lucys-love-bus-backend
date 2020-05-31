package com.codeforcommunity.dto.checkout;

public class LineItem {
  private String name;
  private String description;
  private Integer cents;
  private Integer quantity;
  private Integer id;

  private LineItem() {}

  public LineItem(String name, String description, Integer amount, Integer quantity, Integer id) {
    this.name = name;
    this.description = description;
    this.cents = amount;
    this.quantity = quantity;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Integer getCents() {
    return cents;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public Integer getId() {
    return id;
  }
}
