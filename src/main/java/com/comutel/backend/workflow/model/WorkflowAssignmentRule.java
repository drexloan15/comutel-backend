package com.comutel.backend.workflow.model;

import com.comutel.backend.model.GrupoResolutor;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_assignment_rule")
public class WorkflowAssignmentRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "state_key", nullable = false)
    private String stateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowAssignmentStrategy strategy = WorkflowAssignmentStrategy.NONE;

    @Column(columnDefinition = "TEXT")
    private String expression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_group_id")
    private GrupoResolutor targetGroup;

    @Column(name = "priority_order", nullable = false)
    private Integer priorityOrder = 100;

    @Column(nullable = false)
    private boolean active = true;

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

    public WorkflowAssignmentStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(WorkflowAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public GrupoResolutor getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(GrupoResolutor targetGroup) {
        this.targetGroup = targetGroup;
    }

    public Integer getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
