package com.codeforcommunity.dto.checkout;

import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.List;

public class CreateCheckoutSessionData {

  public static final String CURRENCY_UNITS = "usd";

  private List<LineItem> lineItems;
  private String cancelUrl;
  private String successUrl;

  public CreateCheckoutSessionData() {}

  public CreateCheckoutSessionData(List<LineItem> lineItems, String cancelUrl, String successUrl) {
    this.lineItems = lineItems;
    this.cancelUrl = cancelUrl;
    this.successUrl = successUrl;
  }

  public List<LineItem> getLineItems() {
    return this.lineItems;
  }

  public String getCancelUrl() {
    return this.cancelUrl;
  }

  public String getSuccessUrl() {
    return this.successUrl;
  }

  public List<SessionCreateParams.LineItem> getStripeLineItems() {
    List<SessionCreateParams.LineItem> out = new ArrayList<>();
    for (LineItem item : this.lineItems) {
      SessionCreateParams.LineItem stripe_line_item =
          new SessionCreateParams.LineItem.Builder()
              .setName(item.getName())
              .setAmount(item.getCents())
              .setCurrency(CURRENCY_UNITS)
              .setQuantity(item.getQuantity())
              .setDescription(item.getDescription())
              .build();
      out.add(stripe_line_item);
    }
    return out;
  }
}
