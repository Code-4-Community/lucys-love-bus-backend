package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import java.util.ArrayList;
import java.util.List;

/**
 * A request to set the main and additional contacts of a group of children.
 */
public class SetContactsAndChildrenRequest extends ApiDto {

  private Contact mainContact;
  private List<Contact> additionalContacts;
  private List<Child> children;

  /**
   * Creates a SetContactsAndChildrenRequest with no information (all fields are null)
   */
  public SetContactsAndChildrenRequest() {}

  /**
   * Creates a new SetContactsAndChildrenRequest with the given information
   * @param mainContact the main contact for the children
   * @param additionalContacts the list of additional contacts for the children
   * @param children the list of children
   */
  public SetContactsAndChildrenRequest(
      Contact mainContact, List<Contact> additionalContacts, List<Child> children) {
    this.mainContact = mainContact;
    this.additionalContacts = additionalContacts;
    this.children = children;
  }

  /**
   * Returns the main contact
   * @return the main contact
   */
  public Contact getMainContact() {
    return mainContact;
  }

  /**
   * Updates the main contact
   * @param mainContact the new main contact
   */
  public void setMainContact(Contact mainContact) {
    this.mainContact = mainContact;
  }

  /**
   * Returns the additional contacts
   * @return the additional contacts
   */
  public List<Contact> getAdditionalContacts() {
    return additionalContacts;
  }

  /**
   * Updates the additional contacts
   * @param additionalContacts the new additional contacts
   */
  public void setAdditionalContacts(List<Contact> additionalContacts) {
    this.additionalContacts = additionalContacts;
  }

  /**
   * Returns the list of children
   * @return the list of children
   */
  public List<Child> getChildren() {
    return children;
  }

  /**
   * Updates the list of children
   * @param children the new list of children
   */
  public void setChildren(List<Child> children) {
    this.children = children;
  }

  /**
   * Verify this SetContactsAndChildrenRequest is valid by checking the validity of the main
   * contact, additional contacts and children.
   * @param fieldPrefix A string to prefix each field with (for use if this is a sub-field). Should
   *     be of the form "OBJECT.".
   * @return a list of strings with each string indicating one field that is invalid
   */
  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "set_contacts_and_children_request.";
    List<String> fields = new ArrayList<>();
    if (mainContact == null) {
      fields.add(fieldPrefix + "main_contact");
    } else {
      fields.addAll(mainContact.validateFields(fieldPrefix));
    }
    if (children == null) {
      fields.add(fieldPrefix + "children");
    } else {
      for (Child child : children) {
        fields.addAll(child.validateFields(fieldPrefix));
      }
    }
    if (additionalContacts == null) {
      fields.add(fieldPrefix + "additional_contacts");
    } else {
      for (Contact ac : additionalContacts) {
        fields.addAll(ac.validateFields(fieldPrefix));
      }
    }
    return fields;
  }
}
