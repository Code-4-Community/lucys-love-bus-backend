package com.codeforcommunity.dto.checkout;

/**
 * Represents one of the lines that will appear on the tab shown at checkout (aka ticket(s) for a
 * specific event). Includes the event name, description, id, price (in cents), and number of
 * tickets to be purchased.
 */
public class LineItem {
  private String name;
  private String description;
  private Integer cents;
  private Integer quantity;
  private Integer id;

  private LineItem() {}

  /**
   * Constructs a item (event tickets) to be purchased at checkout with the given event name,
   * description, amount (price in cents), id, and the quantity of tickets being purchased.
   *
   * @param name event name
   * @param description event description
   * @param amount price in cents of the tickets
   * @param quantity number of tickets
   * @param id event id
   */
  public LineItem(String name, String description, Integer amount, Integer quantity, Integer id) {
    this.name = name;
    this.description = description;
    this.cents = amount;
    this.quantity = quantity;
    this.id = id;
  }

  /**
   * Gets the name of the event these tickets are for.
   *
   * @return name of the event these tickets are for
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the description of the event these tickets are for.
   *
   * @return description of the event these tickets are for
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the price in cents of these tickets.
   *
   * @return price in cents of these tickets
   */
  public Integer getCents() {
    return cents;
  }

  /**
   * Gets the number of tickets being purchased in this line item.
   *
   * @return number of tickets being purchased in this line item
   */
  public Integer getQuantity() {
    return quantity;
  }

  /**
   * Gets the id of the event these tickets are for.
   *
   * @return id of the event these tickets are for
   */
  public Integer getId() {
    return id;
  }
}
