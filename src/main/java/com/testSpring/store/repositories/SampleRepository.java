package com.testSpring.store.repositories;

import com.testSpring.store.entities.Sample;
import org.springframework.data.repository.CrudRepository;

public interface SampleRepository extends CrudRepository<Sample, Integer> {
    Sample findSampleBySampleCode(String sampleCode);
}
