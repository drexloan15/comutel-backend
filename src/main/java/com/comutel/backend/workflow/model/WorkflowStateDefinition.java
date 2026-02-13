package com.comutel.backend.workflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workflow_state_definition", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workflow_state_key", columnNames = {"workflow_definition_id", "state_key"})
})
public class WorkflowStateDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "state_key", nullable = false)
    private String stateKey;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStateType stateType = WorkflowStateType.NORMAL;

    @Column(name = "external_status")
    private String externalStatus;

    @Column(name = "ui_color")
    private String uiColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id")
    private WorkflowSlaPolicy slaPolicy;

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

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkflowStateType getStateType() {
        return stateType;
    }

    public void setStateType(WorkflowStateType stateType) {
        this.stateType = stateType;
    }

    public String getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(String externalStatus) {
        this.externalStatus = externalStatus;
    }

    public String getUiColor() {
        return uiColor;
    }

    public void setUiColor(String uiColor) {
        this.uiColor = uiColor;
    }

    public WorkflowSlaPolicy getSlaPolicy() {
        return slaPolicy;
    }

    public void setSlaPolicy(WorkflowSlaPolicy slaPolicy) {
        this.slaPolicy = slaPolicy;
    }
}
