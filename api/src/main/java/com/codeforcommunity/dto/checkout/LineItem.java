package com.codeforcommunity.dto.checkout;

public class LineItem {
  private String name;
  private String description;
  private Long cents;
  private Long quantity;
  private Integer id;

  private LineItem() {}

  public LineItem(String name, String description, Integer amount, Integer quantity, Integer id) {
    this.name = name;
    this.description = description;
    this.cents = Long.valueOf(amount);
    this.quantity = Long.valueOf(quantity);
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Long getCents() {
    return cents;
  }

  public Long getQuantity() {
    return quantity;
  }

  public Integer getId() {
    return id;
  }
}
