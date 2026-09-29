package com.visitorpass.repository;

import com.visitorpass.entity.EntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntryLogRepository extends JpaRepository<EntryLog, Long> {
    List<EntryLog> findAllByOrderByEntryTimeDesc();
    List<EntryLog> findByApprovalId(Long approvalId);
}
