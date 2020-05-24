package com.codeforcommunity.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforcommunity.dto.auth.LoginRequest;
import com.codeforcommunity.dto.auth.NewUserRequest;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.exceptions.MissingHeaderException;
import com.codeforcommunity.exceptions.MissingParameterException;
import com.codeforcommunity.exceptions.RequestBodyMappingException;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

// Contains tests for RestFunctions.java in main
public class RestFunctionsTest {

  private final HttpServerRequest mockRequest = mock(HttpServerRequest.class);
  private final RoutingContext mockRoutingContext = mock(RoutingContext.class);

  // Test for empty JSON response
  @Test
  public void testGetJsonBodyAsClass1() {
    String emptyJSONString = "{}";
    JsonObject emptyJSONObject = new JsonObject(emptyJSONString);

    when(mockRoutingContext.getBodyAsJson()).thenReturn(emptyJSONObject);

    LoginRequest result = RestFunctions.getJsonBodyAsClass(mockRoutingContext, LoginRequest.class);

    assertNull(result.getEmail());
    assertNull(result.getPassword());
  }

  // Test for JSON response for login request
  @Test
  public void testGetJsonBodyAsClass2() {
    String loginRequestJSONString = "{\"email\":\"testemail\",\"password\":\"testpassword\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson()).thenReturn(loginRequestJSONObject);

    LoginRequest result = RestFunctions.getJsonBodyAsClass(mockRoutingContext, LoginRequest.class);

    String expectedEmail = "testemail";
    String expectedPassword = "testpassword";

    assertEquals(result.getEmail(), expectedEmail);
    assertEquals(result.getPassword(), expectedPassword);
  }

  // Test for JSON response for new user request
  @Test
  public void testGetJsonBodyAsClass3() {
    String loginRequestJSONString =
        "{\"email\":\"brandon@example.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"brandon\","
            + "\"lastName\":\"liang\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson()).thenReturn(loginRequestJSONObject);

    NewUserRequest result =
        RestFunctions.getJsonBodyAsClass(mockRoutingContext, NewUserRequest.class);

    String expectedEmail = "brandon@example.com";
    String expectedPassword = "password";
    String expectedFirstName = "brandon";
    String expectedLastName = "liang";

    assertEquals(result.getEmail(), expectedEmail);
    assertEquals(result.getPassword(), expectedPassword);
    assertEquals(result.getFirstName(), expectedFirstName);
    assertEquals(result.getLastName(), expectedLastName);
  }

  // tests that forces the RequestBodyMappingException to be thrown
  @Test
  public void testGetJsonBodyAsClass4() {
    when(mockRoutingContext.getBodyAsJson())
        .thenReturn(null);

    // in the case where the body is not present
    try {
      RestFunctions.getJsonBodyAsClass(mockRoutingContext, NewUserRequest.class);
      fail();
    } catch (RequestBodyMappingException ignored) {
    }

    String loginRequestJSONString =
        "{\"email\":\"brandon@example.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"brandon\","
            + "\"lastName\":\"liang\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson())
        .thenReturn(loginRequestJSONObject);

    // in the case where the wrong class was given as an argument
    try {
      RestFunctions.getJsonBodyAsClass(mockRoutingContext, LoginRequest.class);
      fail();
    } catch (RequestBodyMappingException ignored) {
      // NOTE: don't worry if an IllegalArgumentException is shown, the stack trace is intended to
      // be reported by the method
      // TODO: do you still want the printStackTrace() call in there?
    }
  }

  // test where JSON has missing fields
  @Test
  public void testGetJsonBodyAsClass5() {
    String loginRequestJSONString =
        "{\"email\":\"brandon@example.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"brandon\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson())
        .thenReturn(loginRequestJSONObject);

    NewUserRequest result =
        RestFunctions.getJsonBodyAsClass(mockRoutingContext, NewUserRequest.class);

    String expectedEmail = "brandon@example.com";
    String expectedPassword = "password";
    String expectedFirstName = "brandon";

    assertEquals(result.getEmail(), expectedEmail);
    assertEquals(result.getPassword(), expectedPassword);
    assertEquals(result.getFirstName(), expectedFirstName);
    assertNull(result.getLastName());
  }

  // test handling JSON has fields with wrong types
  @Test
  public void testGetJsonBodyAsClass6() {
    String loginRequestJSONString =
        "{\"email\":\"brandon@example.com\","
            + "\"password\":\"password\","
            // give a JSON array type, that is
            + "\"firstName\":[\"John\", \"Anna\", \"Peter\"],"
            + "\"lastName\":\"liang\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson())
        .thenReturn(loginRequestJSONObject);

    // TODO: just confirming that this is what you guys want
    try {
      RestFunctions.getJsonBodyAsClass(mockRoutingContext, NewUserRequest.class);
      fail();
    } catch (RequestBodyMappingException ignored) {}
  }

  // test handling JSON has fields with repeated fields
  @Test
  public void testGetJsonBodyAsClass7() {
    String loginRequestJSONString =
        "{\"email\":\"brandon@example.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"brandon\","
            + "\"firstName\":\"brandon\","
            + "\"lastName\":\"liang\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson())
        .thenReturn(loginRequestJSONObject);

    NewUserRequest result =
        RestFunctions.getJsonBodyAsClass(mockRoutingContext, NewUserRequest.class);

    String expectedEmail = "brandon@example.com";
    String expectedPassword = "password";
    String expectedFirstName = "brandon";
    String expectedLastName = "liang";

    assertEquals(result.getEmail(), expectedEmail);
    assertEquals(result.getPassword(), expectedPassword);
    assertEquals(result.getFirstName(), expectedFirstName);
    assertEquals(result.getLastName(), expectedLastName);
  }

  // test handling JSON has fields with extra, unexpected fields
  @Test
  public void testGetJsonBodyAsClass8() {
    String loginRequestJSONString =
        "{\"email\":\"brandon@example.com\","
            + "\"password\":\"password\","
            + "\"firstName\":\"brandon\","
            + "\"hobby\":\"coding\","
            + "\"lastName\":\"liang\"}";
    JsonObject loginRequestJSONObject = new JsonObject(loginRequestJSONString);

    when(mockRoutingContext.getBodyAsJson())
        .thenReturn(loginRequestJSONObject);

    // TODO: again, just confirming that this is what you guys want
    try {
      RestFunctions.getJsonBodyAsClass(mockRoutingContext, NewUserRequest.class);
      fail();
    } catch (RequestBodyMappingException ignored) {}
  }

  // test request header for existence
  @Test
  public void testGetRequestHeader1() {
    String myVal = "test";

    when(mockRequest.getHeader(anyString())).thenReturn(myVal);

    String result = RestFunctions.getRequestHeader(mockRequest, myVal);

    assertEquals(result, myVal);
  }

  // test request header for exception thrown
  @Test
  public void testGetRequestHeader2() {
    String name = "name";

    when(mockRequest.getHeader(name))
        .thenReturn(null);

    try {
      RestFunctions.getRequestHeader(mockRequest, name);
      fail();
    } catch (MissingHeaderException e) {
      assertEquals(name, e.getMissingHeaderName());
    }
  }

  // test request parameter as int when given nat
  @Test
  public void testGetRequestParameterAsInt1() {
    String myInt = "200";

    when(mockRequest.getParam(any())).thenReturn(myInt);

    int result = RestFunctions.getRequestParameterAsInt(mockRequest, myInt);
    int expectedResult = 200;

    assertEquals(result, expectedResult);
  }

  // test request parameter as int when given negative int
  @Test
  public void testGetRequestParameterAsInt2() {
    String myInt = "-200";

    when(mockRequest.getParam(any())).thenReturn(myInt);

    int result = RestFunctions.getRequestParameterAsInt(mockRequest, myInt);
    int expectedResult = -200;

    assertEquals(result, expectedResult);
  }

  // test request parameter exception when given malformed int
  @Test
  public void testGetRequestParameterAsInt3() {
    String myInt = "hello";

    when(mockRequest.getParam(any()))
        .thenReturn(myInt);

    try {
      RestFunctions.getRequestParameterAsInt(mockRequest, myInt);
      fail();
    } catch (MalformedParameterException e) {
      assertEquals(myInt, e.getParameterName());
    }
  }

  // test request parameter as string when given number
  @Test
  public void testGetRequestParameterAsString1() {
    String myInt = "200";

    when(mockRequest.getParam(any())).thenReturn(myInt);

    String result = RestFunctions.getRequestParameterAsString(mockRequest, myInt);

    assertEquals(result, myInt);
  }

  // test request parameter as string when given all letters
  @Test
  public void testGetRequestParameterAsString2() {
    String myString = "foobar";

    when(mockRequest.getParam(any())).thenReturn(myString);

    String result = RestFunctions.getRequestParameterAsString(mockRequest, myString);

    assertEquals(result, myString);
  }

  // test request parameter exception when parameter is missing
  @Test
  public void testGetRequestParameterAsString3() {
    String param = "param";

    when(mockRequest.getParam(param))
        .thenReturn(null);

    try {
      RestFunctions.getRequestParameterAsString(mockRequest, param);
      fail();
    } catch (MissingParameterException e) {
      assertEquals(param, e.getMissingParameterName());
    }
  }
}
