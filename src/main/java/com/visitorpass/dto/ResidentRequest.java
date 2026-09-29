package com.visitorpass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResidentRequest {

    @NotBlank(message = "Resident name is required")
    private String name;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Please provide a valid email address")
    private String email;

    public ResidentRequest() {
    }

    public ResidentRequest(String name, String flatNumber, String phoneNumber, String email) {
        this.name = name;
        this.flatNumber = flatNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
