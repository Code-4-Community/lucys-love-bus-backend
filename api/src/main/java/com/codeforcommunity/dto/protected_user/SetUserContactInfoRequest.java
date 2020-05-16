package com.codeforcommunity.dto.protected_user;

import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;

import java.util.List;

public class SetUserInfoRequest {

    private Contact mainContact;
    private List<Contact> additionalContacts;
    private List<Child> children;

    public SetUserInfoRequest(Contact mainContact, List<Contact> additionalContacts, List<Child> children) {
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

}
