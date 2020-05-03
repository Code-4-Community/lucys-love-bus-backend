package com.codeforcommunity.dto.auth;

import com.codeforcommunity.dto.auth.NewUserRequest;
import com.codeforcommunity.dto.pfrequests.CreateRequest;

public class NewUserAsPFRequest {

	private NewUserRequest newUserRequest;
	private CreateRequest createRequest;

	public void setNewUserRequest(NewUserRequest newUserRequest) {
		this.newUserRequest = newUserRequest;
	}

	public NewUserRequest getNewUserRequest() {
		return this.newUserRequest;
	}

	public void setCreateRequest(CreateRequest createRequest) {
		this.createRequest = createRequest;
	}

	public CreateRequest getCreateRequest() {
		return this.createRequest;
	}

}

