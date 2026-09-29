package com.visitorpass.repository;

import com.visitorpass.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {
    Optional<Flat> findByFlatNumber(String flatNumber);
    boolean existsByFlatNumber(String flatNumber);
}
