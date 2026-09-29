package com.visitorpass.controller;

import com.visitorpass.entity.Visitor;
import com.visitorpass.service.VisitorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping
    public ResponseEntity<List<Visitor>> getAllVisitors() {
        return ResponseEntity.ok(visitorService.getAllVisitors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visitor> getVisitorById(@PathVariable Long id) {
        return ResponseEntity.ok(visitorService.getVisitorById(id));
    }

    @PostMapping
    public ResponseEntity<Visitor> addVisitor(@Valid @RequestBody Visitor visitor) {
        Visitor saved = visitorService.saveVisitor(visitor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visitor> updateVisitor(
            @PathVariable Long id,
            @Valid @RequestBody Visitor visitor) {
        visitor.setId(id);
        Visitor updated = visitorService.saveVisitor(visitor);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitor(@PathVariable Long id) {
        visitorService.deleteVisitor(id);
        return ResponseEntity.noContent().build();
    }
}