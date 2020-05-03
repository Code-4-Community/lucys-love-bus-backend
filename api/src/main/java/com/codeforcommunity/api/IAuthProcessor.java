package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.auth.LoginRequest;
import com.codeforcommunity.dto.auth.NewUserRequest;
import com.codeforcommunity.dto.auth.RefreshSessionRequest;
import com.codeforcommunity.dto.auth.RefreshSessionResponse;
import com.codeforcommunity.dto.auth.SessionResponse;
import com.codeforcommunity.exceptions.AuthException;
import com.codeforcommunity.exceptions.ExpiredEmailVerificationTokenException;
import com.codeforcommunity.exceptions.InvalidEmailVerificationTokenException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;

public interface IAuthProcessor {

    /**
     * Creates a new user to be known to our application.
     * @param request request object containing new user information.
     */
    SessionResponse signUp(NewUserRequest request) throws AuthException;

    /**
     * Given a LoginRequest log the user in if they're valid and return access and
     * refresh tokens for their session.
     *
     * @throws AuthException If the given email / password combination is invalid
     */
    SessionResponse login(LoginRequest loginRequest) throws AuthException;

    /**
     * Logs the user out by adding the given refresh token to the blacklist so that
     * it cannot be used for future refreshes.
     */
    void logout(String refreshToken);

    /**
     * Allows clients to refresh session and receive access token using given refresh token.
     * @param request request object containing refresh token as well as needed user information.
     * @return response object containing new access token to be passed to client.
     * @throws AuthException if given refresh token is invalid.
     */
    RefreshSessionResponse refreshSession(RefreshSessionRequest request) throws AuthException;

    /**
     * Allows clients to submit a secret key in order to verify their email.
     * @param secretKey string of user's verificaiton token.
     * @throws ExpiredEmailVerificationTokenException if the token is expired.
     * @throws InvalidEmailVerificationTokenException if the token is invalid.
     */
    void validateSecretKey(String secretKey);

    /**
     * Get's a users privilege level and id as a JWTData object.
     * @param email address associated with user to retrieve data for.
     * @return JWTData object containing userId and privilege level.
     */
    JWTData getUserJWTData(String email);
}
