package com.testSpring.store.repositories;

import com.testSpring.store.entities.SampleStatusHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SampleStatusHistoryRepository extends CrudRepository<SampleStatusHistory, Integer> {
    List<SampleStatusHistory> findBySample_IdOrderByChangedAtAsc(int sampleId);
}
