package com.visitorpass.controller;

import com.visitorpass.entity.EntryLog;
import com.visitorpass.service.EntryLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entry-logs")
public class EntryLogController {

    private final EntryLogService entryLogService;

    public EntryLogController(EntryLogService entryLogService) {
        this.entryLogService = entryLogService;
    }

    @GetMapping
    public ResponseEntity<List<EntryLog>> getAllLogs() {
        return ResponseEntity.ok(entryLogService.getAllLogs());
    }

    @GetMapping("/approval/{approvalId}")
    public ResponseEntity<List<EntryLog>> getLogsByApproval(@PathVariable Long approvalId) {
        return ResponseEntity.ok(entryLogService.getLogsByApproval(approvalId));
    }
}
