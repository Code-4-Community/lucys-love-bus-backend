package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.dto.protected_user.components.UserSummary;
import java.util.List;

/** Represents the action of getting all user summaries for the user directory */
public class GetAllUserInfoResponse {

  private List<UserSummary> users;

  /**
   * Creates a GetAllUserInfoResponse object with a list of UserSummaries
   *
   * @param users a list of UserSummary
   */
  public GetAllUserInfoResponse(List<UserSummary> users) {
    this.users = users;
  }

  /**
   * Gets the list of UserSummary of this GetAllUserInfoResponse
   *
   * @return the list of UserSummary of this GetAllUserInfoResponse
   */
  public List<UserSummary> getUsers() {
    return users;
  }
}
