package com.visitorpass.repository;

import com.visitorpass.entity.VisitorPreApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface VisitorPreApprovalRepository extends JpaRepository<VisitorPreApproval, Long> {
    List<VisitorPreApproval> findByResidentId(Long residentId);
    List<VisitorPreApproval> findByStatus(String status);
    List<VisitorPreApproval> findByStatusInAndValidUntilBefore(Collection<String> statuses, LocalDateTime now);
    List<VisitorPreApproval> findAllByOrderByCreatedAtDesc();
}