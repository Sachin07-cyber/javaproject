package com.visitorpass.controller;

import com.visitorpass.dto.PreApprovalRequest;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.service.VisitorPreApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class VisitorPreApprovalController {

    private final VisitorPreApprovalService approvalService;

    public VisitorPreApprovalController(VisitorPreApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    // Get all approvals
    @GetMapping
    public ResponseEntity<List<VisitorPreApproval>> getAllApprovals() {
        return ResponseEntity.ok(approvalService.getAllApprovals());
    }

    // Get approval by ID
    @GetMapping("/{id}")
    public ResponseEntity<VisitorPreApproval> getApprovalById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalService.getApprovalById(id));
    }

    // Get approvals for a specific resident
    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<VisitorPreApproval>> getApprovalsByResident(@PathVariable Long residentId) {
        return ResponseEntity.ok(approvalService.getApprovalsByResident(residentId));
    }

    // Create a new pre-approval using DTO
    @PostMapping
    public ResponseEntity<VisitorPreApproval> createApproval(
            @Valid @RequestBody PreApprovalRequest request) {
        VisitorPreApproval savedApproval = approvalService.createPreApproval(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApproval);
    }

    // Direct pre-approval creation backward-compatibility
    @PostMapping("/direct")
    public ResponseEntity<VisitorPreApproval> createApprovalDirect(
            @RequestBody VisitorPreApproval approval) {
        VisitorPreApproval saved = approvalService.createApprovalDirect(approval);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Revoke pre-approval
    @PutMapping("/{id}/revoke")
    public ResponseEntity<VisitorPreApproval> revokeApproval(@PathVariable Long id) {
        VisitorPreApproval revoked = approvalService.revokeApproval(id);
        return ResponseEntity.ok(revoked);
    }
}