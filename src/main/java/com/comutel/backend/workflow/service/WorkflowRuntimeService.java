package com.comutel.backend.workflow.service;

import com.comutel.backend.workflow.engine.WorkflowEngine;
import com.comutel.backend.workflow.engine.dto.FireTransitionCommand;
import com.comutel.backend.workflow.engine.dto.TransitionOption;
import com.comutel.backend.workflow.engine.dto.TransitionResult;
import com.comutel.backend.workflow.model.WorkflowInstance;
import com.comutel.backend.workflow.model.WorkflowInstanceTransitionLog;
import com.comutel.backend.workflow.model.WorkflowTask;
import com.comutel.backend.workflow.repository.WorkflowInstanceRepository;
import com.comutel.backend.workflow.repository.WorkflowInstanceTransitionLogRepository;
import com.comutel.backend.workflow.repository.WorkflowTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowRuntimeService {

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowInstanceTransitionLogRepository transitionLogRepository;

    @Autowired
    private WorkflowTaskRepository taskRepository;

    public WorkflowInstance obtenerPorEntity(String entityType, Long entityId) {
        return instanceRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .orElseThrow(() -> new RuntimeException("Workflow instance no encontrada"));
    }

    public Map<String, Object> obtenerPorEntityView(String entityType, Long entityId) {
        WorkflowInstance instance = obtenerPorEntity(entityType, entityId);
        return toInstanceView(instance);
    }

    public List<TransitionOption> obtenerTransicionesDisponibles(Long instanceId, Long actorId) {
        return workflowEngine.getAvailableTransitions(instanceId, actorId);
    }

    public TransitionResult ejecutarTransicion(Long instanceId, String eventKey, Long actorId, Map<String, Object> payload) {
        FireTransitionCommand cmd = new FireTransitionCommand();
        cmd.setInstanceId(instanceId);
        cmd.setEventKey(eventKey);
        cmd.setActorUserId(actorId);
        cmd.setPayload(payload);
        return workflowEngine.fire(cmd);
    }

    public List<WorkflowInstanceTransitionLog> obtenerLog(Long instanceId) {
        return transitionLogRepository.findByWorkflowInstanceIdOrderByExecutedAtDesc(instanceId);
    }

    public List<WorkflowTask> obtenerTareas(Long instanceId) {
        return taskRepository.findByWorkflowInstanceIdOrderByCreatedAtDesc(instanceId);
    }

    public List<Map<String, Object>> obtenerLogView(Long instanceId) {
        return obtenerLog(instanceId).stream().map(log -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", log.getId());
            row.put("fromStateKey", log.getFromStateKey());
            row.put("toStateKey", log.getToStateKey());
            row.put("eventKey", log.getEventKey());
            row.put("actorUserId", log.getActorUser() != null ? log.getActorUser().getId() : null);
            row.put("payload", log.getPayload());
            row.put("executedAt", log.getExecutedAt());
            row.put("executionMs", log.getExecutionMs());
            row.put("result", log.getResult());
            return row;
        }).toList();
    }

    public List<Map<String, Object>> obtenerTareasView(Long instanceId) {
        return obtenerTareas(instanceId).stream().map(task -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", task.getId());
            row.put("stateKey", task.getStateKey());
            row.put("assigneeUserId", task.getAssigneeUser() != null ? task.getAssigneeUser().getId() : null);
            row.put("assigneeUserName", task.getAssigneeUser() != null ? task.getAssigneeUser().getNombre() : null);
            row.put("assigneeGroupId", task.getAssigneeGroup() != null ? task.getAssigneeGroup().getId() : null);
            row.put("assigneeGroupName", task.getAssigneeGroup() != null ? task.getAssigneeGroup().getNombre() : null);
            row.put("status", task.getStatus().name());
            row.put("createdAt", task.getCreatedAt());
            row.put("dueAt", task.getDueAt());
            row.put("closedAt", task.getClosedAt());
            return row;
        }).toList();
    }

    private Map<String, Object> toInstanceView(WorkflowInstance instance) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", instance.getId());
        row.put("workflowDefinitionId", instance.getWorkflowDefinition().getId());
        row.put("workflowKey", instance.getWorkflowDefinition().getKey());
        row.put("processType", instance.getWorkflowDefinition().getProcessType());
        row.put("entityType", instance.getEntityType());
        row.put("entityId", instance.getEntityId());
        row.put("currentStateKey", instance.getCurrentStateKey());
        row.put("status", instance.getStatus().name());
        row.put("startedAt", instance.getStartedAt());
        row.put("updatedAt", instance.getUpdatedAt());
        row.put("dueAt", instance.getDueAt());
        row.put("lastEventKey", instance.getLastEventKey());
        row.put("version", instance.getVersion());
        return row;
    }
}
