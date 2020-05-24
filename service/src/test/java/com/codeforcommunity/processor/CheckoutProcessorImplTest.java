package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.stripe.exception.StripeException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains unit tests for CheckoutProcessorImpl.java in the service module
public class CheckoutProcessorImplTest {
  private JooqMock myJooqMock;
  private CheckoutProcessorImpl myCheckoutProcessorImpl;

  // set up all the mocks
  @BeforeEach
  public void setup() throws StripeException {
    this.myJooqMock = new JooqMock();
    this.myCheckoutProcessorImpl = new CheckoutProcessorImpl(myJooqMock.getContext());
  }

  // test creating checkout session and event registration fails if not a general user
  @Test
  public void testCreateCheckoutSessionAndEventRegistration() {
    JWTData myUser1 = new JWTData(1, PrivilegeLevel.PF);
    JWTData myUser2 = new JWTData(2, PrivilegeLevel.ADMIN);

    PostCreateCheckoutSession req =
        new PostCreateCheckoutSession(
            new ArrayList<>(),
            "https://lucy.c4cneu.com/checkout",
            "https://lucy.c4cneu.com/checkout");

    try {
      myCheckoutProcessorImpl.createCheckoutSessionAndEventRegistration(req, myUser1);
      fail();
    } catch (WrongPrivilegeException e) {
      assertEquals(PrivilegeLevel.GP, e.getRequiredPrivilegeLevel());
    }

    try {
      myCheckoutProcessorImpl.createCheckoutSessionAndEventRegistration(req, myUser2);
      fail();
    } catch (WrongPrivilegeException e) {
      assertEquals(PrivilegeLevel.GP, e.getRequiredPrivilegeLevel());
    }
  }

  // Jack said don't worry about testing a properly working
  // createCheckoutSessionAndEventRegistration "cause it has to do with another api"

  @Test
  public void testCreateEventRegistration1() {
    fail("TODO!!!");
  }

  @Test
  public void testHandleStripeCheckoutEventComplete1() {
    fail("TODO!!!");
  }
}
