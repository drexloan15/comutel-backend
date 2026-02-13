package com.comutel.backend.workflow.model;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_task", indexes = {
        @Index(name = "idx_workflow_task_user", columnList = "assignee_user_id,status"),
        @Index(name = "idx_workflow_task_group", columnList = "assignee_group_id,status")
})
public class WorkflowTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;

    @Column(name = "state_key", nullable = false)
    private String stateKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private Usuario assigneeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_group_id")
    private GrupoResolutor assigneeGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowTaskStatus status = WorkflowTaskStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
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

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public Usuario getAssigneeUser() {
        return assigneeUser;
    }

    public void setAssigneeUser(Usuario assigneeUser) {
        this.assigneeUser = assigneeUser;
    }

    public GrupoResolutor getAssigneeGroup() {
        return assigneeGroup;
    }

    public void setAssigneeGroup(GrupoResolutor assigneeGroup) {
        this.assigneeGroup = assigneeGroup;
    }

    public WorkflowTaskStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowTaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
