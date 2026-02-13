package com.comutel.backend.workflow.model;

import com.comutel.backend.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_instance_transition_log", indexes = {
        @Index(name = "idx_transition_log_instance", columnList = "workflow_instance_id,executed_at")
})
public class WorkflowInstanceTransitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;

    @Column(name = "from_state_key")
    private String fromStateKey;

    @Column(name = "to_state_key")
    private String toStateKey;

    @Column(name = "event_key", nullable = false)
    private String eventKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private Usuario actorUser;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "execution_ms")
    private Long executionMs;

    @Column(name = "result", nullable = false)
    private String result;

    @PrePersist
    public void prePersist() {
        this.executedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkflowInstance getWorkflowInstance() {
        return workflowInstance;
    }

    public void setWorkflowInstance(WorkflowInstance workflowInstance) {
        this.workflowInstance = workflowInstance;
    }

    public String getFromStateKey() {
        return fromStateKey;
    }

    public void setFromStateKey(String fromStateKey) {
        this.fromStateKey = fromStateKey;
    }

    public String getToStateKey() {
        return toStateKey;
    }

    public void setToStateKey(String toStateKey) {
        this.toStateKey = toStateKey;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public Usuario getActorUser() {
        return actorUser;
    }

    public void setActorUser(Usuario actorUser) {
        this.actorUser = actorUser;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public Long getExecutionMs() {
        return executionMs;
    }

    public void setExecutionMs(Long executionMs) {
        this.executionMs = executionMs;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
