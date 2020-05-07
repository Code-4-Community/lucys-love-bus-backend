package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;

public interface IProtectedUserProcessor {

  void deleteUser(JWTData userData);
}
