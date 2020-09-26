package com.codeforcommunity.dto.auth;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Representing the location (AddressData) portion of the Auth DTO
 */
public class AddressData extends ApiDto {

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

  @Override
  public List<String> validateFields(String fieldPrefix) {
    return new ArrayList<>();
  }

  /**
   * Retrieves the address field.
   *
   * @return the String address field.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets this AddressData's address field to the provided address.
   *
   * @param address the value to set this address field to.
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Retrieves the city field.
   *
   * @return the String city field.
   */
  public String getCity() {
    return city;
  }

  /**
   * Sets this AddressData's city field to the provided city.
   *
   * @param city the value to set this city field to.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Retrieves the state field.
   *
   * @return the String state field.
   */
  public String getState() {
    return state;
  }

  /**
   * Sets this AddressData's state field to the provided state.
   *
   * @param state the value to set this state field to.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Retrieves the zip code field.
   *
   * @return the String zip code field.
   */
  public String getZipCode() {
    return zipCode;
  }

  /**
   * Sets this AddressData's zip code field to the provided zip code.
   *
   * @param zipCode the value to set this zip code field to.
   */
  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }
}
