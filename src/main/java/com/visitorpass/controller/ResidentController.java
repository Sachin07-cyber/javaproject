package com.visitorpass.controller;

import com.visitorpass.entity.Resident;
import com.visitorpass.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    // Get all residents
    @GetMapping
    public ResponseEntity<List<Resident>> getAllResidents() {
        return ResponseEntity.ok(residentService.getAllResidents());
    }

    // Get resident by ID
    @GetMapping("/{id}")
    public ResponseEntity<Resident> getResidentById(@PathVariable Long id) {
        return ResponseEntity.ok(residentService.getResidentById(id));
    }

    // Add a new resident
    @PostMapping
    public ResponseEntity<Resident> addResident(@Valid @RequestBody Resident resident) {
        Resident saved = residentService.createResident(resident);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update resident
    @PutMapping("/{id}")
    public ResponseEntity<Resident> updateResident(
            @PathVariable Long id,
            @Valid @RequestBody Resident resident) {
        Resident updated = residentService.updateResident(id, resident);
        return ResponseEntity.ok(updated);
    }

    // Delete resident
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResident(@PathVariable Long id) {
        residentService.deleteResident(id);
        return ResponseEntity.noContent().build();
    }
}