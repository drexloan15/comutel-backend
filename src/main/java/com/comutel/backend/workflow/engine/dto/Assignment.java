package com.comutel.backend.workflow.engine.dto;

public class Assignment {
    private Long assigneeUserId;
    private Long assigneeGroupId;

    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public Long getAssigneeGroupId() { return assigneeGroupId; }
    public void setAssigneeGroupId(Long assigneeGroupId) { this.assigneeGroupId = assigneeGroupId; }
}
