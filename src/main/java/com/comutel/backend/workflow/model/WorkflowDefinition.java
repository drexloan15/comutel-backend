package com.comutel.backend.workflow.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_definition", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workflow_key_version", columnNames = {"workflow_key", "version"})
})
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(name = "process_type", nullable = false)
    private String processType;

    @Column(nullable = false)
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowDefinitionStatus status = WorkflowDefinitionStatus.DRAFT;

    @Column(nullable = false)
    private boolean active = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowStateDefinition> states = new ArrayList<>();

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowTransitionDefinition> transitions = new ArrayList<>();

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowAssignmentRule> assignmentRules = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public WorkflowDefinitionStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowDefinitionStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<WorkflowStateDefinition> getStates() {
        return states;
    }

    public void setStates(List<WorkflowStateDefinition> states) {
        this.states = states;
    }

    public List<WorkflowTransitionDefinition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<WorkflowTransitionDefinition> transitions) {
        this.transitions = transitions;
    }

    public List<WorkflowAssignmentRule> getAssignmentRules() {
        return assignmentRules;
    }

    public void setAssignmentRules(List<WorkflowAssignmentRule> assignmentRules) {
        this.assignmentRules = assignmentRules;
    }
}
