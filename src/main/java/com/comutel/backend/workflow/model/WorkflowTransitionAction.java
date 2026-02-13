package com.comutel.backend.workflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workflow_transition_action")
public class WorkflowTransitionAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_id", nullable = false)
    private WorkflowTransitionDefinition transition;

    @Column(name = "action_key", nullable = false)
    private String actionKey;

    @Column(name = "action_params", columnDefinition = "TEXT")
    private String actionParams;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_mode", nullable = false)
    private WorkflowActionRunMode runMode = WorkflowActionRunMode.SYNC;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkflowTransitionDefinition getTransition() {
        return transition;
    }

    public void setTransition(WorkflowTransitionDefinition transition) {
        this.transition = transition;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    public String getActionParams() {
        return actionParams;
    }

    public void setActionParams(String actionParams) {
        this.actionParams = actionParams;
    }

    public WorkflowActionRunMode getRunMode() {
        return runMode;
    }

    public void setRunMode(WorkflowActionRunMode runMode) {
        this.runMode = runMode;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}
