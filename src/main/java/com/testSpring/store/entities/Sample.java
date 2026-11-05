package com.testSpring.store.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "SAMPLES")
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String sampleCode;
    private String type;
    private String description;
    private String state;
    private Integer createdBy;
    private LocalDate dateReception;
    private LocalDate dateCreation;
    private LocalDate dateModified;
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "STATUS_ID")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "TECHNICIAN_ID")
    private LimsUser technician;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSampleCode() { return sampleCode; }
    public void setSampleCode(String sampleCode) { this.sampleCode = sampleCode; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public LocalDate getDateReception() { return dateReception; }
    public void setDateReception(LocalDate dateReception) { this.dateReception = dateReception; }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public LocalDate getDateModified() { return dateModified; }
    public void setDateModified(LocalDate dateModified) { this.dateModified = dateModified; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LimsUser getTechnician() { return technician; }
    public void setTechnician(LimsUser technician) { this.technician = technician; }
}
