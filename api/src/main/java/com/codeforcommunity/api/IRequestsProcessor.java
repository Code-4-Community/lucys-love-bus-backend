package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.dto.pfrequests.RequestStatusData;
import com.codeforcommunity.dto.protected_user.UserInformation;
import java.util.List;

public interface IRequestsProcessor {
  void createRequest(JWTData userData);

  List<RequestData> getRequests(JWTData userData);

  UserInformation getRequestData(int requestId, JWTData userData);

  void approveRequest(int requestId, JWTData userData);

  void rejectRequest(int requestId, JWTData userData);

  List<RequestStatusData> getRequestStatuses(JWTData userData);
}
