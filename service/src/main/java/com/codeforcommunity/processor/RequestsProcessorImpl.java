package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.PF_REQUESTS;
import static org.jooq.generated.Tables.USERS;

import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.pfrequests.CreateRequest;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.enums.RequestStatus;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.OutstandingRequestException;
import com.codeforcommunity.exceptions.ResourceNotOwnedException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.PfRequests;
import org.jooq.generated.tables.pojos.Users;
import org.jooq.generated.tables.records.PfRequestsRecord;

public class RequestsProcessorImpl implements IRequestsProcessor {
  private DSLContext db;

  public RequestsProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public void createRequest(CreateRequest requestData, JWTData userData) {
    // Check that this user is a GP
    // Check that this user doesn't have outstanding requests
    // Add request to database

    if (userData.getPrivilegeLevel() != PrivilegeLevel.GP) {
      throw new WrongPrivilegeException(PrivilegeLevel.GP);
    }
    List<RequestStatus> pastRequests =
        db.selectFrom(PF_REQUESTS)
            .where(PF_REQUESTS.USER_ID.eq(userData.getUserId()))
            .fetch(PF_REQUESTS.STATUS);

    if (pastRequests.stream().anyMatch(s -> s.equals(RequestStatus.PENDING))) {
      throw new OutstandingRequestException();
    }

    PfRequestsRecord newRecord = db.newRecord(PF_REQUESTS);
    newRecord.setUserId(userData.getUserId());
    newRecord.setStatus(RequestStatus.PENDING);
    newRecord.setDescription(requestData.getDescription());
    newRecord.store();
  }

  @Override
  public List<RequestData> getRequests(JWTData userData) {
    // Check that this user is an Admin
    // Get rows from database
    // Map into DTO

    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    List<RequestData> outstandingRequests =
        db.select(PF_REQUESTS.ID, PF_REQUESTS.DESCRIPTION, USERS.EMAIL)
            .from(PF_REQUESTS)
            .join(USERS)
            .onKey()
            .where(PF_REQUESTS.STATUS.eq(RequestStatus.PENDING))
            .fetch()
            .map(r -> new RequestData(r.component1(), r.component2(), r.component3()));

    return outstandingRequests;
  }

  @Override
  public void approveRequest(int requestId, JWTData userData) {
    // Check that this user is an Admin
    // Get request users id
    // Update request status
    // Update request users privilege

    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    PfRequestsRecord requestsRecord =
        db.selectFrom(PF_REQUESTS).where(PF_REQUESTS.ID.eq(requestId)).fetchOne();

    requestsRecord.setStatus(RequestStatus.APPROVED);
    requestsRecord.store(PF_REQUESTS.STATUS);

    db.update(USERS)
        .set(USERS.PRIVILEGE_LEVEL, PrivilegeLevel.PF)
        .where(USERS.ID.eq(requestsRecord.getUserId()))
        .execute();
  }

  @Override
  public void rejectRequest(int requestId, JWTData userData) {
    // Check that this user is an Admin
    // Get request users id
    // Update request status

    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    PfRequestsRecord requestsRecord =
        db.selectFrom(PF_REQUESTS).where(PF_REQUESTS.ID.eq(requestId)).fetchOne();

    requestsRecord.setStatus(RequestStatus.REJECTED);
    requestsRecord.store(PF_REQUESTS.STATUS);
  }

  @Override
  public RequestStatus getRequestStatus(int requestId, JWTData userData) {
    // Get requests user id
    // Check that this user is an Admin or the same user

    PfRequests request =
        db.selectFrom(PF_REQUESTS)
            .where(PF_REQUESTS.ID.eq(requestId))
            .fetchOneInto(PfRequests.class);

    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      Users requestUser =
          db.selectFrom(USERS).where(USERS.ID.eq(request.getUserId())).fetchOneInto(Users.class);

      if (!requestUser.getId().equals(userData.getUserId())) {
        throw new ResourceNotOwnedException("request " + requestId);
      }
    }

    return request.getStatus();
  }
}
