package com.visitorpass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visitorpass.dto.OTPVerifyRequest;
import com.visitorpass.dto.PreApprovalRequest;
import com.visitorpass.dto.ResidentRequest;
import com.visitorpass.entity.OTPPass;
import com.visitorpass.entity.Resident;
import com.visitorpass.repository.OTPPassRepository;
import com.visitorpass.service.ResidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class VisitorPassControllerIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private ResidentService residentService;

    @Autowired
    private OTPPassRepository otpPassRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("REST API: Create and retrieve residents via /api/residents")
    public void testResidentApi() throws Exception {
        String phone = "8" + System.currentTimeMillis() % 1000000000L;
        ResidentRequest resident = new ResidentRequest("Integration Resident", "102", phone, "intres@test.com");

        mockMvc.perform(post("/api/residents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resident)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Resident"))
                .andExpect(jsonPath("$.flatNumber").value("102"));

        mockMvc.perform(get("/api/residents"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("REST API: End-to-end Pre-Approval, OTP verification and Revocation")
    public void testApprovalAndOTPVerificationApi() throws Exception {
        String phone = "8" + (System.currentTimeMillis() + 10) % 1000000000L;
        Resident host = residentService.createResident(new Resident("API Host", "205", phone, "apihost@test.com"));

        PreApprovalRequest approvalReq = new PreApprovalRequest(
                host.getId(),
                "API Visitor",
                "9876500001",
                "apivisitor@test.com",
                LocalDateTime.now(),
                120,
                "Official Visit"
        );

        // 1. Create Pre-Approval
        String responseContent = mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitorName").value("API Visitor"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn().getResponse().getContentAsString();

        Long approvalId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Fetch the generated OTP from repository
        OTPPass pass = otpPassRepository.findByApprovalId(approvalId).orElseThrow();
        String otpCode = pass.getOtpCode();

        // 3. Verify OTP via /api/otp/verify
        OTPVerifyRequest verifyReq = new OTPVerifyRequest(otpCode);
        mockMvc.perform(post("/api/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("GRANTED"))
                .andExpect(jsonPath("$.visitorName").value("API Visitor"))
                .andExpect(jsonPath("$.flatNumber").value("205"));

        // 4. Verify Entry Logs via /api/entry-logs
        mockMvc.perform(get("/api/entry-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("REST API: Revoke approval via /api/approvals/{id}/revoke")
    public void testRevokeApi() throws Exception {
        String phone = "8" + (System.currentTimeMillis() + 20) % 1000000000L;
        Resident host = residentService.createResident(new Resident("Revoke API Host", "206", phone, "revhost@test.com"));

        PreApprovalRequest approvalReq = new PreApprovalRequest(
                host.getId(),
                "Will Be Revoked",
                "9876500002",
                "revokedapi@test.com",
                LocalDateTime.now(),
                120,
                "Temporary"
        );

        String responseContent = mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long approvalId = objectMapper.readTree(responseContent).get("id").asLong();

        mockMvc.perform(put("/api/approvals/" + approvalId + "/revoke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
    }
}
