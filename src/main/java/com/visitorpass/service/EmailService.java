package com.visitorpass.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public boolean sendOtpEmail(String toEmail, String visitorName, String residentName,
                                String flatNumber, String otp, String expiresAtStr) {
        if (mailSender == null) {
            logger.warn("JavaMailSender is not configured. Simulating OTP email to {}: OTP={}", toEmail, otp);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("VisitorPass - Entry OTP Pass for Flat " + flatNumber);
            message.setText(
                    "Hello " + visitorName + ",\n\n" +
                    "Resident " + residentName + " has pre-approved your visit to Flat " + flatNumber + ".\n\n" +
                    "Your One-Time Password (OTP) for security gate entry is:\n\n" +
                    "  >>>  " + otp + "  <<<\n\n" +
                    "Validity: Valid until " + expiresAtStr + ".\n" +
                    "Important: Please present this 6-digit OTP to the security guard at the gate for entry verification.\n" +
                    "Note: This OTP is valid for a single entry only.\n\n" +
                    "VisitorPass Security System"
            );

            mailSender.send(message);
            logger.info("Successfully sent OTP email to: {}", toEmail);
            return true;
        } catch (Exception e) {
            logger.error("Could not send email to {}: {}. OTP is generated in system.", toEmail, e.getMessage());
            return false;
        }
    }

    // Retain backward-compatible method signature
    public void sendOtpEmail(String toEmail, String otp) {
        sendOtpEmail(toEmail, "Visitor", "Resident", "Community", otp, "15 minutes");
    }
}