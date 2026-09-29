package com.visitorpass.dto;

import java.time.LocalDateTime;

public class OTPVerifyResponse {

    private boolean valid;
    private String status; // GRANTED or DENIED
    private String message;
    private String visitorName;
    private String visitorPhone;
    private String residentName;
    private String flatNumber;
    private String purpose;
    private LocalDateTime entryTime;
    private Long approvalId;

    public OTPVerifyResponse() {
    }

    public static OTPVerifyResponse granted(String message, String visitorName, String visitorPhone,
                                            String residentName, String flatNumber, String purpose,
                                            LocalDateTime entryTime, Long approvalId) {
        OTPVerifyResponse response = new OTPVerifyResponse();
        response.valid = true;
        response.status = "GRANTED";
        response.message = message;
        response.visitorName = visitorName;
        response.visitorPhone = visitorPhone;
        response.residentName = residentName;
        response.flatNumber = flatNumber;
        response.purpose = purpose;
        response.entryTime = entryTime;
        response.approvalId = approvalId;
        return response;
    }

    public static OTPVerifyResponse denied(String message) {
        OTPVerifyResponse response = new OTPVerifyResponse();
        response.valid = false;
        response.status = "DENIED";
        response.message = message;
        return response;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorPhone() {
        return visitorPhone;
    }

    public void setVisitorPhone(String visitorPhone) {
        this.visitorPhone = visitorPhone;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
    }
}
