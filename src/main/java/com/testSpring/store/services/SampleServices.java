package com.testSpring.store.services;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.repositories.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SampleServices {

    @Autowired
    private SampleRepository sampleRepository;

    public List<Sample> getAllSamples() { return (List<Sample>) this.sampleRepository.findAll(); }
    public Optional<Sample> getSample(int id) {
        Optional<Sample> optional = this.sampleRepository.findById(id);
        return optional.isEmpty() ? null : optional;
    }
    public Optional<Sample> getSampleByCode(String sampleCode) {
        return Optional.ofNullable(this.sampleRepository.findSampleBySampleCode(sampleCode));
    }
    public void addSample(Sample sample) {
        if (sample.getDateCreation() == null) sample.setDateCreation(LocalDate.now());
        this.sampleRepository.save(sample);
    }
    public void updateSample(Sample sample, int id) {
        sample.setId(id);
        sample.setDateModified(LocalDate.now());
        this.sampleRepository.save(sample);
    }
    public void deleteSample(int id) {
        this.sampleRepository.findById(id).ifPresent(sample -> {
            sample.setActive(false);
            this.sampleRepository.save(sample);
        });
    }
    public void activateSample(int id) {
        this.sampleRepository.findById(id).ifPresent(sample -> {
            sample.setActive(true);
            this.sampleRepository.save(sample);
        });
    }
}
