package com.visitorpass.service;

import com.visitorpass.dto.OTPVerifyResponse;
import com.visitorpass.entity.OTPPass;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.exception.InvalidPassException;
import com.visitorpass.repository.OTPPassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class OTPPassService {

    private static final Logger logger = LoggerFactory.getLogger(OTPPassService.class);

    private final OTPPassRepository otpPassRepository;
    private final EmailService emailService;
    private final EntryLogService entryLogService;
    private final SecureRandom random = new SecureRandom();

    public OTPPassService(OTPPassRepository otpPassRepository,
                          EmailService emailService,
                          EntryLogService entryLogService) {
        this.otpPassRepository = otpPassRepository;
        this.emailService = emailService;
        this.entryLogService = entryLogService;
    }

    @Transactional
    public OTPPass generateOTP(VisitorPreApproval approval) {
        if (approval == null) {
            throw new IllegalArgumentException("Cannot generate OTP for null approval");
        }

        // Prevent creating multiple OTPs for the same approval
        Optional<OTPPass> existing = otpPassRepository.findByApprovalId(approval.getId());
        if (existing.isPresent()) {
            OTPPass existingPass = existing.get();
            if (!existingPass.isUsed() && existingPass.getExpiresAt().isAfter(LocalDateTime.now())) {
                logger.info("Reusing existing active OTP for approval id: {}", approval.getId());
                return existingPass;
            }
        }

        OTPPass otpPass = new OTPPass();
        String otp = String.format("%06d", random.nextInt(1000000));
        otpPass.setOtpCode(otp);
        otpPass.setUsed(false);

        // Expiration time set to match visit window end time or default 2 hours
        LocalDateTime expiresAt = approval.getValidUntil() != null
                ? approval.getValidUntil()
                : (approval.getExpectedVisitDateTime() != null
                    ? approval.getExpectedVisitDateTime().plusHours(2)
                    : LocalDateTime.now().plusHours(2));

        otpPass.setExpiresAt(expiresAt);
        otpPass.setApproval(approval);

        OTPPass savedOtp = otpPassRepository.save(otpPass);

        // Send OTP email
        String residentName = approval.getResident() != null ? approval.getResident().getName() : "Resident";
        String flatNumber = approval.getResident() != null ? approval.getResident().getFlatNumber() : "N/A";
        String expiresAtStr = expiresAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        emailService.sendOtpEmail(approval.getEmail(), approval.getVisitorName(), residentName, flatNumber, otp, expiresAtStr);

        return savedOtp;
    }

    /**
     * Verifies OTP at the security gate with strict transactional isolation
     * enforcing all business rules:
     * 1. OTP exists
     * 2. Approval exists
     * 3. Approval is not revoked
     * 4. Approval is not expired
     * 5. OTP is not expired
     * 6. OTP has not been used before
     * 7. Current time is within permitted visit window
     * 8. OTP accepted only once
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OTPVerifyResponse verifyOTP(String otpCode) {
        if (otpCode == null || otpCode.trim().isEmpty()) {
            entryLogService.recordRejectedAttempt("Unknown", "Unknown", "Unknown", "Missing OTP code");
            throw new InvalidPassException("Invalid OTP: Please enter a valid 6-digit code.");
        }

        String cleanedOtp = otpCode.trim();

        OTPPass otpPass = otpPassRepository.findByOtpCode(cleanedOtp)
                .orElse(null);

        if (otpPass == null) {
            entryLogService.recordRejectedAttempt("Unknown", "Unknown", "Unknown", "Invalid OTP code: " + cleanedOtp);
            throw new InvalidPassException("Invalid OTP: The entered OTP pass code does not exist.");
        }

        VisitorPreApproval approval = otpPass.getApproval();

        String visitorName = approval != null ? approval.getVisitorName() : "Unknown";
        String residentName = (approval != null && approval.getResident() != null) ? approval.getResident().getName() : "Unknown";
        String flatNumber = (approval != null && approval.getResident() != null) ? approval.getResident().getFlatNumber() : "Unknown";
        String purpose = approval != null ? approval.getPurpose() : "Visit";

        // Check 1: Does approval exist?
        if (approval == null) {
            entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber, "No pre-approval associated with pass");
            throw new InvalidPassException("Invalid Pass: No pre-approval associated with this OTP.");
        }

        // Check 2: Is pre-approval revoked?
        if ("REVOKED".equalsIgnoreCase(approval.getStatus())) {
            entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber, "Entry denied: Pre-approval revoked by resident");
            throw new InvalidPassException("Approval revoked: This visitor pre-approval was revoked by the resident.");
        }

        // Check 3: Is pre-approval expired?
        if ("EXPIRED".equalsIgnoreCase(approval.getStatus())) {
            entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber, "Entry denied: Pre-approval marked expired");
            throw new InvalidPassException("Approval expired: This visitor pre-approval has expired.");
        }

        // Check 4: Has OTP already been used?
        if (Boolean.TRUE.equals(otpPass.isUsed())) {
            entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber,
                    "Rejected duplicate attempt: OTP already used previously on " + otpPass.getUsedAt());
            throw new InvalidPassException("OTP already used: This pass has already been validated for entry and cannot be reused.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Check 5: Has OTP pass expired?
        if (otpPass.getExpiresAt() != null && otpPass.getExpiresAt().isBefore(now)) {
            approval.setStatus("EXPIRED");
            entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber,
                    "Entry denied: OTP expired at " + otpPass.getExpiresAt());
            throw new InvalidPassException("OTP has expired: The pass expired at " +
                    otpPass.getExpiresAt().format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")));
        }

        // Check 6: Permitted visit window
        if (approval.getExpectedVisitDateTime() != null) {
            // Permit early arrival up to 30 minutes before expected time
            if (now.isBefore(approval.getExpectedVisitDateTime().minusMinutes(30))) {
                entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber,
                        "Entry denied: Too early. Scheduled for " + approval.getExpectedVisitDateTime());
                throw new InvalidPassException("Visit time has not arrived yet. Permitted from " +
                        approval.getExpectedVisitDateTime().format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")));
            }
        }

        if (approval.getValidUntil() != null && now.isAfter(approval.getValidUntil())) {
            approval.setStatus("EXPIRED");
            entryLogService.recordRejectedAttempt(visitorName, residentName, flatNumber,
                    "Entry denied: Permitted visit window closed at " + approval.getValidUntil());
            throw new InvalidPassException("Visit window has ended. Expected visit window closed at " +
                    approval.getValidUntil().format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")));
        }

        // ALL CHECKS PASSED: Mark used atomically
        otpPass.setUsed(true);
        otpPass.setUsedAt(now);
        approval.setStatus("USED");

        otpPassRepository.save(otpPass);

        // Record entry log
        entryLogService.recordEntry(approval, "GRANTED", "Security gate entry approved");

        logger.info("Security Gate Entry GRANTED for visitor {} to flat {}", visitorName, flatNumber);

        return OTPVerifyResponse.granted(
                "Entry Approved: Valid visitor pass verified successfully.",
                visitorName,
                approval.getVisitorPhone(),
                residentName,
                flatNumber,
                purpose,
                now,
                approval.getId()
        );
    }

    /**
     * Backward-compatible validation method for existing controllers
     */
    @Transactional
    public boolean validateOTP(String otpCode) {
        OTPVerifyResponse response = verifyOTP(otpCode);
        return response.isValid();
    }
}