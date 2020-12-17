package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.PF_REQUESTS;
import static org.jooq.generated.Tables.USERS;

import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.dto.pfrequests.RequestStatusData;
import com.codeforcommunity.dto.pfrequests.RequestUser;
import com.codeforcommunity.dto.protected_user.UserInformation;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.enums.RequestStatus;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.OutstandingRequestException;
import com.codeforcommunity.exceptions.RequestDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.codeforcommunity.requester.Emailer;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Users;
import org.jooq.generated.tables.records.PfRequestsRecord;

public class RequestsProcessorImpl implements IRequestsProcessor {
  private final DSLContext db;
  private final Emailer emailer;
  private final AuthDatabaseOperations authDatabaseOperations;

  public RequestsProcessorImpl(DSLContext db, Emailer emailer) {
    this.db = db;
    this.emailer = emailer;
    this.authDatabaseOperations = new AuthDatabaseOperations(db);
  }

  @Override
  public void createRequest(JWTData userData) {
    // Check that this user is a GP
    // Check that this user doesn't have outstanding requests
    // Add request to database
    // Sends an email to all administrators

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
    newRecord.store();

    emailer.sendEmailToAllAdministrators(emailer::sendParticipatingFamilyRequestNotification);
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
        db.select(
                PF_REQUESTS.ID,
                PF_REQUESTS.USER_ID,
                CONTACTS.EMAIL,
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.PHONE_NUMBER)
            .from(PF_REQUESTS)
            .join(CONTACTS)
            .on(PF_REQUESTS.USER_ID.eq(CONTACTS.USER_ID))
            .where(CONTACTS.IS_MAIN_CONTACT.isTrue())
            .and(PF_REQUESTS.STATUS.eq(RequestStatus.PENDING))
            .fetch()
            .map(
                r ->
                    new RequestData(
                        r.component1(),
                        new RequestUser(
                            r.component2(),
                            r.component3(),
                            r.component4(),
                            r.component5(),
                            r.component6())));

    return outstandingRequests;
  }

  @Override
  public UserInformation getRequestData(int requestId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    Users user =
        db.select(USERS.fields())
            .from(PF_REQUESTS.join(USERS).onKey())
            .where(PF_REQUESTS.ID.eq(requestId))
            .fetchOneInto(Users.class);

    if (user == null) {
      throw new RequestDoesNotExistException(requestId);
    }

    return authDatabaseOperations.getUserInformation(user);
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

    if (requestsRecord == null) {
      throw new RequestDoesNotExistException(requestId);
    }

    requestsRecord.setStatus(RequestStatus.APPROVED);
    requestsRecord.store(PF_REQUESTS.STATUS);

    emailer.sendEmailToAllContacts(requestsRecord.getUserId(), emailer::sendRequestApproved);

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

    if (requestsRecord == null) {
      throw new RequestDoesNotExistException(requestId);
    }

    emailer.sendEmailToAllContacts(requestsRecord.getUserId(), emailer::sendRequestDenied);

    requestsRecord.setStatus(RequestStatus.REJECTED);
    requestsRecord.store(PF_REQUESTS.STATUS);
  }

  @Override
  public List<RequestStatusData> getRequestStatuses(JWTData userData) {
    List<RequestStatusData> requestStatuses =
        db.select(PF_REQUESTS.ID, PF_REQUESTS.STATUS, PF_REQUESTS.CREATED)
            .from(PF_REQUESTS)
            .where(PF_REQUESTS.USER_ID.eq(userData.getUserId()))
            .orderBy(PF_REQUESTS.CREATED.desc())
            .fetchInto(RequestStatusData.class);

    return requestStatuses;
  }
}
