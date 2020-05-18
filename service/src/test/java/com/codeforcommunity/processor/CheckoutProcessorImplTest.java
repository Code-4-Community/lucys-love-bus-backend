package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

public class CheckoutProcessorImplTest {
  private JooqMock myJooqMock;
  private CheckoutProcessorImpl myCheckoutProcessorImpl;

  // set up all the mocks
  @BeforeAll
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myCheckoutProcessorImpl = new CheckoutProcessorImpl(myJooqMock.getContext());
  }

  // test creating checkout session and event registration if not a general user
  @Test
  public void testCreateCheckoutSessionAndEventRegistration1() {
    JWTData myUser1 = new JWTData(1, PrivilegeLevel.PF);
    JWTData myUser2 = new JWTData(1, PrivilegeLevel.ADMIN);

    PostCreateCheckoutSession req = new PostCreateCheckoutSession(
        new ArrayList<>(),
        "https://lucy.c4cneu.com/checkout",
        "https://lucy.c4cneu.com/checkout"
    );

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

  // test case where creating checkout session and event registration works correctly
  @Test
  public void testCreateCheckoutSessionAndEventRegistration2() throws StripeException {
    JWTData myUser = new JWTData(1, PrivilegeLevel.GP);

    PostCreateCheckoutSession req = new PostCreateCheckoutSession(
        new ArrayList<>(),
        "https://lucy.c4cneu.com/checkout",
        "https://lucy.c4cneu.com/checkout"
    );

    // mock the Stripe API interface
    Session mockSession = mock(Session.class);
    SessionCreateParams mockSessionCreateParams = mock(SessionCreateParams.class);
    Properties stripeProperties = PropertiesLoader.getStripeProperties();

    when(Stripe.apiKey).thenReturn(stripeProperties.getProperty("stripe_api_secret_key"));

    when(mockSession.getId()).thenReturn("0");
    when(Session.create(mockSessionCreateParams)).thenReturn(mockSession);

    String res = myCheckoutProcessorImpl.createCheckoutSessionAndEventRegistration(req, myUser);
    System.out.println(res);
  }

  @Test
  public void testCreateEventRegistration1() {
    fail("TODO!!!");
  }

  @Test
  public void testHandleStripeCheckoutEventComplete1() {
    fail("TODO!!!");
  }
}