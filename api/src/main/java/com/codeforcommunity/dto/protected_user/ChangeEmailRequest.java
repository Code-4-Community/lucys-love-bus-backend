package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class ChangeEmailRequest extends ApiDto {

  private String newEmail;
  private String password;

  public ChangeEmailRequest(String newEmail, String password) {
    this.newEmail = newEmail;
    this.password = password;
  }

  private ChangeEmailRequest() {}

  public String getNewEmail() {
    return newEmail;
  }

  public void setNewEmail(String newEmail) {
    this.newEmail = newEmail;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (emailInvalid(newEmail)) {
      fields.add(fieldPrefix + "new_email");
    }
    if (password == null) {
      fields.add(fieldPrefix + "password");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "change_email_request.";
  }
}
