package com.comutel.backend.workflow.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_transition_definition", indexes = {
        @Index(name = "idx_transition_lookup", columnList = "workflow_definition_id,from_state_key,event_key,active")
})
public class WorkflowTransitionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "from_state_key", nullable = false)
    private String fromStateKey;

    @Column(name = "to_state_key", nullable = false)
    private String toStateKey;

    @Column(name = "event_key", nullable = false)
    private String eventKey;

    @Column(nullable = false)
    private String name;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(nullable = false)
    private Integer priority = 100;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "transition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    private List<WorkflowTransitionAction> actions = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<WorkflowTransitionAction> getActions() {
        return actions;
    }

    public void setActions(List<WorkflowTransitionAction> actions) {
        this.actions = actions;
    }
}
