package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import java.util.ArrayList;
import java.util.List;

/** A representation of a user's information. */
// TODO: what kind of a user does this information belong to?
public class UserInformation extends ApiDto {

  private Contact mainContact;
  private List<Contact> additionalContacts;
  private List<Child> children;
  private AddressData location;

  /**
   * Creates a new UserInformation object with the given fields.
   *
   * @param mainContact the main contact
   * @param additionalContacts the list of additional contacts
   * @param children the list of children
   * @param location the location of the user
   */
  public UserInformation(
      Contact mainContact,
      List<Contact> additionalContacts,
      List<Child> children,
      AddressData location) {
    this.mainContact = mainContact;
    this.additionalContacts = additionalContacts;
    this.children = children;
    this.location = location;
  }

  /** Creates a UserInformation with no information (all fields are null) */
  private UserInformation() {}

  /**
   * Returns the main contact
   *
   * @return the main contact
   */
  public Contact getMainContact() {
    return mainContact;
  }

  /**
   * Updates the main contact
   *
   * @param mainContact the new main contact
   */
  public void setMainContact(Contact mainContact) {
    this.mainContact = mainContact;
  }

  /**
   * Returns the list of additional contacts
   *
   * @return the list of additional contacts
   */
  public List<Contact> getAdditionalContacts() {
    return additionalContacts;
  }

  /**
   * Updates the list of additional contacts
   *
   * @param additionalContacts the new list of additional contacts
   */
  public void setAdditionalContacts(List<Contact> additionalContacts) {
    this.additionalContacts = additionalContacts;
  }

  /**
   * Returns the list of children
   *
   * @return the list of children
   */
  public List<Child> getChildren() {
    return children;
  }

  /**
   * Updates the list of children
   *
   * @param children the new list of children
   */
  public void setChildren(List<Child> children) {
    this.children = children;
  }

  /**
   * Returns this user's location
   *
   * @return this user's location
   */
  public AddressData getLocation() {
    return location;
  }

  /**
   * Updates this user's location
   *
   * @param location the new location
   */
  public void setLocation(AddressData location) {
    this.location = location;
  }

  /**
   * Verify this UserInformation is valid by checking the validity of the main contact, additional
   * contacts and children.
   *
   * @param fieldPrefix A string to prefix each field with (for use if this is a sub-field). Should
   *     be of the form "OBJECT.".
   * @return a list of strings with each string indicating one field that is invalid
   */
  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "user_information.";
    List<String> fields = new ArrayList<>();
    if (mainContact == null) {
      fields.add(fieldName + "main_contact");
    } else {
      fields.addAll(mainContact.validateFields(fieldName));
    }
    if (location == null) {
      fields.add(fieldName + "location");
    } else {
      fields.addAll(location.validateFields(fieldName));
    }
    if (additionalContacts == null) {
      fields.add(fieldName + "additional_contacts");
    } else {
      for (Contact contact : additionalContacts) {
        fields.addAll(contact.validateFields(fieldName));
      }
    }
    if (children == null) {
      fields.add(fieldName + "children");
    } else {
      for (Child child : children) {
        fields.addAll(child.validateFields(fieldName));
      }
    }
    return fields;
  }
}
