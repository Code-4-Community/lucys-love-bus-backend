package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * A request by a user to change the email address to which emails are sent.
 */
public class ChangeEmailRequest extends ApiDto {

  private String newEmail;
  private String password;

  /**
   * Creates a new ChangeEmailRequest where the user is requesting their email to be changed to
   * {@code newEmail} and the password they have entered is {@code password}.
   * @param newEmail the new email address
   * @param password the password entered by the user
   */
  public ChangeEmailRequest(String newEmail, String password) {
    this.newEmail = newEmail;
    this.password = password;
  }

  /**
   * Creates a ChangeEmailRequest with no information.
   */
  private ChangeEmailRequest() {}

  /**
   * Returns the new email address.
   * @return the new email address
   */
  public String getNewEmail() {
    return newEmail;
  }

  /**
   * Updates the new email address.
   * @param newEmail the new email address
   */
  public void setNewEmail(String newEmail) {
    this.newEmail = newEmail;
  }

  /**
   * Returns the password entered by the user.
   * @return the password entered by the user
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password entered by the user
   * @param password the new password entered by the user
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Verify this ChangeEmailRequest is valid by checking the validity of the email and the password.
   * @param fieldPrefix A string to prefix each field with (for use if this is a sub-field). Should
   *     be of the form "OBJECT.".
   * @return a list of strings with each string indicating one field that is invalid
   */
  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "change_email_request.";
    List<String> fields = new ArrayList<>();
    if (emailInvalid(newEmail)) {
      fields.add(fieldName + "new_email");
    }
    if (password == null) {
      fields.add(fieldName + "password");
    }
    return fields;
  }
}
