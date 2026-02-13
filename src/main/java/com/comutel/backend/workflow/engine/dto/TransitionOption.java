package com.comutel.backend.workflow.engine.dto;

public class TransitionOption {
    private Long transitionId;
    private String eventKey;
    private String name;
    private String toStateKey;

    public Long getTransitionId() { return transitionId; }
    public void setTransitionId(Long transitionId) { this.transitionId = transitionId; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getToStateKey() { return toStateKey; }
    public void setToStateKey(String toStateKey) { this.toStateKey = toStateKey; }
}
