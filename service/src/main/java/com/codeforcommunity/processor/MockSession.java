package com.codeforcommunity.processor;

import com.stripe.exception.StripeException;
import com.stripe.model.HasId;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.UUID;

public class MockSession implements HasId {

  private final String id;
  private final CheckoutProcessorImpl processor;

  private MockSession(CheckoutProcessorImpl processor) {
    id = UUID.randomUUID().toString();
    this.processor = processor;
    waitHandleCheckout();
  }

  static MockSession create(SessionCreateParams params, CheckoutProcessorImpl processor)
      throws StripeException {
    return new MockSession(processor);
  }

  @Override
  public String getId() {
    return id;
  }

  private void waitHandleCheckout() {
    (new Thread(
            () -> {
              try {
                Thread.sleep(30000L);
                processor.handleStripeCheckoutEventComplete(id, "");
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }))
        .start();
  }
}
