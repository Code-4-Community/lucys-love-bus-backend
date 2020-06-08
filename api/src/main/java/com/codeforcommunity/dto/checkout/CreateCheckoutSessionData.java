package com.codeforcommunity.dto.checkout;

import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.List;

public class CreateCheckoutSessionData {

  public static final String CURRENCY_UNITS = "usd";

  private List<LineItem> lineItems;
  private String cancelUrl;
  private String successUrlTemplate;

  public CreateCheckoutSessionData() {}

  public CreateCheckoutSessionData(
      List<LineItem> lineItems, String cancelUrl, String successUrlTemplate) {
    this.lineItems = lineItems;
    this.cancelUrl = cancelUrl;
    this.successUrlTemplate = successUrlTemplate;
  }

  public List<LineItem> getLineItems() {
    return this.lineItems;
  }

  public String getCancelUrl() {
    return this.cancelUrl;
  }

  public String getSuccessUrlTemplate() {
    return this.successUrlTemplate;
  }

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
