package com.visitorpass.repository;

import com.visitorpass.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Optional<Resident> findByPhoneNumber(String phoneNumber);
    Optional<Resident> findByEmail(String email);
    List<Resident> findByFlatNumber(String flatNumber);
    boolean existsByPhoneNumber(String phoneNumber);
}