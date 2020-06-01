package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import java.util.ArrayList;
import java.util.List;

public class UserInformation extends ApiDto {

  private Contact mainContact;
  private List<Contact> additionalContacts;
  private List<Child> children;
  private AddressData location;

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

  private UserInformation() {}

  public Contact getMainContact() {
    return mainContact;
  }

  public void setMainContact(Contact mainContact) {
    this.mainContact = mainContact;
  }

  public List<Contact> getAdditionalContacts() {
    return additionalContacts;
  }

  public void setAdditionalContacts(List<Contact> additionalContacts) {
    this.additionalContacts = additionalContacts;
  }

  public List<Child> getChildren() {
    return children;
  }

  public void setChildren(List<Child> children) {
    this.children = children;
  }

  public AddressData getLocation() {
    return location;
  }

  public void setLocation(AddressData location) {
    this.location = location;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (mainContact == null) {
      fields.add(fieldPrefix + "main_contact");
    } else {
      fields.addAll(mainContact.validateFields(fieldPrefix + mainContact.fieldName()));
    }
    if (location == null) {
      fields.add(fieldPrefix + "location");
    } else {
      fields.addAll(location.validateFields(fieldPrefix + location.fieldName()));
    }
    if (additionalContacts == null) {
      fields.add(fieldPrefix + "additional_contacts");
    } else {
      for (Contact contact : additionalContacts) {
        fields.addAll(contact.validateFields(fieldPrefix + contact.fieldName()));
      }
    }
    if (children == null) {
      fields.add(fieldPrefix + "children");
    } else {
      for (Child child : children) {
        fields.addAll(child.validateFields(fieldPrefix + child.fieldName()));
      }
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "user_information.";
  }
}
