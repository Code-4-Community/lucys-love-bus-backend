package com.codeforcommunity.rest;

import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.exceptions.MissingHeaderException;
import com.codeforcommunity.exceptions.MissingParameterException;
import com.codeforcommunity.exceptions.RequestBodyMappingException;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface RestFunctions {

  /**
   * Gets the JSON body from the given routing context and parses it into the given class.
   * @throws RequestBodyMappingException if the given request cannot be successfully mapped
   *      into the given class.
   * @throws RequestBodyMappingException if the given request does not have a body that can be
   *      parsed.
   */
  static <T> T getJsonBodyAsClass(RoutingContext ctx, Class<T> clazz) {
    Optional<JsonObject> body = Optional.ofNullable(ctx.getBodyAsJson());
    if (body.isPresent()) {
      try {
        return body.get().mapTo(clazz);
      } catch (IllegalArgumentException e) {
        throw new RequestBodyMappingException();
      }
    } else {
      throw new RequestBodyMappingException();
    }
  }

  static String getRequestHeader(HttpServerRequest req, String name) {
    String headerValue = req.getHeader(name);
    if (headerValue != null) {
      return headerValue;
    }
    throw new MissingHeaderException(name);
  }

  static int getPathParameterAsInt(RoutingContext ctx, String name) {
    String paramValue = ctx.pathParam(name);
    if (paramValue != null) {
      try {
        return Integer.parseInt(paramValue);
      } catch (NumberFormatException nex) {
        throw new MalformedParameterException(name);
      }
    }
    else {
      throw new MissingParameterException(name);
    }
  }

  static String getRequestParameterAsString(HttpServerRequest req, String name) {
    String paramValue = req.getParam(name);
    if (paramValue != null) {
      return paramValue;
    }
    throw new MissingParameterException(name);
  }

  static <T> Optional<T> getNullableQueryParam(RoutingContext ctx, String name,
                                                  Function<String, T> mapper) {
    Optional<String> paramValue = Optional.ofNullable(ctx.queryParam(name).get(0));
    T returnValue;
    if(paramValue.isPresent()) {
      try {
        returnValue = mapper.apply(paramValue.get());
      } catch (Throwable t) {
        throw new MalformedParameterException(name);
      }
    }
    else {
      returnValue = null;
    }
    return Optional.ofNullable(returnValue);
  }

  static Function<String, Integer> getCountParamMapper() {
    return Integer::parseInt;
  }

  static Function<String, Timestamp> getDateParamMapper() {
    return Timestamp::valueOf;
  }

  //todo if string format in spec lines up with contructor for timestamp


  //todo unit test this class



}
