package com.visitorpass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OTPVerifyRequest {

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit numeric code")
    private String otpCode;

    public OTPVerifyRequest() {
    }

    public OTPVerifyRequest(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
