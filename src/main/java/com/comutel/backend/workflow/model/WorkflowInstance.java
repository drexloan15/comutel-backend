package com.comutel.backend.workflow.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_instance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workflow_entity", columnNames = {"entity_type", "entity_id"})
}, indexes = {
        @Index(name = "idx_workflow_instance_due", columnList = "status,due_at"),
        @Index(name = "idx_workflow_instance_state", columnList = "workflow_definition_id,current_state_key")
})
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "current_state_key", nullable = false)
    private String currentStateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowInstanceStatus status = WorkflowInstanceStatus.RUNNING;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "last_event_key")
    private String lastEventKey;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.startedAt = now;
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

    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getCurrentStateKey() {
        return currentStateKey;
    }

    public void setCurrentStateKey(String currentStateKey) {
        this.currentStateKey = currentStateKey;
    }

    public WorkflowInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowInstanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public String getLastEventKey() {
        return lastEventKey;
    }

    public void setLastEventKey(String lastEventKey) {
        this.lastEventKey = lastEventKey;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
