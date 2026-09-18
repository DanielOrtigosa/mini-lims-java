package com.testSpring.store.repositories;

import com.testSpring.store.entities.Result;
import org.springframework.data.repository.CrudRepository;

public interface ResultRepository extends CrudRepository<Result, Integer> {
}
