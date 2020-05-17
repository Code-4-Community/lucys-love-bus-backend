package com.codeforcommunity.dto.checkout;

import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.List;

public class PostCreateCheckoutSession {

  private List<LineItemRequest> lineItems;
  private String cancelUrl;
  private String successUrl;

  private PostCreateCheckoutSession() {}

  public PostCreateCheckoutSession(
      List<LineItemRequest> lineItems, String cancelUrl, String successUrl) {
    this.lineItems = lineItems;
    this.cancelUrl = cancelUrl;
    this.successUrl = successUrl;
  }

  public List<LineItemRequest> getLineItems() {
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
    for (LineItemRequest item : this.lineItems) {
      SessionCreateParams.LineItem stripe_line_item =
          new SessionCreateParams.LineItem.Builder()
              .setName(item.getName())
              .setAmount(item.getAmount() * 100)
              .setCurrency(item.getCurrency())
              .setQuantity(item.getQuantity())
              .setDescription(item.getDescription())
              .build();
      out.add(stripe_line_item);
    }
    return out;
  }
}
