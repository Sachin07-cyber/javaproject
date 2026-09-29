package com.visitorpass;

import com.visitorpass.dto.OTPVerifyResponse;
import com.visitorpass.dto.PreApprovalRequest;
import com.visitorpass.entity.EntryLog;
import com.visitorpass.entity.OTPPass;
import com.visitorpass.entity.Resident;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.exception.DuplicateResourceException;
import com.visitorpass.exception.InvalidPassException;
import com.visitorpass.repository.EntryLogRepository;
import com.visitorpass.repository.OTPPassRepository;
import com.visitorpass.repository.ResidentRepository;
import com.visitorpass.repository.VisitorPreApprovalRepository;
import com.visitorpass.service.EntryLogService;
import com.visitorpass.service.OTPPassService;
import com.visitorpass.service.ResidentService;
import com.visitorpass.service.VisitorPreApprovalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VisitorPassServiceTests {

    @Autowired
    private ResidentService residentService;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private VisitorPreApprovalService approvalService;

    @Autowired
    private VisitorPreApprovalRepository approvalRepository;

    @Autowired
    private OTPPassService otpPassService;

    @Autowired
    private OTPPassRepository otpPassRepository;

    @Autowired
    private EntryLogService entryLogService;

    @Autowired
    private EntryLogRepository entryLogRepository;

    @Test
    @DisplayName("Scenario 1 & 2: Register a resident successfully and view details")
    public void testRegisterAndViewResident() {
        String uniquePhone = "9" + System.currentTimeMillis() % 1000000000L;
        Resident resident = new Resident("Test Resident", "101", uniquePhone, "test.resident@test.com");
        Resident saved = residentService.createResident(resident);

        assertNotNull(saved.getId());
        assertEquals("Test Resident", saved.getName());

        Resident fetched = residentService.getResidentById(saved.getId());
        assertEquals(saved.getId(), fetched.getId());
        assertEquals(uniquePhone, fetched.getPhoneNumber());
    }

    @Test
    @DisplayName("Scenario 10: Reject duplicate resident registration with same phone")
    public void testRejectDuplicateResidentPhone() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 1) % 1000000000L;
        Resident resident1 = new Resident("First Resident", "201", uniquePhone, "res1@test.com");
        residentService.createResident(resident1);

        Resident resident2 = new Resident("Duplicate Resident", "202", uniquePhone, "res2@test.com");
        assertThrows(DuplicateResourceException.class, () -> {
            residentService.createResident(resident2);
        });
    }

    @Test
    @DisplayName("Scenario 3, 4, 5 & 12: Create pre-approval, generate OTP, verify OTP successfully and log entry")
    public void testPreApprovalAndSuccessfulOTPVerification() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 2) % 1000000000L;
        Resident resident = residentService.createResident(
                new Resident("Host Resident", "301", uniquePhone, "host@test.com")
        );

        PreApprovalRequest request = new PreApprovalRequest(
                resident.getId(),
                "Guest Visitor",
                "9876543210",
                "guest@test.com",
                LocalDateTime.now(),
                120,
                "Family Gathering"
        );

        VisitorPreApproval approval = approvalService.createPreApproval(request);
        assertNotNull(approval.getId());
        assertEquals("APPROVED", approval.getStatus());

        OTPPass otpPass = otpPassRepository.findByApprovalId(approval.getId()).orElse(null);
        assertNotNull(otpPass);
        assertNotNull(otpPass.getOtpCode());
        assertFalse(otpPass.isUsed());

        // Verify OTP at gate
        OTPVerifyResponse response = otpPassService.verifyOTP(otpPass.getOtpCode());
        assertTrue(response.isValid());
        assertEquals("GRANTED", response.getStatus());
        assertEquals("Guest Visitor", response.getVisitorName());
        assertEquals("Host Resident", response.getResidentName());
        assertEquals("301", response.getFlatNumber());

        // Check that OTP is now marked as used
        OTPPass updatedPass = otpPassRepository.findById(otpPass.getId()).orElseThrow();
        assertTrue(updatedPass.isUsed());
        assertNotNull(updatedPass.getUsedAt());

        // Check that EntryLog was created
        List<EntryLog> logs = entryLogRepository.findByApprovalId(approval.getId());
        assertFalse(logs.isEmpty());
        assertEquals("GRANTED", logs.get(0).getStatus());
    }

    @Test
    @DisplayName("Scenario 6: Confirm that a used OTP cannot be reused")
    public void testRejectReusedOTP() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 3) % 1000000000L;
        Resident resident = residentService.createResident(
                new Resident("Reuse Test Host", "302", uniquePhone, "reuse@test.com")
        );

        VisitorPreApproval approval = approvalService.createPreApproval(
                new PreApprovalRequest(resident.getId(), "One-Time Visitor", "9876543210",
                        "onetime@test.com", LocalDateTime.now(), 120, "Delivery")
        );

        OTPPass otpPass = otpPassRepository.findByApprovalId(approval.getId()).orElseThrow();
        String otpCode = otpPass.getOtpCode();

        // First verification succeeds
        OTPVerifyResponse first = otpPassService.verifyOTP(otpCode);
        assertTrue(first.isValid());

        // Second verification with the exact same OTP must FAIL
        InvalidPassException ex = assertThrows(InvalidPassException.class, () -> {
            otpPassService.verifyOTP(otpCode);
        });
        assertTrue(ex.getMessage().contains("already used"));
    }

    @Test
    @DisplayName("Scenario 7: Confirm that an expired OTP is rejected")
    public void testRejectExpiredOTP() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 4) % 1000000000L;
        Resident resident = residentService.createResident(
                new Resident("Expiry Test Host", "303", uniquePhone, "expiry@test.com")
        );

        VisitorPreApproval approval = approvalService.createPreApproval(
                new PreApprovalRequest(resident.getId(), "Late Visitor", "9876543210",
                        "late@test.com", LocalDateTime.now(), 120, "Meeting")
        );

        OTPPass otpPass = otpPassRepository.findByApprovalId(approval.getId()).orElseThrow();
        // Force expiry in the past
        otpPass.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        otpPassRepository.save(otpPass);

        InvalidPassException ex = assertThrows(InvalidPassException.class, () -> {
            otpPassService.verifyOTP(otpPass.getOtpCode());
        });
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Scenario 8 & 11: Confirm that a resident can view and revoke a pending approval, and revoked approval is rejected")
    public void testRevokeApprovalAndRejectEntry() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 5) % 1000000000L;
        Resident resident = residentService.createResident(
                new Resident("Revoke Host", "304", uniquePhone, "revoke@test.com")
        );

        VisitorPreApproval approval = approvalService.createPreApproval(
                new PreApprovalRequest(resident.getId(), "Revoked Visitor", "9876543210",
                        "revoked@test.com", LocalDateTime.now(), 120, "Inspection")
        );

        OTPPass otpPass = otpPassRepository.findByApprovalId(approval.getId()).orElseThrow();
        String otpCode = otpPass.getOtpCode();

        // Host resident revokes the pre-approval
        VisitorPreApproval revoked = approvalService.revokeApproval(approval.getId());
        assertEquals("REVOKED", revoked.getStatus());

        // Entry attempt must be rejected
        InvalidPassException ex = assertThrows(InvalidPassException.class, () -> {
            otpPassService.verifyOTP(otpCode);
        });
        assertTrue(ex.getMessage().contains("revoked"));
    }

    @Test
    @DisplayName("Scenario 9: Confirm that a visitor cannot enter before permitted visit window")
    public void testRejectEarlyVisit() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 6) % 1000000000L;
        Resident resident = residentService.createResident(
                new Resident("Early Host", "305", uniquePhone, "early@test.com")
        );

        // Expected visit is 3 hours in the future
        VisitorPreApproval approval = approvalService.createPreApproval(
                new PreApprovalRequest(resident.getId(), "Early Visitor", "9876543210",
                        "earlyvis@test.com", LocalDateTime.now().plusHours(3), 120, "Consultation")
        );

        OTPPass otpPass = otpPassRepository.findByApprovalId(approval.getId()).orElseThrow();

        InvalidPassException ex = assertThrows(InvalidPassException.class, () -> {
            otpPassService.verifyOTP(otpPass.getOtpCode());
        });
        assertTrue(ex.getMessage().contains("Visit time has not arrived yet"));
    }

    @Test
    @DisplayName("Scenario 13: Auto-expiration marks overdue approvals as EXPIRED")
    public void testAutoExpireOverdueApprovals() {
        String uniquePhone = "9" + (System.currentTimeMillis() + 7) % 1000000000L;
        Resident resident = residentService.createResident(
                new Resident("AutoExpire Host", "306", uniquePhone, "autoexp@test.com")
        );

        VisitorPreApproval approval = approvalService.createPreApproval(
                new PreApprovalRequest(resident.getId(), "Overdue Visitor", "9876543210",
                        "overdue@test.com", LocalDateTime.now().minusHours(4), 60, "Past Visit")
        );

        // Force validUntil to past
        approval.setValidUntil(LocalDateTime.now().minusHours(2));
        approvalRepository.save(approval);

        int expiredCount = approvalService.autoExpireOverdueApprovals();
        assertTrue(expiredCount >= 1);

        VisitorPreApproval refreshed = approvalRepository.findById(approval.getId()).orElseThrow();
        assertEquals("EXPIRED", refreshed.getStatus());
    }
}
