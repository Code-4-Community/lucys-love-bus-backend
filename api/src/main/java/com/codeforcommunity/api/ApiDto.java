package com.codeforcommunity.api;

import com.codeforcommunity.exceptions.HandledException;
import com.codeforcommunity.exceptions.MalformedParameterException;
import java.util.List;

public abstract class ApiDto {

  /**
   * Verify if the extending DTO is a valid object.
   *
   * @return A list of strings containing the fields that are invalid. Return a non-null an empty
   *     list if all fields are valid.
   */
  private final List<String> validateFields() {
    return validateFields(this.fieldName());
  }

  /**
   * Verify if the extending DTO is a valid object.
   *
   * @param fieldPrefix A string to prefix each field with (for use if this is a sub-field). Should
   *     be of the form "OBJECT.".
   * @return A list of strings containing the fields that are invalid. Return a non-null empty list
   *     if all fields are valid.
   */
  public abstract List<String> validateFields(String fieldPrefix);

  /**
   * Verify if the extending DTO is a valid object. This version should be overridden if this object
   * has sometimes-optional fields. For an example, see {@link
   * com.codeforcommunity.dto.userEvents.components.EventDetails}.
   *
   * @param fieldPrefix A string to prefix each field with 9for use if this is a sub-field). Should
   *     be of the form "OBJECT.".
   * @param nullable a boolean representing whether this is the nullable version of an object with
   *     sometimes-optional fields.
   * @return A list of strings containing the fields that are invalid. Return a non-null empty list
   *     if all fields are valid.
   */
  public List<String> validateFields(String fieldPrefix, boolean nullable) {
    return validateFields(fieldPrefix);
  }

  /**
   * Validate the extending DTO. Calls validateFields, joins the list of fields, and throws a {@link
   * HandledException} containing the field(s) that caused the issue. Can be overridden if another
   * {@link HandledException} should be thrown.
   *
   * @throws HandledException Containing the error fields.
   */
  public void validate() throws HandledException {
    List<String> fields = this.validateFields();
    if (fields == null) {
      throw new IllegalStateException("Field validation cannot return null value.");
    }
    if (fields.size() == 0) {
      return;
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < fields.size(); i++) {
      builder.append(fields.get(i));
      if (i < fields.size() - 1) {
        builder.append(", ");
      }
    }
    throw new MalformedParameterException(builder.toString());
  }

  /**
   * Checks to see if email is valid.
   *
   * @param email the email to check.
   * @return a boolean representing whether this email is not valid.
   */
  public boolean emailInvalid(String email) {
    return email == null || email.matches("^/S+@/S+\\./S+$");
  }

  /**
   * Checks to see if the given string is empty.
   *
   * @param str the string to check.
   * @return a boolean representing whether this string is empty.
   */
  public boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * Returns the name of the field. Used to start validate with a field prefix.
   *
   * @return A string representing the name of the field.
   */
  public abstract String fieldName();
}
