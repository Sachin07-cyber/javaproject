package com.visitorpass.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entry_log")
public class EntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String visitorName;

    private String visitorPhone;

    private String residentName;

    private String flatNumber;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    @Column(nullable = false)
    private String status; // GRANTED, DENIED

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approval_id")
    private VisitorPreApproval approval;

    public EntryLog() {
    }

    public EntryLog(String visitorName, String visitorPhone, String residentName, String flatNumber,
                    LocalDateTime entryTime, String status, String remarks, VisitorPreApproval approval) {
        this.visitorName = visitorName;
        this.visitorPhone = visitorPhone;
        this.residentName = residentName;
        this.flatNumber = flatNumber;
        this.entryTime = entryTime;
        this.status = status;
        this.remarks = remarks;
        this.approval = approval;
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

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public VisitorPreApproval getApproval() {
        return approval;
    }

    public void setApproval(VisitorPreApproval approval) {
        this.approval = approval;
    }
}
