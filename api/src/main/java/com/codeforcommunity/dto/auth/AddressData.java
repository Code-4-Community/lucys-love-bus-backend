package com.codeforcommunity.dto.auth;

public class AddressData {

  private String address;
  private String city;
  private String state;
  private String zipCode;

  public AddressData(String address, String city, String state, String zipCode) {
    this.address = address;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
  }

  private AddressData() {}

  public boolean validate() {
    return address != null && city != null && state != null && zipCode != null;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }
}
