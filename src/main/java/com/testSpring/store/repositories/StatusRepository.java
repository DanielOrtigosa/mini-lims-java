package com.testSpring.store.repositories;

import com.testSpring.store.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    Status findStatusByCode(String code);
    Status findFirstByCodeIgnoreCase(String code);
}
