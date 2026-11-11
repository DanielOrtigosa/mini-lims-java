package com.testSpring.store.services;

import com.testSpring.store.entities.Result;
import com.testSpring.store.entities.Sample;
import com.testSpring.store.repositories.ResultRepository;
import com.testSpring.store.repositories.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ResultServices {

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private SampleRepository sampleRepository;

    public List<Result> getAllResults() {
        return (List<Result>) this.resultRepository.findAll();
    }
    public Page<Result> getAllResults(Pageable pageable) { return resultRepository.findAll(pageable); }

    public Optional<Result> getResult(int id) {
        return this.resultRepository.findById(id);
    }

    public void addResult(Result result) {
        result.setSample(requireSample(result.getSample().getId()));
        this.resultRepository.save(result);
    }

    public void updateResult(Result result, int id) {
        if (!resultRepository.existsById(id)) throw new IllegalArgumentException("No existe el resultado " + id);
        result.setId(id);
        result.setSample(requireSample(result.getSample().getId()));
        this.resultRepository.save(result);
    }

    public void deleteResult(int id) {
        this.resultRepository.deleteById(id);
    }

    private Sample requireSample(int sampleId) {
        return sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la muestra " + sampleId));
    }
}
