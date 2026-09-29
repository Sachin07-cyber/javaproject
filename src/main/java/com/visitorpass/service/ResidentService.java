package com.visitorpass.service;

import com.visitorpass.entity.Flat;
import com.visitorpass.entity.Resident;
import com.visitorpass.exception.DuplicateResourceException;
import com.visitorpass.exception.ResourceNotFoundException;
import com.visitorpass.repository.FlatRepository;
import com.visitorpass.repository.ResidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;

    public ResidentService(ResidentRepository residentRepository, FlatRepository flatRepository) {
        this.residentRepository = residentRepository;
        this.flatRepository = flatRepository;
    }

    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }

    public Resident getResidentById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));
    }

    @Transactional
    public Resident createResident(Resident resident) {
        if (resident.getPhoneNumber() != null && !resident.getPhoneNumber().isBlank()) {
            residentRepository.findByPhoneNumber(resident.getPhoneNumber()).ifPresent(existing -> {
                throw new DuplicateResourceException("Resident already registered with phone number: " + resident.getPhoneNumber());
            });
        }

        // Link with Flat entity if flat exists or create default flat
        if (resident.getFlatNumber() != null && !resident.getFlatNumber().isBlank()) {
            Flat flat = flatRepository.findByFlatNumber(resident.getFlatNumber())
                    .orElseGet(() -> flatRepository.save(new Flat(resident.getFlatNumber(), "Main Block", 1)));
            resident.setFlat(flat);
        }

        return residentRepository.save(resident);
    }

    @Transactional
    public Resident updateResident(Long id, Resident residentDetails) {
        Resident resident = getResidentById(id);

        if (residentDetails.getPhoneNumber() != null &&
                !residentDetails.getPhoneNumber().equals(resident.getPhoneNumber())) {
            residentRepository.findByPhoneNumber(residentDetails.getPhoneNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("Phone number already in use by another resident");
                }
            });
        }

        resident.setName(residentDetails.getName());
        resident.setFlatNumber(residentDetails.getFlatNumber());
        resident.setPhoneNumber(residentDetails.getPhoneNumber());
        resident.setEmail(residentDetails.getEmail());

        if (residentDetails.getFlatNumber() != null && !residentDetails.getFlatNumber().isBlank()) {
            Flat flat = flatRepository.findByFlatNumber(residentDetails.getFlatNumber())
                    .orElseGet(() -> flatRepository.save(new Flat(residentDetails.getFlatNumber(), "Main Block", 1)));
            resident.setFlat(flat);
        }

        return residentRepository.save(resident);
    }

    @Transactional
    public void deleteResident(Long id) {
        if (!residentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Resident not found with id: " + id);
        }
        residentRepository.deleteById(id);
    }
}
