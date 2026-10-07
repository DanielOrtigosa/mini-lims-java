package com.testSpring.store.repositories;

import com.testSpring.store.entities.Status;
import org.springframework.data.repository.CrudRepository;

public interface StatusRepository extends CrudRepository<Status, Integer> {
    Status findStatusByCode(String code);
    Status findFirstByCodeIgnoreCase(String code);
}
