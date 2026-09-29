package com.visitorpass.service;

import com.visitorpass.entity.EntryLog;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.repository.EntryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EntryLogService {

    private final EntryLogRepository entryLogRepository;

    public EntryLogService(EntryLogRepository entryLogRepository) {
        this.entryLogRepository = entryLogRepository;
    }

    public List<EntryLog> getAllLogs() {
        return entryLogRepository.findAllByOrderByEntryTimeDesc();
    }

    public List<EntryLog> getLogsByApproval(Long approvalId) {
        return entryLogRepository.findByApprovalId(approvalId);
    }

    @Transactional
    public EntryLog recordEntry(VisitorPreApproval approval, String status, String remarks) {
        EntryLog log = new EntryLog();
        log.setApproval(approval);
        log.setEntryTime(LocalDateTime.now());
        log.setStatus(status);
        log.setRemarks(remarks);

        if (approval != null) {
            log.setVisitorName(approval.getVisitorName());
            log.setVisitorPhone(approval.getVisitorPhone());
            if (approval.getResident() != null) {
                log.setResidentName(approval.getResident().getName());
                log.setFlatNumber(approval.getResident().getFlatNumber());
            }
        }

        return entryLogRepository.save(log);
    }

    @Transactional
    public EntryLog recordRejectedAttempt(String visitorName, String residentName, String flatNumber, String remarks) {
        EntryLog log = new EntryLog();
        log.setVisitorName(visitorName != null ? visitorName : "Unknown Visitor");
        log.setResidentName(residentName != null ? residentName : "Unknown Resident");
        log.setFlatNumber(flatNumber != null ? flatNumber : "Unknown Flat");
        log.setEntryTime(LocalDateTime.now());
        log.setStatus("DENIED");
        log.setRemarks(remarks);
        return entryLogRepository.save(log);
    }
}
