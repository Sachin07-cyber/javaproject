package com.visitorpass.controller;

import com.visitorpass.dto.OTPVerifyRequest;
import com.visitorpass.dto.OTPVerifyResponse;
import com.visitorpass.service.OTPPassService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OTPController {

    private final OTPPassService otpPassService;

    public OTPController(OTPPassService otpPassService) {
        this.otpPassService = otpPassService;
    }

    // Verify OTP at security gate
    @PostMapping("/verify")
    public ResponseEntity<OTPVerifyResponse> verifyOTP(@Valid @RequestBody OTPVerifyRequest request) {
        OTPVerifyResponse response = otpPassService.verifyOTP(request.getOtpCode());
        return ResponseEntity.ok(response);
    }

    // Form/query param verification
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateOtpParam(@RequestParam String otpCode) {
        OTPVerifyResponse response = otpPassService.verifyOTP(otpCode);
        return ResponseEntity.ok(Map.of(
                "valid", response.isValid(),
                "status", response.getStatus(),
                "message", response.getMessage(),
                "visitorName", response.getVisitorName() != null ? response.getVisitorName() : "",
                "flatNumber", response.getFlatNumber() != null ? response.getFlatNumber() : ""
        ));
    }
}
