package com.codeforcommunity.api;

import com.codeforcommunity.dto.auth.NewUserAsPFRequest;
import com.codeforcommunity.dto.auth.SessionResponse;

public interface IPfOperationsProcessor {

	SessionResponse signUpPF(NewUserAsPFRequest newUserAsPFRequest);

}