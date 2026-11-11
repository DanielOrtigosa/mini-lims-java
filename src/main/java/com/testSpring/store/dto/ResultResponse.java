package com.testSpring.store.dto;

import com.testSpring.store.entities.Result;
import java.time.LocalDate;

public record ResultResponse(int id, SampleSummary sample, String resultValue, LocalDate resultDate) {
    public static ResultResponse from(Result result) {
        var sample = result.getSample();
        return new ResultResponse(result.getId(), new SampleSummary(sample.getId(), sample.getSampleCode()),
                result.getResultValue(), result.getResultDate());
    }
    public record SampleSummary(int id, String sampleCode) { }
}
