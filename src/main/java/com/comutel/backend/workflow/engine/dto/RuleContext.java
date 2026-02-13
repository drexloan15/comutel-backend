package com.comutel.backend.workflow.engine.dto;

import com.comutel.backend.model.Usuario;
import com.comutel.backend.workflow.model.WorkflowDefinition;
import com.comutel.backend.workflow.model.WorkflowInstance;

import java.util.HashMap;
import java.util.Map;

public class RuleContext {
    private WorkflowDefinition definition;
    private WorkflowInstance instance;
    private Usuario actor;
    private Map<String, Object> payload = new HashMap<>();

    public WorkflowDefinition getDefinition() { return definition; }
    public void setDefinition(WorkflowDefinition definition) { this.definition = definition; }
    public WorkflowInstance getInstance() { return instance; }
    public void setInstance(WorkflowInstance instance) { this.instance = instance; }
    public Usuario getActor() { return actor; }
    public void setActor(Usuario actor) { this.actor = actor; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
