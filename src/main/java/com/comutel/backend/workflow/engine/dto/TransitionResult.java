package com.comutel.backend.workflow.engine.dto;

public class TransitionResult {
    private boolean changed;
    private Long instanceId;
    private String fromState;
    private String toState;
    private String eventKey;
    private String message;

    public boolean isChanged() { return changed; }
    public void setChanged(boolean changed) { this.changed = changed; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public String getFromState() { return fromState; }
    public void setFromState(String fromState) { this.fromState = fromState; }
    public String getToState() { return toState; }
    public void setToState(String toState) { this.toState = toState; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
