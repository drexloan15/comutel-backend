package com.comutel.backend.workflow.engine.dto;

import java.util.HashMap;
import java.util.Map;

public class FireTransitionCommand {
    private Long instanceId;
    private String eventKey;
    private Long actorUserId;
    private Map<String, Object> payload = new HashMap<>();

    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
