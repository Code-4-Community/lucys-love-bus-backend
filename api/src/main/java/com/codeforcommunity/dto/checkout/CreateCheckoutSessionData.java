package com.codeforcommunity.dto.checkout;

import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.List;

/** */
public class CreateCheckoutSessionData {

  /** The currency units to be used for stripe checkout session. */
  public static final String CURRENCY_UNITS = "usd";

  private List<LineItem> lineItems;
  private String cancelUrl;
  private String successUrl;

  public CreateCheckoutSessionData() {}

  /**
   * Constructs a CreateCheckoutSessionData object, which contains a list of LineItems that are
   * being purchased at this checkout, the url to return to if checkout is cancelled, and the url to
   * go to is the checkout is successful.
   *
   * @param lineItems list of LineItems that are being purchased at checkout
   * @param cancelUrl the url to go to if this checkout is cancelled
   * @param successUrl the url to go to if this checkout is successful
   */
  public CreateCheckoutSessionData(List<LineItem> lineItems, String cancelUrl, String successUrl) {
    this.lineItems = lineItems;
    this.cancelUrl = cancelUrl;
    this.successUrl = successUrl;
  }

  /**
   * Get the list of LineItems stored in this object.
   *
   * @return the list of LineItems stored in this object
   */
  public List<LineItem> getLineItems() {
    return this.lineItems;
  }

  /**
   * Get the url to go to if this checkout is cancelled.
   *
   * @return the url to go to if this checkout is cancelled
   */
  public String getCancelUrl() {
    return this.cancelUrl;
  }

  /**
   * Get the url to go to if this checkout is successful.
   *
   * @return the url to go to if this checkout is successful
   */
  public String getSuccessUrl() {
    return this.successUrl;
  }

  /**
   * Creates and returns the list of line items in the appropriate format for creating a Stripe
   * session.
   *
   * @return list of line items in the appropriate format for creating a Stripe session
   */
  public List<SessionCreateParams.LineItem> getStripeLineItems() {
    List<SessionCreateParams.LineItem> out = new ArrayList<>();
    for (LineItem item : this.lineItems) {
      SessionCreateParams.LineItem stripe_line_item =
          new SessionCreateParams.LineItem.Builder()
              .setName(item.getName())
              .setAmount(Long.valueOf(item.getCents()))
              .setCurrency(CURRENCY_UNITS)
              .setQuantity(Long.valueOf(item.getQuantity()))
              .setDescription(item.getDescription())
              .build();
      out.add(stripe_line_item);
    }
    return out;
  }
}
