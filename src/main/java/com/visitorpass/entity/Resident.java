package com.visitorpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resident")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resident name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Flat number is required")
    @Column(nullable = false)
    private String flatNumber;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;

    @Email(message = "Please provide a valid email address")
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @JsonIgnore
    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VisitorPreApproval> preApprovals = new ArrayList<>();

    public Resident() {
    }

    public Resident(String name, String flatNumber, String phoneNumber, String email) {
        this.name = name;
        this.flatNumber = flatNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

    public List<VisitorPreApproval> getPreApprovals() {
        return preApprovals;
    }

    public void setPreApprovals(List<VisitorPreApproval> preApprovals) {
        this.preApprovals = preApprovals;
    }
}