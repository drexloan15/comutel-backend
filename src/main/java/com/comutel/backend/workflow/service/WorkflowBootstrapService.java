package com.comutel.backend.workflow.service;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.repository.GrupoResolutorRepository;
import com.comutel.backend.workflow.model.*;
import com.comutel.backend.workflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WorkflowBootstrapService {

    @Autowired
    private WorkflowDefinitionRepository definitionRepository;

    @Autowired
    private WorkflowStateDefinitionRepository stateRepository;

    @Autowired
    private WorkflowTransitionDefinitionRepository transitionRepository;

    @Autowired
    private WorkflowAssignmentRuleRepository assignmentRuleRepository;

    @Autowired
    private WorkflowSlaPolicyRepository slaPolicyRepository;

    @Autowired
    private GrupoResolutorRepository grupoResolutorRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapDefaults() {
        WorkflowSlaPolicy incidentSla = ensureSlaPolicy("SLA_INCIDENT_DEFAULT", 30, 240, "ESCALATE");
        WorkflowSlaPolicy requestSla = ensureSlaPolicy("SLA_REQUEST_DEFAULT", 60, 480, "ESCALATE");
        WorkflowSlaPolicy changeSla = ensureSlaPolicy("SLA_CHANGE_DEFAULT", 120, 1440, "ESCALATE");
        WorkflowSlaPolicy approvalSla = ensureSlaPolicy("SLA_APPROVAL_DEFAULT", 15, 120, "ESCALATE");

        ensureWorkflow("INCIDENT_DEFAULT", "Incidencias", "INCIDENCIA", incidentSla);
        ensureWorkflow("REQUEST_DEFAULT", "Requerimientos", "REQUERIMIENTO", requestSla);
        ensureWorkflow("CHANGE_DEFAULT", "Cambios", "CAMBIO", changeSla);
        ensureWorkflow("APPROVAL_DEFAULT", "Aprobaciones", "APROBACION", approvalSla);
    }

    private WorkflowSlaPolicy ensureSlaPolicy(String name, Integer response, Integer resolution, String escalationEvent) {
        return slaPolicyRepository.findByName(name).orElseGet(() -> {
            WorkflowSlaPolicy policy = new WorkflowSlaPolicy();
            policy.setName(name);
            policy.setResponseMinutes(response);
            policy.setResolutionMinutes(resolution);
            policy.setEscalationEventKey(escalationEvent);
            GrupoResolutor group = grupoResolutorRepository.findAll().stream().findFirst().orElse(null);
            policy.setEscalationGroup(group);
            return slaPolicyRepository.save(policy);
        });
    }

    private void ensureWorkflow(String key, String name, String processType, WorkflowSlaPolicy slaPolicy) {
        if (definitionRepository.existsByKeyAndProcessType(key, processType)) {
            return;
        }

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setKey(key);
        definition.setName(name);
        definition.setProcessType(processType);
        definition.setVersion(1);
        definition.setStatus(WorkflowDefinitionStatus.PUBLISHED);
        definition.setActive(true);
        definition.setPublishedAt(LocalDateTime.now());
        definition = definitionRepository.save(definition);

        createState(definition, "NEW", "Nuevo", WorkflowStateType.START, "NUEVO", slaPolicy);
        createState(definition, "ASSIGNED", "Asignado", WorkflowStateType.NORMAL, "EN_PROCESO", slaPolicy);
        createState(definition, "IN_PROGRESS", "En Proceso", WorkflowStateType.NORMAL, "EN_PROCESO", slaPolicy);
        createState(definition, "RESOLVED", "Resuelto", WorkflowStateType.NORMAL, "RESUELTO", null);
        createState(definition, "CLOSED", "Cerrado", WorkflowStateType.END, "CERRADO", null);

        createTransition(definition, "NEW", "ASSIGNED", "ASSIGN_TECHNICIAN", "Asignar Tecnico", "tecnicoId!=null", 10);
        createTransition(definition, "NEW", "IN_PROGRESS", "TAKE_OWNERSHIP", "Tomar Caso", "tecnicoId!=null", 20);
        createTransition(definition, "NEW", "IN_PROGRESS", "NEXT", "Siguiente", null, 100);

        createTransition(definition, "ASSIGNED", "IN_PROGRESS", "START_WORK", "Iniciar Trabajo", null, 10);
        createTransition(definition, "ASSIGNED", "IN_PROGRESS", "TAKE_OWNERSHIP", "Tomar Caso", "tecnicoId!=null", 15);
        createTransition(definition, "ASSIGNED", "RESOLVED", "RESOLVE", "Resolver", null, 20);
        createTransition(definition, "ASSIGNED", "IN_PROGRESS", "NEXT", "Siguiente", null, 100);

        createTransition(definition, "IN_PROGRESS", "IN_PROGRESS", "TAKE_OWNERSHIP", "Reasignar Caso", "tecnicoId!=null", 5);
        createTransition(definition, "IN_PROGRESS", "RESOLVED", "RESOLVE", "Resolver", null, 10);
        createTransition(definition, "IN_PROGRESS", "RESOLVED", "NEXT", "Siguiente", null, 100);

        createTransition(definition, "RESOLVED", "CLOSED", "CLOSE", "Cerrar", null, 10);
        createTransition(definition, "RESOLVED", "CLOSED", "NEXT", "Siguiente", null, 100);

        createTransition(definition, "NEW", "ASSIGNED", "ESCALATE", "Escalar", null, 5);
        createTransition(definition, "ASSIGNED", "ASSIGNED", "ESCALATE", "Escalar", null, 5);
        createTransition(definition, "IN_PROGRESS", "ASSIGNED", "ESCALATE", "Escalar", null, 5);

        createAssignmentRule(definition, "NEW", WorkflowAssignmentStrategy.GROUP_DEFAULT, null, 1);
        createAssignmentRule(definition, "ASSIGNED", WorkflowAssignmentStrategy.PAYLOAD, null, 1);
        createAssignmentRule(definition, "IN_PROGRESS", WorkflowAssignmentStrategy.PAYLOAD, null, 1);
    }

    private void createState(
            WorkflowDefinition definition,
            String stateKey,
            String name,
            WorkflowStateType stateType,
            String externalStatus,
            WorkflowSlaPolicy slaPolicy
    ) {
        WorkflowStateDefinition state = new WorkflowStateDefinition();
        state.setWorkflowDefinition(definition);
        state.setStateKey(stateKey);
        state.setName(name);
        state.setStateType(stateType);
        state.setExternalStatus(externalStatus);
        state.setSlaPolicy(slaPolicy);
        stateRepository.save(state);
    }

    private void createTransition(
            WorkflowDefinition definition,
            String from,
            String to,
            String event,
            String name,
            String condition,
            int priority
    ) {
        WorkflowTransitionDefinition transition = new WorkflowTransitionDefinition();
        transition.setWorkflowDefinition(definition);
        transition.setFromStateKey(from);
        transition.setToStateKey(to);
        transition.setEventKey(event);
        transition.setName(name);
        transition.setConditionExpression(condition);
        transition.setPriority(priority);
        transition.setActive(true);
        transitionRepository.save(transition);
    }

    private void createAssignmentRule(
            WorkflowDefinition definition,
            String stateKey,
            WorkflowAssignmentStrategy strategy,
            String expression,
            int priority
    ) {
        WorkflowAssignmentRule rule = new WorkflowAssignmentRule();
        rule.setWorkflowDefinition(definition);
        rule.setStateKey(stateKey);
        rule.setStrategy(strategy);
        rule.setExpression(expression);
        rule.setPriorityOrder(priority);
        rule.setActive(true);

        if (strategy == WorkflowAssignmentStrategy.GROUP_DEFAULT) {
            List<GrupoResolutor> grupos = grupoResolutorRepository.findAll();
            if (!grupos.isEmpty()) {
                rule.setTargetGroup(grupos.get(0));
            }
        }

        assignmentRuleRepository.save(rule);
    }
}
