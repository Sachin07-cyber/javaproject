package com.visitorpass.controller;

import com.visitorpass.dto.OTPVerifyResponse;
import com.visitorpass.service.EntryLogService;
import com.visitorpass.service.OTPPassService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SecurityPageController {

    private final OTPPassService otpPassService;
    private final EntryLogService entryLogService;

    public SecurityPageController(OTPPassService otpPassService, EntryLogService entryLogService) {
        this.otpPassService = otpPassService;
        this.entryLogService = entryLogService;
    }

    @GetMapping("/security")
    public String showSecurityGate(Model model) {
        return "security-verify";
    }

    @PostMapping("/security/verify")
    public String verifyOtp(@RequestParam String otpCode, Model model) {
        model.addAttribute("enteredOtp", otpCode);
        try {
            OTPVerifyResponse result = otpPassService.verifyOTP(otpCode);
            model.addAttribute("verificationResult", result);
        } catch (Exception e) {
            OTPVerifyResponse denied = OTPVerifyResponse.denied(e.getMessage());
            model.addAttribute("verificationResult", denied);
        }
        return "security-verify";
    }

    @GetMapping({"/security/logs", "/logs"})
    public String showEntryLogs(Model model) {
        model.addAttribute("logs", entryLogService.getAllLogs());
        return "entry-logs";
    }
}
