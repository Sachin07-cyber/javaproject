package com.visitorpass.controller;

import com.visitorpass.entity.EntryLog;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.repository.EntryLogRepository;
import com.visitorpass.repository.ResidentRepository;
import com.visitorpass.repository.VisitorPreApprovalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardPageController {

    private final ResidentRepository residentRepository;
    private final VisitorPreApprovalRepository approvalRepository;
    private final EntryLogRepository entryLogRepository;

    public DashboardPageController(ResidentRepository residentRepository,
                                  VisitorPreApprovalRepository approvalRepository,
                                  EntryLogRepository entryLogRepository) {
        this.residentRepository = residentRepository;
        this.approvalRepository = approvalRepository;
        this.entryLogRepository = entryLogRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        long totalResidents = residentRepository.count();
        List<VisitorPreApproval> allApprovals = approvalRepository.findAllByOrderByCreatedAtDesc();
        long activeApprovals = allApprovals.stream()
                .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()) || "PENDING".equalsIgnoreCase(a.getStatus()))
                .count();
        List<EntryLog> allLogs = entryLogRepository.findAllByOrderByEntryTimeDesc();
        long grantedEntries = allLogs.stream()
                .filter(l -> "GRANTED".equalsIgnoreCase(l.getStatus()))
                .count();

        model.addAttribute("totalResidents", totalResidents);
        model.addAttribute("activeApprovals", activeApprovals);
        model.addAttribute("totalApprovals", allApprovals.size());
        model.addAttribute("grantedEntries", grantedEntries);
        model.addAttribute("recentApprovals", allApprovals.stream().limit(5).toList());
        model.addAttribute("recentLogs", allLogs.stream().limit(5).toList());

        return "dashboard";
    }
}
