 package com.codeforcommunity.processor;

import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;

public class PfOperationsProcessorImpl implements IPfOperationsProcessor {

	private final IAuthProcessor authProcessor;
	private final IRequestProcessor requestProcessor;
	private final AuthDatabaseOperations authDatabaseOperations;

	public PfOperationsProcessorImpl(IAuthProcessor authProcessor, IRequestProcessor requestProcessor,
		AuthDatabaseOperations authDatabaseOperations) {

		this.authProcessor = authProcessor;
		this.requestProcessor = requestProcessor;
		this.authDatabaseOperations = authDatabaseOperations;
	}

	//a sign up as pf request (as it stands now) is the union of signing up as a GP 
	//and making a request to be upgraded to PF
	@Override
	public SessionResponse signUpPF(NewUserAsPFRequest newUserAsPFRequest) {

		//signs user up as normal GP
		NewUserRequest gpUserRequest = newUserPFRequest.getNewUserRequest();
		//gets description body from PF request
		CreateRequest gpToPFRequest = newUserPFRequest.getCreateRequest();

		//normal gp session response. GP account has been created
		SessionResponse gpSessionResponse = authProcessor.signUp(newUserPFRequest.getNewUserRequest());

		JWTData jwtData = authDatabaseOperations.getUsersJWTData(gpUserRequest.getEmail());

		//make request to be upgraded
		requestProcessor.createRequest(gpToPFRequest, jwtData);

}