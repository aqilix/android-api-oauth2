package com.aqilix.mobile.aqilix.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by surya on 9/17/16.
 */
public class UserProfileModel {
    protected String uuid;
    protected String firstName;
    protected String lastName;
    protected Long dateOfBirth;
    protected String address;
    protected String city;
    protected String province;
    protected String postalCode;
    protected String country;
    protected String user;

    public UserProfileModel() {
    }

    public UserProfileModel(String UUID, String firstName, String lastName, Long dateOfBirth, String address,
                            String city, String province, String postalCode, String country, String user) {
        setUuid(UUID);
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
        setCity(city);
        setProvince(province);
        setPostalCode(postalCode);
        setCountry(country);
        setUser(user);
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(Long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getDateOfBirth() {
        return dateOfBirth;
    }

    public String getStringDateOfBirth(String pattern) {
        String result = null;
        if (dateOfBirth != null && dateOfBirth > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
            Date dob = new Date(dateOfBirth);
            result = formatter.format(dob);
        }
        return result;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getUser() {
        return user;
    }
}
