package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.LineItemRequest;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.requester.Emailer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutProcessorImplTest {

  private CheckoutProcessorImpl processor;
  private Emailer emailMock = mock(Emailer.class);
  private JooqMock mock;

  @BeforeEach
  private void setup() {
    this.mock = new JooqMock();
    this.processor = new CheckoutProcessorImpl(mock.getContext(), emailMock);
  }

  @Test
  void createEventRegistration() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    List<LineItemRequest> requests = new ArrayList<>();
    try {
      processor.createEventRegistration(new PostCreateEventRegistrations(requests), jwtData);
      fail();
    } catch (MalformedParameterException e) {
      assertEquals(e.getParameterName(), "lineItems");
    }
  }
}
