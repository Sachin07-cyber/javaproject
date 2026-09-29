package com.visitorpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_pre_approval")
public class VisitorPreApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Visitor name is required")
    @Column(nullable = false)
    private String visitorName;

    private String visitorPhone;

    @NotBlank(message = "Visitor email is required for OTP delivery")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false)
    private String email;

    @NotNull(message = "Expected visit date and time is required")
    @Column(nullable = false)
    private LocalDateTime expectedVisitDateTime;

    private LocalDateTime validUntil;

    @NotBlank(message = "Purpose of visit is required")
    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, USED, REVOKED, EXPIRED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    @JsonIgnore
    @OneToOne(mappedBy = "approval", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OTPPass otpPass;

    private LocalDateTime createdAt = LocalDateTime.now();

    public VisitorPreApproval() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getExpectedVisitDateTime() {
        return expectedVisitDateTime;
    }

    public void setExpectedVisitDateTime(LocalDateTime expectedVisitDateTime) {
        this.expectedVisitDateTime = expectedVisitDateTime;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public OTPPass getOtpPass() {
        return otpPass;
    }

    public void setOtpPass(OTPPass otpPass) {
        this.otpPass = otpPass;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}