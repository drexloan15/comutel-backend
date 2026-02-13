package com.comutel.backend.workflow.engine.impl;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.GrupoResolutorRepository;
import com.comutel.backend.repository.UsuarioRepository;
import com.comutel.backend.workflow.actions.ActionDispatcher;
import com.comutel.backend.workflow.engine.AssignmentResolver;
import com.comutel.backend.workflow.engine.WorkflowDefinitionProvider;
import com.comutel.backend.workflow.engine.WorkflowEngine;
import com.comutel.backend.workflow.engine.dto.*;
import com.comutel.backend.workflow.model.*;
import com.comutel.backend.workflow.repository.*;
import com.comutel.backend.workflow.rules.RuleEvaluator;
import com.comutel.backend.workflow.service.WorkflowPayloadCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultWorkflowEngine implements WorkflowEngine {

    @Autowired
    private WorkflowDefinitionProvider definitionProvider;

    @Autowired
    private WorkflowStateDefinitionRepository stateRepository;

    @Autowired
    private WorkflowTransitionDefinitionRepository transitionRepository;

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowInstanceTransitionLogRepository transitionLogRepository;

    @Autowired
    private WorkflowTaskRepository taskRepository;

    @Autowired
    private WorkflowOutboxEventRepository outboxRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoResolutorRepository grupoResolutorRepository;

    @Autowired
    private RuleEvaluator ruleEvaluator;

    @Autowired
    private AssignmentResolver assignmentResolver;

    @Autowired
    private ActionDispatcher actionDispatcher;

    @Override
    @Transactional
    public WorkflowInstance start(StartWorkflowCommand command) {
        if (command.getEntityType() == null || command.getEntityId() == null) {
            throw new RuntimeException("entityType y entityId son obligatorios para iniciar workflow");
        }

        instanceRepository.findByEntityTypeAndEntityId(command.getEntityType(), command.getEntityId())
                .ifPresent(existing -> {
                    throw new RuntimeException("Ya existe workflow instance para entity=" + command.getEntityType() + ":" + command.getEntityId());
                });

        WorkflowDefinition definition = definitionProvider.loadActive(command.getWorkflowKey(), command.getProcessType());
        WorkflowStateDefinition startState = stateRepository
                .findByWorkflowDefinitionIdAndStateType(definition.getId(), WorkflowStateType.START)
                .orElseThrow(() -> new RuntimeException("Workflow sin estado START: " + definition.getKey()));

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowDefinition(definition);
        instance.setEntityType(command.getEntityType());
        instance.setEntityId(command.getEntityId());
        instance.setCurrentStateKey(startState.getStateKey());
        instance.setStatus(WorkflowInstanceStatus.RUNNING);
        instance.setLastEventKey("START");
        applySla(instance, startState);
        instanceRepository.save(instance);

        Usuario actor = findActor(command.getActorUserId());
        RuleContext context = buildContext(definition, instance, actor, command.getPayload());

        Assignment assignment = assignmentResolver.resolve(instance, startState, context);
        reopenTask(instance, startState.getStateKey(), assignment);

        logTransition(instance, null, startState.getStateKey(), "START", actor, command.getPayload(), "OK", 0L);
        emitOutbox("WORKFLOW.STARTED", instance, command.getPayload());

        return instance;
    }

    @Override
    @Transactional
    public TransitionResult fire(FireTransitionCommand command) {
        long startNanos = System.nanoTime();

        WorkflowInstance instance = instanceRepository.findById(command.getInstanceId())
                .orElseThrow(() -> new RuntimeException("Workflow instance no encontrado"));

        if (instance.getStatus() != WorkflowInstanceStatus.RUNNING) {
            throw new RuntimeException("Workflow instance no esta en ejecucion");
        }

        WorkflowDefinition definition = definitionProvider.loadById(instance.getWorkflowDefinition().getId());
        Usuario actor = findActor(command.getActorUserId());
        RuleContext context = buildContext(definition, instance, actor, command.getPayload());

        List<WorkflowTransitionDefinition> candidates = transitionRepository
                .findByWorkflowDefinitionIdAndFromStateKeyAndEventKeyAndActiveTrueOrderByPriorityAsc(
                        definition.getId(),
                        instance.getCurrentStateKey(),
                        command.getEventKey()
                );

        WorkflowTransitionDefinition selected = null;
        for (WorkflowTransitionDefinition candidate : candidates) {
            if (ruleEvaluator.evaluate(candidate.getConditionExpression(), context)) {
                selected = candidate;
                break;
            }
        }

        if (selected == null) {
            throw new RuntimeException("No existe transicion valida para estado=" + instance.getCurrentStateKey() + " evento=" + command.getEventKey());
        }

        String fromState = instance.getCurrentStateKey();
        String toState = selected.getToStateKey();

        WorkflowStateDefinition targetState = stateRepository
                .findByWorkflowDefinitionIdAndStateKey(definition.getId(), toState)
                .orElseThrow(() -> new RuntimeException("Estado destino no definido: " + toState));

        instance.setCurrentStateKey(toState);
        instance.setLastEventKey(command.getEventKey());
        if (targetState.getStateType() == WorkflowStateType.END) {
            instance.setStatus(WorkflowInstanceStatus.COMPLETED);
        }

        applySla(instance, targetState);
        actionDispatcher.dispatch(selected.getActions(), context);

        Assignment assignment = assignmentResolver.resolve(instance, targetState, context);
        closeOpenTasks(instance);
        if (instance.getStatus() == WorkflowInstanceStatus.RUNNING) {
            reopenTask(instance, toState, assignment);
        }

        instanceRepository.save(instance);

        long executionMs = (System.nanoTime() - startNanos) / 1_000_000L;
        logTransition(instance, fromState, toState, command.getEventKey(), actor, command.getPayload(), "OK", executionMs);
        emitOutbox("WORKFLOW.TRANSITION", instance, command.getPayload());

        TransitionResult result = new TransitionResult();
        result.setChanged(true);
        result.setInstanceId(instance.getId());
        result.setFromState(fromState);
        result.setToState(toState);
        result.setEventKey(command.getEventKey());
        result.setMessage("Transicion aplicada");
        return result;
    }

    @Override
    public List<TransitionOption> getAvailableTransitions(Long instanceId, Long actorUserId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Workflow instance no encontrado"));

        WorkflowDefinition definition = definitionProvider.loadById(instance.getWorkflowDefinition().getId());
        Usuario actor = findActor(actorUserId);
        RuleContext context = buildContext(definition, instance, actor, java.util.Map.of());

        List<WorkflowTransitionDefinition> transitions = transitionRepository
                .findByWorkflowDefinitionIdAndFromStateKeyAndActiveTrueOrderByPriorityAsc(
                        definition.getId(),
                        instance.getCurrentStateKey()
                );

        List<TransitionOption> options = new ArrayList<>();
        for (WorkflowTransitionDefinition transition : transitions) {
            if (!ruleEvaluator.evaluate(transition.getConditionExpression(), context)) {
                continue;
            }
            TransitionOption option = new TransitionOption();
            option.setTransitionId(transition.getId());
            option.setEventKey(transition.getEventKey());
            option.setName(transition.getName());
            option.setToStateKey(transition.getToStateKey());
            options.add(option);
        }

        return options;
    }

    private RuleContext buildContext(WorkflowDefinition definition, WorkflowInstance instance, Usuario actor, java.util.Map<String, Object> payload) {
        RuleContext context = new RuleContext();
        context.setDefinition(definition);
        context.setInstance(instance);
        context.setActor(actor);
        if (payload != null) {
            context.setPayload(new java.util.HashMap<>(payload));
        }
        return context;
    }

    private Usuario findActor(Long actorUserId) {
        if (actorUserId == null) {
            return null;
        }
        return usuarioRepository.findById(actorUserId).orElse(null);
    }

    private void applySla(WorkflowInstance instance, WorkflowStateDefinition stateDefinition) {
        if (stateDefinition.getSlaPolicy() == null || stateDefinition.getSlaPolicy().getResolutionMinutes() == null) {
            instance.setDueAt(null);
            return;
        }

        instance.setDueAt(LocalDateTime.now().plusMinutes(stateDefinition.getSlaPolicy().getResolutionMinutes()));
    }

    private void closeOpenTasks(WorkflowInstance instance) {
        List<WorkflowTask> tasks = taskRepository.findByWorkflowInstanceIdOrderByCreatedAtDesc(instance.getId());
        for (WorkflowTask task : tasks) {
            if (task.getStatus() == WorkflowTaskStatus.OPEN || task.getStatus() == WorkflowTaskStatus.IN_PROGRESS) {
                task.setStatus(WorkflowTaskStatus.DONE);
                task.setClosedAt(LocalDateTime.now());
            }
        }
        taskRepository.saveAll(tasks);
    }

    private void reopenTask(WorkflowInstance instance, String stateKey, Assignment assignment) {
        WorkflowTask task = new WorkflowTask();
        task.setWorkflowInstance(instance);
        task.setStateKey(stateKey);
        task.setStatus(WorkflowTaskStatus.OPEN);
        task.setDueAt(instance.getDueAt());

        if (assignment != null) {
            if (assignment.getAssigneeUserId() != null) {
                usuarioRepository.findById(assignment.getAssigneeUserId()).ifPresent(task::setAssigneeUser);
            }
            if (assignment.getAssigneeGroupId() != null) {
                grupoResolutorRepository.findById(assignment.getAssigneeGroupId()).ifPresent(task::setAssigneeGroup);
            }
        }

        taskRepository.save(task);
    }

    private void logTransition(
            WorkflowInstance instance,
            String fromState,
            String toState,
            String eventKey,
            Usuario actor,
            java.util.Map<String, Object> payload,
            String result,
            Long executionMs
    ) {
        WorkflowInstanceTransitionLog log = new WorkflowInstanceTransitionLog();
        log.setWorkflowInstance(instance);
        log.setFromStateKey(fromState);
        log.setToStateKey(toState);
        log.setEventKey(eventKey);
        log.setActorUser(actor);
        log.setPayload(WorkflowPayloadCodec.encode(payload));
        log.setResult(result);
        log.setExecutionMs(executionMs);
        transitionLogRepository.save(log);
    }

    private void emitOutbox(String eventType, WorkflowInstance instance, java.util.Map<String, Object> payload) {
        WorkflowOutboxEvent event = new WorkflowOutboxEvent();
        event.setEventType(eventType);
        event.setAggregateType(instance.getEntityType());
        event.setAggregateId(instance.getEntityId());
        event.setPayload(WorkflowPayloadCodec.encode(payload));
        event.setStatus(WorkflowOutboxStatus.PENDING);
        event.setAvailableAt(LocalDateTime.now());
        outboxRepository.save(event);
    }
}
