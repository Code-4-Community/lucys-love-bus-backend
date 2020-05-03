 package com.codeforcommunity.processor;

import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;
import com.codeforcommunity.api.IPfOperationsProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.dto.auth.NewUserAsPFRequest;
import com.codeforcommunity.dto.pfrequests.CreateRequest;
import com.codeforcommunity.dto.auth.NewUserRequest;
import com.codeforcommunity.dto.auth.SessionResponse;
	
public class PfOperationsProcessorImpl implements IPfOperationsProcessor {

	private final IAuthProcessor authProcessor;
	private final IRequestsProcessor requestProcessor;
	private final AuthDatabaseOperations authDatabaseOperations;

	public PfOperationsProcessorImpl(IAuthProcessor authProcessor, IRequestsProcessor requestProcessor,
		AuthDatabaseOperations authDatabaseOperations) {

		this.authProcessor = authProcessor;
		this.requestProcessor = requestProcessor;
		this.authDatabaseOperations = authDatabaseOperations;
	}

	//a sign up as pf request (as it stands now) is the union of signing up as a GP 
	//and making a request to be upgraded to PF
	@Override
	public SessionResponse signUpPF(NewUserAsPFRequest newUserPFRequest) {


		NewUserRequest gpUserRequest = newUserPFRequest.getNewUserRequest();

		CreateRequest gpToPFRequest = newUserPFRequest.getCreateRequest();

		//normal gp session response. GP account has been created
		SessionResponse gpSessionResponse = authProcessor.signUp(newUserPFRequest.getNewUserRequest());

		JWTData jwtData = authDatabaseOperations.getUserJWTData(gpUserRequest.getEmail());

		//make request to be upgraded
		requestProcessor.createRequest(gpToPFRequest, jwtData);

		return gpSessionResponse;
	}

}