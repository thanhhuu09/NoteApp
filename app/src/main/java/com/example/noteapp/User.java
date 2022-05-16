package com.example.noteapp;

import java.io.Serializable;

public class User implements Serializable{
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean OTPChecked;

    public User(String name, String email, String phoneNumber, Boolean OTPChecked) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.OTPChecked = OTPChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getOTPChecked() {
        return OTPChecked;
    }

    public void setOTPChecked(Boolean OTPChecked) {
        this.OTPChecked = OTPChecked;
    }
}
