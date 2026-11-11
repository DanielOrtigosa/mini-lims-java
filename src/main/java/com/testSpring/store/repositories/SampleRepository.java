package com.testSpring.store.repositories;

import com.testSpring.store.entities.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<Sample, Integer> {
    Sample findSampleBySampleCode(String sampleCode);
    boolean existsByStatus_Id(int statusId);
}
