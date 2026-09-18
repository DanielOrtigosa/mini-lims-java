package com.testSpring.store.services;

import com.testSpring.store.entities.Result;
import com.testSpring.store.repositories.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultServices {

    @Autowired
    private ResultRepository resultRepository;

    public List<Result> getAllResults() {
        return (List<Result>) this.resultRepository.findAll();
    }

    public Optional<Result> getResult(int id) {
        Optional<Result> optional = this.resultRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        return optional;
    }

    public void addResult(Result result) {
        this.resultRepository.save(result);
    }

    public void updateResult(Result result, int id) {
        result.setId(id);
        this.resultRepository.save(result);
    }

    public void deleteResult(int id) {
        this.resultRepository.deleteById(id);
    }
}
