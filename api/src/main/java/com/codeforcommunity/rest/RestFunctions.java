package com.codeforcommunity.rest;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.exceptions.MissingHeaderException;
import com.codeforcommunity.exceptions.MissingParameterException;
import com.codeforcommunity.exceptions.RequestBodyMappingException;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface RestFunctions {

  /**
   * Gets the JSON body from the given routing context and parses it into the given class.
   *
   * @throws RequestBodyMappingException if the given request cannot be successfully mapped into the
   *     given class.
   * @throws RequestBodyMappingException if the given request does not have a body that can be
   *     parsed.
   */
  static <T extends ApiDto> T getJsonBodyAsClass(RoutingContext ctx, Class<T> clazz) {
    Optional<JsonObject> body = Optional.ofNullable(ctx.getBodyAsJson());
    if (body.isPresent()) {
      try {
        T value = body.get().mapTo(clazz);
        value.validate();
        return value;
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        throw new RequestBodyMappingException();
      }
    } else {
      throw new RequestBodyMappingException();
    }
  }

  static String getRequestHeader(HttpServerRequest req, String name) {
    String headerValue = req.getHeader(name);
    if (headerValue != null && !headerValue.isEmpty()) {
      return headerValue;
    }
    throw new MissingHeaderException(name);
  }

  static int getRequestParameterAsInt(HttpServerRequest req, String name) {
    String paramValue = getRequestParameterAsString(req, name);
    try {
      return Integer.parseInt(paramValue);
    } catch (NumberFormatException ex) {
      throw new MalformedParameterException(name);
    }
  }

  static String getRequestParameterAsString(HttpServerRequest req, String name) {
    String paramValue = req.getParam(name);
    if (paramValue != null && !paramValue.isEmpty()) {
      return paramValue;
    }
    throw new MissingParameterException(name);
  }

  /**
   * Get's a query parameter that may or may not be there as an optional of the desired type.
   * Attempts to map the query parameter from a string to an instance of the desired type.
   *
   * @param ctx routing context to retrieve query param from.
   * @param name of query param.
   * @param mapper a function that maps the query param from string to desired type.
   * @param <T> the desired type.
   * @return An optional object of the query param as it's desired type.
   */
  static <T> Optional<T> getOptionalQueryParam(
      RoutingContext ctx, String name, Function<String, T> mapper) {
    List<String> params = ctx.queryParam(name);
    T returnValue;
    if (!params.isEmpty()) {
      try {
        returnValue = mapper.apply(params.get(0));
      } catch (Throwable t) {
        throw new MalformedParameterException(name);
      }
    } else {
      returnValue = null;
    }
    return Optional.ofNullable(returnValue);
  }

  /**
   * Get's List of query parameters associated with given name. Attempts to map each parameter to
   * the desired type. Only returns the values that can be mapped.
   *
   * @param ctx routing context to retrieve query param from.
   * @param name of query param.
   * @param mapper a function that maps the query param from string to desired type.
   * @param <T> the desired type.
   * @return A list of the desired type of all the values of the query param that could successfully
   *     be mapped.
   */
  static <T> List<T> getMultipleQueryParams(
      RoutingContext ctx, String name, Function<String, T> mapper) {
    List<String> queryParam = ctx.queryParam(name);
    return queryParam.stream().map(mapper).collect(Collectors.toList());
  }
}
