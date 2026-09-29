package com.visitorpass.service;

import com.visitorpass.entity.Visitor;
import com.visitorpass.exception.ResourceNotFoundException;
import com.visitorpass.repository.VisitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }

    public Visitor getVisitorById(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found with id: " + id));
    }

    @Transactional
    public Visitor saveVisitor(Visitor visitor) {
        return visitorRepository.save(visitor);
    }

    @Transactional
    public Visitor getOrCreateVisitor(String name, String phoneNumber, String email) {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            return visitorRepository.findByPhoneNumber(phoneNumber)
                    .map(existing -> {
                        existing.setName(name);
                        if (email != null && !email.isBlank()) {
                            existing.setEmail(email);
                        }
                        return visitorRepository.save(existing);
                    })
                    .orElseGet(() -> visitorRepository.save(new Visitor(name, phoneNumber, email)));
        }
        return visitorRepository.save(new Visitor(name, phoneNumber, email));
    }

    @Transactional
    public void deleteVisitor(Long id) {
        if (!visitorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Visitor not found with id: " + id);
        }
        visitorRepository.deleteById(id);
    }
}
