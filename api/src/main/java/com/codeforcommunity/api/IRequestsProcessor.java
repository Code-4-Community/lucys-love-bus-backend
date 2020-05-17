package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.pfrequests.CreateRequest;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.enums.RequestStatus;
import java.util.List;

public interface IRequestsProcessor {
  void createRequest(CreateRequest requestData, JWTData userData);

  List<RequestData> getRequests(JWTData userData);

  void approveRequest(int requestId, JWTData userData);

  void rejectRequest(int requestId, JWTData userData);

  RequestStatus getRequestStatus(int requestId, JWTData userData);
}
