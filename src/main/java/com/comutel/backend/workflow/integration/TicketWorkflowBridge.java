package com.comutel.backend.workflow.integration;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.TicketRepository;
import com.comutel.backend.workflow.engine.WorkflowEngine;
import com.comutel.backend.workflow.engine.dto.FireTransitionCommand;
import com.comutel.backend.workflow.engine.dto.StartWorkflowCommand;
import com.comutel.backend.workflow.model.*;
import com.comutel.backend.workflow.repository.WorkflowInstanceRepository;
import com.comutel.backend.workflow.repository.WorkflowStateDefinitionRepository;
import com.comutel.backend.workflow.repository.WorkflowTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class TicketWorkflowBridge {

    public static final String ENTITY_TYPE_TICKET = "TICKET";
    public static final String DEFAULT_WORKFLOW_KEY = "INCIDENT_DEFAULT";
    public static final String DEFAULT_PROCESS_TYPE = "INCIDENCIA";

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowStateDefinitionRepository workflowStateDefinitionRepository;

    @Autowired
    private WorkflowTaskRepository workflowTaskRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public Ticket startForTicket(Ticket ticket, Long actorUserId) {
        if (ticket.getWorkflowInstanceId() != null) {
            return syncFromInstance(ticket);
        }

        WorkflowInstance existing = workflowInstanceRepository
                .findByEntityTypeAndEntityId(ENTITY_TYPE_TICKET, ticket.getId())
                .orElse(null);
        if (existing != null) {
            ticket.setWorkflowInstanceId(existing.getId());
            return syncFromInstance(ticket);
        }

        StartWorkflowCommand cmd = new StartWorkflowCommand();
        cmd.setWorkflowKey(ticket.getWorkflowKey() == null ? DEFAULT_WORKFLOW_KEY : ticket.getWorkflowKey());
        cmd.setProcessType(ticket.getProcessType() == null ? DEFAULT_PROCESS_TYPE : ticket.getProcessType());
        cmd.setEntityType(ENTITY_TYPE_TICKET);
        cmd.setEntityId(ticket.getId());
        cmd.setActorUserId(actorUserId);

        Map<String, Object> payload = new HashMap<>();
        if (ticket.getGrupoAsignado() != null) payload.put("grupoId", ticket.getGrupoAsignado().getId());
        if (ticket.getTecnico() != null) payload.put("tecnicoId", ticket.getTecnico().getId());
        cmd.setPayload(payload);

        WorkflowInstance instance = workflowEngine.start(cmd);
        ticket.setWorkflowInstanceId(instance.getId());
        ticket.setWorkflowKey(instance.getWorkflowDefinition().getKey());
        ticket.setProcessType(instance.getWorkflowDefinition().getProcessType());
        ticket.setWorkflowStateKey(instance.getCurrentStateKey());

        return syncFromInstance(ticket);
    }

    @Transactional
    public Ticket fireEvent(Ticket ticket, String eventKey, Long actorUserId, Map<String, Object> payload) {
        Ticket working = ticket;
        if (working.getWorkflowInstanceId() == null) {
            working = startForTicket(working, actorUserId);
        }

        FireTransitionCommand cmd = new FireTransitionCommand();
        cmd.setInstanceId(working.getWorkflowInstanceId());
        cmd.setEventKey(eventKey);
        cmd.setActorUserId(actorUserId);
        cmd.setPayload(payload == null ? new HashMap<>() : payload);

        workflowEngine.fire(cmd);
        return syncFromInstance(working);
    }

    @Transactional
    public Ticket syncFromInstance(Ticket ticket) {
        if (ticket.getWorkflowInstanceId() == null) {
            return ticket;
        }

        WorkflowInstance instance = workflowInstanceRepository.findById(ticket.getWorkflowInstanceId())
                .orElseThrow(() -> new RuntimeException("Workflow instance no encontrado para ticket " + ticket.getId()));

        ticket.setWorkflowStateKey(instance.getCurrentStateKey());
        ticket.setWorkflowKey(instance.getWorkflowDefinition().getKey());
        ticket.setProcessType(instance.getWorkflowDefinition().getProcessType());
        ticket.setFechaVencimiento(instance.getDueAt());

        WorkflowStateDefinition state = workflowStateDefinitionRepository
                .findByWorkflowDefinitionIdAndStateKey(instance.getWorkflowDefinition().getId(), instance.getCurrentStateKey())
                .orElse(null);

        if (state != null) {
            Ticket.Estado mapped = mapExternalStatus(state.getExternalStatus(), instance.getCurrentStateKey());
            if (mapped != null) {
                ticket.setEstado(mapped);
            }
        }

        syncAssigneeFromTask(ticket, instance.getId());
        return ticketRepository.save(ticket);
    }

    private void syncAssigneeFromTask(Ticket ticket, Long workflowInstanceId) {
        List<WorkflowTask> tasks = workflowTaskRepository.findByWorkflowInstanceIdOrderByCreatedAtDesc(workflowInstanceId);
        WorkflowTask activeTask = tasks.stream()
                .filter(t -> t.getStatus() == WorkflowTaskStatus.OPEN || t.getStatus() == WorkflowTaskStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);

        if (activeTask == null) {
            return;
        }

        if (activeTask.getAssigneeGroup() != null) {
            ticket.setGrupoAsignado(activeTask.getAssigneeGroup());
        }
        if (activeTask.getAssigneeUser() != null) {
            ticket.setTecnico(activeTask.getAssigneeUser());
        }
    }

    private Ticket.Estado mapExternalStatus(String externalStatus, String stateKey) {
        String candidate = externalStatus != null && !externalStatus.isBlank() ? externalStatus : stateKey;
        if (candidate == null) {
            return null;
        }

        String normalized = candidate.trim().toUpperCase(Locale.ROOT);

        try {
            return Ticket.Estado.valueOf(normalized);
        } catch (Exception ignored) {
            // fallthrough
        }

        return switch (normalized) {
            case "NEW", "NUEVO" -> Ticket.Estado.NUEVO;
            case "ASSIGNED", "EN_PROCESO", "IN_PROGRESS" -> Ticket.Estado.EN_PROCESO;
            case "PENDING", "PENDIENTE" -> Ticket.Estado.PENDIENTE;
            case "RESOLVED", "RESUELTO" -> Ticket.Estado.RESUELTO;
            case "CLOSED", "CERRADO" -> Ticket.Estado.CERRADO;
            default -> null;
        };
    }
}
