package com.visitorpass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PreApprovalRequest {

    @NotNull(message = "Resident ID is required")
    private Long residentId;

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    private String visitorPhone;

    @NotBlank(message = "Visitor email is required for OTP delivery")
    @Email(message = "Please provide a valid email address")
    private String visitorEmail;

    @NotNull(message = "Expected visit date and time is required")
    private LocalDateTime expectedVisitDateTime;

    private Integer durationMinutes = 120; // default 2 hours visit window

    @NotBlank(message = "Purpose of visit is required")
    private String purpose;

    public PreApprovalRequest() {
    }

    public PreApprovalRequest(Long residentId, String visitorName, String visitorPhone,
                              String visitorEmail, LocalDateTime expectedVisitDateTime,
                              Integer durationMinutes, String purpose) {
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.visitorPhone = visitorPhone;
        this.visitorEmail = visitorEmail;
        this.expectedVisitDateTime = expectedVisitDateTime;
        this.durationMinutes = durationMinutes;
        this.purpose = purpose;
    }

    public Long getResidentId() {
        return residentId;
    }

    public void setResidentId(Long residentId) {
        this.residentId = residentId;
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

    public String getVisitorEmail() {
        return visitorEmail;
    }

    public void setVisitorEmail(String visitorEmail) {
        this.visitorEmail = visitorEmail;
    }

    public LocalDateTime getExpectedVisitDateTime() {
        return expectedVisitDateTime;
    }

    public void setExpectedVisitDateTime(LocalDateTime expectedVisitDateTime) {
        this.expectedVisitDateTime = expectedVisitDateTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
