package com.comutel.backend.workflow.engine.dto;

import java.util.HashMap;
import java.util.Map;

public class StartWorkflowCommand {
    private String workflowKey;
    private String processType;
    private String entityType;
    private Long entityId;
    private Long actorUserId;
    private Map<String, Object> payload = new HashMap<>();

    public String getWorkflowKey() { return workflowKey; }
    public void setWorkflowKey(String workflowKey) { this.workflowKey = workflowKey; }
    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
