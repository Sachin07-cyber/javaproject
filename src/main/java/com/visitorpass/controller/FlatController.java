package com.visitorpass.controller;

import com.visitorpass.entity.Flat;
import com.visitorpass.repository.FlatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flats")
public class FlatController {

    private final FlatRepository flatRepository;

    public FlatController(FlatRepository flatRepository) {
        this.flatRepository = flatRepository;
    }

    @GetMapping
    public ResponseEntity<List<Flat>> getAllFlats() {
        return ResponseEntity.ok(flatRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Flat> createFlat(@RequestBody Flat flat) {
        Flat saved = flatRepository.save(flat);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
