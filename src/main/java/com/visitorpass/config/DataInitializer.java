package com.visitorpass.config;

import com.visitorpass.entity.Flat;
import com.visitorpass.entity.Resident;
import com.visitorpass.repository.FlatRepository;
import com.visitorpass.repository.ResidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final FlatRepository flatRepository;
    private final ResidentRepository residentRepository;

    public DataInitializer(FlatRepository flatRepository, ResidentRepository residentRepository) {
        this.flatRepository = flatRepository;
        this.residentRepository = residentRepository;
    }

    @Override
    public void run(String... args) {
        // Initialize sample flats if none exist
        if (flatRepository.count() == 0) {
            Flat flat1 = flatRepository.save(new Flat("101", "Tower A", 1));
            Flat flat2 = flatRepository.save(new Flat("102", "Tower A", 1));
            Flat flat3 = flatRepository.save(new Flat("201", "Tower B", 2));
            Flat flat4 = flatRepository.save(new Flat("248", "Tower B", 2));
            logger.info("Initialized default flats in database.");
        }

        // Initialize sample resident if none exist
        if (residentRepository.count() == 0) {
            Flat flat248 = flatRepository.findByFlatNumber("248").orElse(null);
            Resident resident1 = new Resident("Sachin S", "248", "9876543210", "sssachin314@gmail.com");
            resident1.setFlat(flat248);
            residentRepository.save(resident1);

            Flat flat101 = flatRepository.findByFlatNumber("101").orElse(null);
            Resident resident2 = new Resident("Priya Sharma", "101", "9876543211", "priya.sharma@example.com");
            resident2.setFlat(flat101);
            residentRepository.save(resident2);

            logger.info("Initialized default residents in database.");
        } else {
            // Ensure existing residents have valid email and flat associations
            residentRepository.findAll().forEach(r -> {
                boolean changed = false;
                if (r.getEmail() == null || r.getEmail().isBlank()) {
                    r.setEmail("resident" + r.getId() + "@example.com");
                    changed = true;
                }
                if (r.getFlat() == null && r.getFlatNumber() != null) {
                    flatRepository.findByFlatNumber(r.getFlatNumber()).ifPresent(f -> {
                        r.setFlat(f);
                    });
                    changed = true;
                }
                if (changed) {
                    residentRepository.save(r);
                }
            });
        }
    }
}
