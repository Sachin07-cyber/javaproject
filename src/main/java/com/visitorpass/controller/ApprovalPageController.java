package com.visitorpass.controller;

import com.visitorpass.dto.PreApprovalRequest;
import com.visitorpass.entity.VisitorPreApproval;
import com.visitorpass.service.ResidentService;
import com.visitorpass.service.VisitorPreApprovalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class ApprovalPageController {

    private final VisitorPreApprovalService approvalService;
    private final ResidentService residentService;

    public ApprovalPageController(VisitorPreApprovalService approvalService,
                                  ResidentService residentService) {
        this.approvalService = approvalService;
        this.residentService = residentService;
    }

    @GetMapping("/approvals")
    public String showApprovals(Model model) {
        model.addAttribute("approvals", approvalService.getAllApprovals());
        return "approval-list";
    }

    @GetMapping("/approvals/new")
    public String showApprovalForm(Model model) {
        model.addAttribute("residents", residentService.getAllResidents());
        model.addAttribute("defaultDateTime", LocalDateTime.now().plusHours(1).withSecond(0).withNano(0));
        return "approval-form";
    }

    @PostMapping("/approvals/create")
    public String createApproval(@RequestParam Long residentId,
                                 @RequestParam String visitorName,
                                 @RequestParam(required = false) String visitorPhone,
                                 @RequestParam String visitorEmail,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expectedVisitDateTime,
                                 @RequestParam(defaultValue = "120") Integer durationMinutes,
                                 @RequestParam String purpose,
                                 RedirectAttributes redirectAttributes) {
        try {
            PreApprovalRequest request = new PreApprovalRequest(
                    residentId, visitorName, visitorPhone, visitorEmail,
                    expectedVisitDateTime, durationMinutes, purpose
            );

            VisitorPreApproval saved = approvalService.createPreApproval(request);

            String otpCode = saved.getOtpPass() != null ? saved.getOtpPass().getOtpCode() : "N/A";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Visitor Pre-Approval created successfully! 6-digit OTP pass generated and sent to " + visitorEmail + ".");
            redirectAttributes.addFlashAttribute("generatedOtp", otpCode);
            redirectAttributes.addFlashAttribute("visitorName", visitorName);
            redirectAttributes.addFlashAttribute("approvalId", saved.getId());

            return "redirect:/approvals?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/approvals/new?error";
        }
    }

    @PostMapping("/approvals/{id}/revoke")
    public String revokeApproval(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            approvalService.revokeApproval(id);
            redirectAttributes.addFlashAttribute("successMessage", "Approval #" + id + " has been successfully REVOKED. Its OTP pass is immediately invalidated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/approvals";
    }
}
