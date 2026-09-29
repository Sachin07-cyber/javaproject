package com.visitorpass.service;

import com.visitorpass.dto.PreApprovalRequest;
import com.visitorpass.entity.OTPPass;
import com.visitorpass.entity.Resident;
import com.visitorpass.entity.Visitor;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.exception.InvalidPassException;
import com.visitorpass.exception.ResourceNotFoundException;
import com.visitorpass.repository.OTPPassRepository;
import com.visitorpass.repository.VisitorPreApprovalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class VisitorPreApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(VisitorPreApprovalService.class);

    private final VisitorPreApprovalRepository preApprovalRepository;
    private final ResidentService residentService;
    private final VisitorService visitorService;
    private final OTPPassService otpPassService;
    private final OTPPassRepository otpPassRepository;

    public VisitorPreApprovalService(VisitorPreApprovalRepository preApprovalRepository,
                                     ResidentService residentService,
                                     VisitorService visitorService,
                                     OTPPassService otpPassService,
                                     OTPPassRepository otpPassRepository) {
        this.preApprovalRepository = preApprovalRepository;
        this.residentService = residentService;
        this.visitorService = visitorService;
        this.otpPassService = otpPassService;
        this.otpPassRepository = otpPassRepository;
    }

    public List<VisitorPreApproval> getAllApprovals() {
        return preApprovalRepository.findAllByOrderByCreatedAtDesc();
    }

    public VisitorPreApproval getApprovalById(Long id) {
        return preApprovalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pre-approval not found with id: " + id));
    }

    public List<VisitorPreApproval> getApprovalsByResident(Long residentId) {
        return preApprovalRepository.findByResidentId(residentId);
    }

    @Transactional
    public VisitorPreApproval createPreApproval(PreApprovalRequest request) {
        Resident resident = residentService.getResidentById(request.getResidentId());

        Visitor visitor = visitorService.getOrCreateVisitor(
                request.getVisitorName(),
                request.getVisitorPhone(),
                request.getVisitorEmail()
        );

        LocalDateTime visitTime = request.getExpectedVisitDateTime() != null
                ? request.getExpectedVisitDateTime()
                : LocalDateTime.now();

        int duration = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0)
                ? request.getDurationMinutes()
                : 120; // Default 2 hours

        LocalDateTime validUntil = visitTime.plusMinutes(duration);

        VisitorPreApproval approval = new VisitorPreApproval();
        approval.setVisitorName(request.getVisitorName());
        approval.setVisitorPhone(request.getVisitorPhone());
        approval.setEmail(request.getVisitorEmail());
        approval.setExpectedVisitDateTime(visitTime);
        approval.setValidUntil(validUntil);
        approval.setPurpose(request.getPurpose());
        approval.setStatus("APPROVED");
        approval.setResident(resident);
        approval.setVisitor(visitor);
        approval.setCreatedAt(LocalDateTime.now());

        VisitorPreApproval savedApproval = preApprovalRepository.save(approval);

        // Generate OTP and send email
        OTPPass pass = otpPassService.generateOTP(savedApproval);
        savedApproval.setOtpPass(pass);

        logger.info("Created pre-approval id={} for visitor {} to resident {}",
                savedApproval.getId(), savedApproval.getVisitorName(), resident.getName());

        return savedApproval;
    }

    @Transactional
    public VisitorPreApproval createApprovalDirect(VisitorPreApproval approval) {
        if (approval.getStatus() == null || approval.getStatus().isBlank()) {
            approval.setStatus("APPROVED");
        }
        if (approval.getExpectedVisitDateTime() == null) {
            approval.setExpectedVisitDateTime(LocalDateTime.now());
        }
        if (approval.getValidUntil() == null) {
            approval.setValidUntil(approval.getExpectedVisitDateTime().plusHours(2));
        }

        VisitorPreApproval saved = preApprovalRepository.save(approval);
        otpPassService.generateOTP(saved);
        return saved;
    }

    @Transactional
    public VisitorPreApproval revokeApproval(Long id) {
        VisitorPreApproval approval = getApprovalById(id);

        if ("USED".equalsIgnoreCase(approval.getStatus())) {
            throw new InvalidPassException("Cannot revoke approval: Visitor has already entered the premises.");
        }
        if ("REVOKED".equalsIgnoreCase(approval.getStatus())) {
            throw new InvalidPassException("Pre-approval is already revoked.");
        }
        if ("EXPIRED".equalsIgnoreCase(approval.getStatus())) {
            throw new InvalidPassException("Pre-approval has already expired and cannot be revoked.");
        }

        approval.setStatus("REVOKED");
        VisitorPreApproval updatedApproval = preApprovalRepository.save(approval);

        // Invalidate associated OTP pass immediately
        otpPassRepository.findByApprovalId(id).ifPresent(otpPass -> {
            otpPass.setExpiresAt(LocalDateTime.now());
            otpPass.setUsed(true); // Ensure it can never be validated
            otpPassRepository.save(otpPass);
        });

        logger.info("Revoked pre-approval id={}", id);
        return updatedApproval;
    }

    @Transactional
    public int autoExpireOverdueApprovals() {
        LocalDateTime now = LocalDateTime.now();
        List<VisitorPreApproval> overdue = preApprovalRepository
                .findByStatusInAndValidUntilBefore(Arrays.asList("PENDING", "APPROVED"), now);

        for (VisitorPreApproval app : overdue) {
            app.setStatus("EXPIRED");
            preApprovalRepository.save(app);

            otpPassRepository.findByApprovalId(app.getId()).ifPresent(pass -> {
                pass.setExpiresAt(now);
                otpPassRepository.save(pass);
            });
        }

        if (!overdue.isEmpty()) {
            logger.info("Auto-expired {} overdue visitor pre-approvals.", overdue.size());
        }
        return overdue.size();
    }
}
