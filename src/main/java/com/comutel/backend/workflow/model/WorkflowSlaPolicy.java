package com.comutel.backend.workflow.model;

import com.comutel.backend.model.GrupoResolutor;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_sla_policy")
public class WorkflowSlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "response_minutes")
    private Integer responseMinutes;

    @Column(name = "resolution_minutes")
    private Integer resolutionMinutes;

    @Column(name = "warning_minutes")
    private Integer warningMinutes;

    @Column(name = "escalation_event_key")
    private String escalationEventKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalation_group_id")
    private GrupoResolutor escalationGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getResponseMinutes() {
        return responseMinutes;
    }

    public void setResponseMinutes(Integer responseMinutes) {
        this.responseMinutes = responseMinutes;
    }

    public Integer getResolutionMinutes() {
        return resolutionMinutes;
    }

    public void setResolutionMinutes(Integer resolutionMinutes) {
        this.resolutionMinutes = resolutionMinutes;
    }

    public Integer getWarningMinutes() {
        return warningMinutes;
    }

    public void setWarningMinutes(Integer warningMinutes) {
        this.warningMinutes = warningMinutes;
    }

    public String getEscalationEventKey() {
        return escalationEventKey;
    }

    public void setEscalationEventKey(String escalationEventKey) {
        this.escalationEventKey = escalationEventKey;
    }

    public GrupoResolutor getEscalationGroup() {
        return escalationGroup;
    }

    public void setEscalationGroup(GrupoResolutor escalationGroup) {
        this.escalationGroup = escalationGroup;
    }
}
