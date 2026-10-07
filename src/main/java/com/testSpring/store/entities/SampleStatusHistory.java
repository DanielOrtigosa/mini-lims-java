package com.testSpring.store.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "SAMPLE_STATUS_HISTORY")
@SuppressWarnings("JpaDataSourceORMInspection")
public class SampleStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "SAMPLE_ID", nullable = false)
    private Sample sample;

    @Column(name = "PREVIOUS_STATUS_CODE")
    private String previousStatusCode;

    @Column(name = "NEW_STATUS_CODE", nullable = false)
    private String newStatusCode;

    @Column(name = "CHANGED_AT", nullable = false)
    private LocalDateTime changedAt;

    protected SampleStatusHistory() { }

    public SampleStatusHistory(Sample sample, String previousStatusCode, String newStatusCode,
                               LocalDateTime changedAt) {
        this.sample = sample;
        this.previousStatusCode = previousStatusCode;
        this.newStatusCode = newStatusCode;
        this.changedAt = changedAt;
    }

    public Integer getId() { return id; }
    public Sample getSample() { return sample; }
    public String getPreviousStatusCode() { return previousStatusCode; }
    public String getNewStatusCode() { return newStatusCode; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
