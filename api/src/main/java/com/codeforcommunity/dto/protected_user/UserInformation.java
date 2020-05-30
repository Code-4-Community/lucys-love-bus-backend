package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import java.util.List;

public class UserInformation implements ApiDto {

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
  public void validate() {}
}
