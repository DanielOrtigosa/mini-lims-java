package com.testSpring.store.services;

import com.testSpring.store.entities.Result;
import com.testSpring.store.entities.Sample;
import com.testSpring.store.repositories.ResultRepository;
import com.testSpring.store.repositories.SampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServicesTest {
    @Mock ResultRepository repository;
    @Mock SampleRepository samples;
    @InjectMocks ResultServices service;

    @Test
    void updateKeepsRequestedId() {
        when(repository.existsById(4)).thenReturn(true);
        when(samples.findById(9)).thenReturn(Optional.of(new Sample()));
        Result result = new Result();
        Sample sample = new Sample();
        sample.setId(9);
        result.setSample(sample);

        service.updateResult(result, 4);

        assertEquals(4, result.getId());
        verify(repository).save(result);
    }

    @Test
    void updateMissingResultFailsClearly() {
        when(repository.existsById(4)).thenReturn(false);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.updateResult(new Result(), 4));
        verify(repository, never()).save(any());
    }

    @Test
    void createRejectsAResultForAMissingSample() {
        Sample sample = new Sample();
        sample.setId(9);
        Result result = new Result();
        result.setSample(sample);
        when(samples.findById(9)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.addResult(result));
        verify(repository, never()).save(any());
    }

    @Test
    void lookupReturnsRepositoryResult() {
        Result result = new Result();
        when(repository.findById(4)).thenReturn(Optional.of(result));
        assertEquals(Optional.of(result), service.getResult(4));
    }
}
