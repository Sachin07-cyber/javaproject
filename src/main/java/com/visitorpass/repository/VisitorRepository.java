package com.visitorpass.repository;

import com.visitorpass.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Optional<Visitor> findByPhoneNumber(String phoneNumber);
    Optional<Visitor> findByEmail(String email);
}