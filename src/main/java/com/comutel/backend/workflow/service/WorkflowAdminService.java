package com.comutel.backend.workflow.service;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.repository.GrupoResolutorRepository;
import com.comutel.backend.workflow.model.*;
import com.comutel.backend.workflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowAdminService {

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

    public List<WorkflowDefinition> listarDefiniciones(String processType) {
        if (processType == null || processType.isBlank()) {
            return definitionRepository.findAll();
        }
        return definitionRepository.findByProcessTypeOrderByKeyAscVersionDesc(processType);
    }

    public List<Map<String, Object>> listarDefinicionesView(String processType) {
        return listarDefiniciones(processType).stream()
                .map(this::toDefinitionSummary)
                .toList();
    }

    public WorkflowDefinition obtenerDefinicion(Long id) {
        return definitionRepository.findById(id).orElseThrow(() -> new RuntimeException("WorkflowDefinition no encontrada"));
    }

    public Map<String, Object> obtenerDefinicionView(Long id) {
        WorkflowDefinition definition = obtenerDefinicion(id);

        Map<String, Object> detail = toDefinitionSummary(definition);
        List<WorkflowStateDefinition> states = stateRepository.findByWorkflowDefinitionIdOrderByIdAsc(id);
        List<WorkflowTransitionDefinition> transitions = transitionRepository.findByWorkflowDefinitionIdOrderByPriorityAsc(id);
        List<WorkflowAssignmentRule> rules = assignmentRuleRepository.findByWorkflowDefinitionIdOrderByPriorityOrderAsc(id);
        List<Map<String, Object>> statesView = states.stream().map(this::toStateView).toList();
        List<Map<String, Object>> transitionsView = transitions.stream()
                .map(this::toTransitionView)
                .toList();
        List<Map<String, Object>> rulesView = rules.stream()
                .map(this::toRuleView)
                .toList();

        detail.put("states", statesView);
        detail.put("transitions", transitionsView);
        detail.put("assignmentRules", rulesView);
        detail.put("transitionsCount", transitionsView.size());
        detail.put("statesCount", statesView.size());
        detail.put("rulesCount", rulesView.size());
        return detail;
    }

    @Transactional
    public WorkflowDefinition crearDefinicion(Map<String, Object> payload) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setKey(requiredString(payload, "key"));
        definition.setName(requiredString(payload, "name"));
        definition.setProcessType(requiredString(payload, "processType"));
        definition.setVersion(parseInt(payload.get("version"), 1));
        definition.setStatus(WorkflowDefinitionStatus.DRAFT);
        definition.setActive(false);
        return definitionRepository.save(definition);
    }

    @Transactional
    public WorkflowDefinition activarDefinicion(Long id) {
        WorkflowDefinition definition = obtenerDefinicion(id);

        List<WorkflowDefinition> sameKey = definitionRepository.findByProcessTypeOrderByKeyAscVersionDesc(definition.getProcessType());
        for (WorkflowDefinition item : sameKey) {
            if (item.getKey().equalsIgnoreCase(definition.getKey()) && item.isActive() && !item.getId().equals(definition.getId())) {
                item.setActive(false);
                if (item.getStatus() == WorkflowDefinitionStatus.PUBLISHED) {
                    item.setStatus(WorkflowDefinitionStatus.ARCHIVED);
                }
                definitionRepository.save(item);
            }
        }

        definition.setActive(true);
        definition.setStatus(WorkflowDefinitionStatus.PUBLISHED);
        definition.setPublishedAt(LocalDateTime.now());
        return definitionRepository.save(definition);
    }

    @Transactional
    public WorkflowDefinition desactivarDefinicion(Long id) {
        WorkflowDefinition definition = obtenerDefinicion(id);
        definition.setActive(false);
        if (definition.getStatus() == WorkflowDefinitionStatus.PUBLISHED) {
            definition.setStatus(WorkflowDefinitionStatus.ARCHIVED);
        }
        return definitionRepository.save(definition);
    }

    @Transactional
    public WorkflowStateDefinition agregarEstado(Long definitionId, Map<String, Object> payload) {
        WorkflowDefinition definition = obtenerDefinicion(definitionId);

        WorkflowStateDefinition state = new WorkflowStateDefinition();
        state.setWorkflowDefinition(definition);
        state.setStateKey(requiredString(payload, "stateKey"));
        state.setName(requiredString(payload, "name"));
        state.setStateType(parseEnum(payload.get("stateType"), WorkflowStateType.class, WorkflowStateType.NORMAL));
        state.setExternalStatus(optionalString(payload, "externalStatus"));
        state.setUiColor(optionalString(payload, "uiColor"));

        Long slaId = parseLong(payload.get("slaPolicyId"), null);
        if (slaId != null) {
            WorkflowSlaPolicy policy = slaPolicyRepository.findById(slaId)
                    .orElseThrow(() -> new RuntimeException("SLA policy no encontrada"));
            state.setSlaPolicy(policy);
        }

        return stateRepository.save(state);
    }

    @Transactional
    public WorkflowTransitionDefinition agregarTransicion(Long definitionId, Map<String, Object> payload) {
        WorkflowDefinition definition = obtenerDefinicion(definitionId);

        WorkflowTransitionDefinition transition = new WorkflowTransitionDefinition();
        transition.setWorkflowDefinition(definition);
        transition.setFromStateKey(requiredString(payload, "fromStateKey"));
        transition.setToStateKey(requiredString(payload, "toStateKey"));
        transition.setEventKey(requiredString(payload, "eventKey"));
        transition.setName(requiredString(payload, "name"));
        transition.setConditionExpression(optionalString(payload, "conditionExpression"));
        transition.setPriority(parseInt(payload.get("priority"), 100));
        transition.setActive(parseBoolean(payload.get("active"), true));
        transition = transitionRepository.save(transition);

        Object actionsObj = payload.get("actions");
        if (actionsObj instanceof List<?> actionsList) {
            int order = 1;
            for (Object actionObj : actionsList) {
                if (!(actionObj instanceof Map<?, ?> actionMap)) continue;

                WorkflowTransitionAction action = new WorkflowTransitionAction();
                action.setTransition(transition);
                Object actionKeyRaw = actionMap.get("actionKey");
                Object actionParamsRaw = actionMap.get("actionParams");
                action.setActionKey(actionKeyRaw == null ? "" : String.valueOf(actionKeyRaw));
                action.setActionParams(actionParamsRaw == null ? "" : String.valueOf(actionParamsRaw));
                action.setRunMode(parseEnum(actionMap.get("runMode"), WorkflowActionRunMode.class, WorkflowActionRunMode.SYNC));
                action.setOrderNo(parseInt(actionMap.get("orderNo"), order++));
                transition.getActions().add(action);
            }
            transition = transitionRepository.save(transition);
        }

        return transition;
    }

    @Transactional
    public WorkflowAssignmentRule agregarReglaAsignacion(Long definitionId, Map<String, Object> payload) {
        WorkflowDefinition definition = obtenerDefinicion(definitionId);

        WorkflowAssignmentRule rule = new WorkflowAssignmentRule();
        rule.setWorkflowDefinition(definition);
        rule.setStateKey(requiredString(payload, "stateKey"));
        rule.setStrategy(parseEnum(payload.get("strategy"), WorkflowAssignmentStrategy.class, WorkflowAssignmentStrategy.NONE));
        rule.setExpression(optionalString(payload, "expression"));
        rule.setPriorityOrder(parseInt(payload.get("priorityOrder"), 100));
        rule.setActive(parseBoolean(payload.get("active"), true));

        Long groupId = parseLong(payload.get("targetGroupId"), null);
        if (groupId != null) {
            GrupoResolutor group = grupoResolutorRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
            rule.setTargetGroup(group);
        }

        return assignmentRuleRepository.save(rule);
    }

    @Transactional
    public WorkflowSlaPolicy crearSlaPolicy(Map<String, Object> payload) {
        WorkflowSlaPolicy policy = new WorkflowSlaPolicy();
        policy.setName(requiredString(payload, "name"));
        policy.setResponseMinutes(parseInt(payload.get("responseMinutes"), null));
        policy.setResolutionMinutes(parseInt(payload.get("resolutionMinutes"), null));
        policy.setWarningMinutes(parseInt(payload.get("warningMinutes"), null));
        policy.setEscalationEventKey(optionalString(payload, "escalationEventKey"));

        Long groupId = parseLong(payload.get("escalationGroupId"), null);
        if (groupId != null) {
            policy.setEscalationGroup(grupoResolutorRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Grupo de escalamiento no encontrado")));
        }

        return slaPolicyRepository.save(policy);
    }

    public List<WorkflowSlaPolicy> listarSlaPolicies() {
        return slaPolicyRepository.findAll();
    }

    private String requiredString(Map<String, Object> payload, String key) {
        String value = optionalString(payload, key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException(key + " es obligatorio");
        }
        return value;
    }

    private String optionalString(Map<String, Object> payload, String key) {
        Object raw = payload.get(key);
        return raw == null ? null : String.valueOf(raw).trim();
    }

    private Integer parseInt(Object value, Integer fallback) {
        try {
            return value == null ? fallback : Integer.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Long parseLong(Object value, Long fallback) {
        try {
            return value == null ? fallback : Long.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private boolean parseBoolean(Object value, boolean fallback) {
        if (value == null) return fallback;
        if (value instanceof Boolean b) return b;
        String raw = String.valueOf(value);
        if ("true".equalsIgnoreCase(raw)) return true;
        if ("false".equalsIgnoreCase(raw)) return false;
        return fallback;
    }

    private <T extends Enum<T>> T parseEnum(Object value, Class<T> type, T fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, String.valueOf(value).trim().toUpperCase());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Map<String, Object> toDefinitionSummary(WorkflowDefinition definition) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", definition.getId());
        view.put("key", definition.getKey());
        view.put("name", definition.getName());
        view.put("processType", definition.getProcessType());
        view.put("version", definition.getVersion());
        view.put("status", definition.getStatus().name());
        view.put("active", definition.isActive());
        view.put("publishedAt", definition.getPublishedAt());
        view.put("createdAt", definition.getCreatedAt());
        view.put("updatedAt", definition.getUpdatedAt());
        return view;
    }

    private Map<String, Object> toStateView(WorkflowStateDefinition state) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", state.getId());
        view.put("stateKey", state.getStateKey());
        view.put("name", state.getName());
        view.put("stateType", state.getStateType().name());
        view.put("externalStatus", state.getExternalStatus());
        view.put("uiColor", state.getUiColor());
        view.put("slaPolicyId", state.getSlaPolicy() != null ? state.getSlaPolicy().getId() : null);
        view.put("slaPolicyName", state.getSlaPolicy() != null ? state.getSlaPolicy().getName() : null);
        return view;
    }

    private Map<String, Object> toTransitionView(WorkflowTransitionDefinition transition) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", transition.getId());
        view.put("fromStateKey", transition.getFromStateKey());
        view.put("toStateKey", transition.getToStateKey());
        view.put("eventKey", transition.getEventKey());
        view.put("name", transition.getName());
        view.put("conditionExpression", transition.getConditionExpression());
        view.put("priority", transition.getPriority());
        view.put("active", transition.isActive());
        view.put("actions", transition.getActions().stream().map(action -> {
            Map<String, Object> actionView = new HashMap<>();
            actionView.put("id", action.getId());
            actionView.put("actionKey", action.getActionKey());
            actionView.put("actionParams", action.getActionParams());
            actionView.put("runMode", action.getRunMode().name());
            actionView.put("orderNo", action.getOrderNo());
            return actionView;
        }).toList());
        return view;
    }

    private Map<String, Object> toRuleView(WorkflowAssignmentRule rule) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", rule.getId());
        view.put("stateKey", rule.getStateKey());
        view.put("strategy", rule.getStrategy().name());
        view.put("expression", rule.getExpression());
        view.put("targetGroupId", rule.getTargetGroup() != null ? rule.getTargetGroup().getId() : null);
        view.put("priorityOrder", rule.getPriorityOrder());
        view.put("active", rule.isActive());
        return view;
    }
}
