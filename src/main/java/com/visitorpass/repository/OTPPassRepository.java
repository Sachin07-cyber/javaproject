package com.visitorpass.repository;

import com.visitorpass.entity.OTPPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPPassRepository extends JpaRepository<OTPPass, Long> {
    Optional<OTPPass> findByOtpCode(String otpCode);
    Optional<OTPPass> findByApprovalId(Long approvalId);
    List<OTPPass> findByUsedFalseAndExpiresAtBefore(LocalDateTime now);
}