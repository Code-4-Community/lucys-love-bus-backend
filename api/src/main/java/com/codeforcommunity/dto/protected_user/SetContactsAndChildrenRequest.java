package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import java.util.ArrayList;
import java.util.List;

public class SetContactsAndChildrenRequest extends ApiDto {

  private Contact mainContact;
  private List<Contact> additionalContacts;
  private List<Child> children;

  public SetContactsAndChildrenRequest() {}

  public SetContactsAndChildrenRequest(
      Contact mainContact, List<Contact> additionalContacts, List<Child> children) {
    this.mainContact = mainContact;
    this.additionalContacts = additionalContacts;
    this.children = children;
  }

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

  @Override
  public List<String> validateFields(String fieldPrefx) {
    String fieldName = fieldPrefx + "set_contacts_and_children_request.";
    List<String> fields = new ArrayList<>();
    if (mainContact == null) {
      fields.add(fieldPrefx + "main_contact");
    } else {
      fields.addAll(mainContact.validateFields(fieldPrefx));
    }
    if (children == null) {
      fields.add(fieldPrefx + "children");
    } else {
      for (Child child : children) {
        fields.addAll(child.validateFields(fieldPrefx));
      }
    }
    if (additionalContacts == null) {
      fields.add(fieldPrefx + "additional_contacts");
    } else {
      for (Contact ac : additionalContacts) {
        fields.addAll(ac.validateFields(fieldPrefx));
      }
    }
    return fields;
  }
}
