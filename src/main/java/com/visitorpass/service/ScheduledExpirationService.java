package com.visitorpass.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledExpirationService {

    private final VisitorPreApprovalService preApprovalService;

    public ScheduledExpirationService(VisitorPreApprovalService preApprovalService) {
        this.preApprovalService = preApprovalService;
    }

    // Run every 60 seconds to automatically expire passes whose visit window has ended
    @Scheduled(fixedRate = 60000)
    public void runAutoExpiration() {
        preApprovalService.autoExpireOverdueApprovals();
    }
}
