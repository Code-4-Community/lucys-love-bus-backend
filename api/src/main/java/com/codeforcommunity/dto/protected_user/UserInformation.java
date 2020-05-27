package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.enums.PrivilegeLevel;
import java.util.List;

public class UserInformation {

  private Contact mainContact;
  private List<Contact> additionalContacts;
  private List<Child> children;
  private AddressData location;
  private PrivilegeLevel accountType;

  public UserInformation(
      Contact mainContact,
      List<Contact> additionalContacts,
      List<Child> children,
      AddressData location,
      PrivilegeLevel accountType) {
    this.mainContact = mainContact;
    this.additionalContacts = additionalContacts;
    this.children = children;
    this.location = location;
    this.accountType = accountType;
  }

  public Contact getMainContact() {
    return mainContact;
  }

  public List<Contact> getAdditionalContacts() {
    return additionalContacts;
  }

  public List<Child> getChildren() {
    return children;
  }

  public AddressData getLocation() {
    return location;
  }

  public PrivilegeLevel getAccountType() {
    return accountType;
  }
}
