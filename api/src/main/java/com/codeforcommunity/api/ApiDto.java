package com.codeforcommunity.api;

import com.codeforcommunity.exceptions.HandledException;

public interface ApiDto {

  /** Verify if this DTO is a valid object. */
  void validate() throws HandledException;
}
