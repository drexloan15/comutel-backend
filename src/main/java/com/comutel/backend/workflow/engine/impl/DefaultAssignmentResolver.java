package com.comutel.backend.workflow.engine.impl;

import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.UsuarioRepository;
import com.comutel.backend.workflow.engine.AssignmentResolver;
import com.comutel.backend.workflow.engine.dto.Assignment;
import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.*;
import com.comutel.backend.workflow.repository.WorkflowAssignmentRuleRepository;
import com.comutel.backend.workflow.service.WorkflowPayloadCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class DefaultAssignmentResolver implements AssignmentResolver {

    @Autowired
    private WorkflowAssignmentRuleRepository assignmentRuleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Assignment resolve(WorkflowInstance instance, WorkflowStateDefinition targetState, RuleContext context) {
        Assignment assignment = fromPayload(context.getPayload());
        if (assignment.getAssigneeUserId() != null || assignment.getAssigneeGroupId() != null) {
            return assignment;
        }

        List<WorkflowAssignmentRule> rules = assignmentRuleRepository
                .findByWorkflowDefinitionIdAndStateKeyAndActiveTrueOrderByPriorityOrderAsc(
                        instance.getWorkflowDefinition().getId(),
                        targetState.getStateKey()
                );

        for (WorkflowAssignmentRule rule : rules) {
            Assignment resolved = resolveByRule(rule, context, instance);
            if (resolved.getAssigneeUserId() != null || resolved.getAssigneeGroupId() != null) {
                return resolved;
            }
        }

        return assignment;
    }

    private Assignment resolveByRule(WorkflowAssignmentRule rule, RuleContext context, WorkflowInstance instance) {
        Assignment assignment = new Assignment();

        switch (rule.getStrategy()) {
            case GROUP_DEFAULT -> {
                if (rule.getTargetGroup() != null) {
                    assignment.setAssigneeGroupId(rule.getTargetGroup().getId());
                }
            }
            case PAYLOAD -> {
                return fromPayload(context.getPayload());
            }
            case ROUND_ROBIN -> {
                if (rule.getTargetGroup() != null) {
                    Long groupId = rule.getTargetGroup().getId();
                    List<Usuario> tecnicos = usuarioRepository.findByRolAndGruposId(Usuario.Rol.TECNICO, groupId)
                            .stream()
                            .sorted(Comparator.comparing(Usuario::getId))
                            .toList();
                    if (!tecnicos.isEmpty()) {
                        int index = (int) (instance.getId() % tecnicos.size());
                        assignment.setAssigneeGroupId(groupId);
                        assignment.setAssigneeUserId(tecnicos.get(index).getId());
                    }
                }
            }
            case EXPRESSION -> {
                Map<String, String> map = WorkflowPayloadCodec.decode(rule.getExpression());
                if (map.containsKey("groupId")) {
                    assignment.setAssigneeGroupId(parseLong(map.get("groupId")));
                }
                if (map.containsKey("userId")) {
                    assignment.setAssigneeUserId(parseLong(map.get("userId")));
                }
            }
            case NONE -> {
                return assignment;
            }
        }

        return assignment;
    }

    private Assignment fromPayload(Map<String, Object> payload) {
        Assignment assignment = new Assignment();
        if (payload == null) {
            return assignment;
        }

        if (payload.get("tecnicoId") != null) {
            assignment.setAssigneeUserId(parseLong(payload.get("tecnicoId")));
        }

        if (payload.get("grupoId") != null) {
            assignment.setAssigneeGroupId(parseLong(payload.get("grupoId")));
        }

        return assignment;
    }

    private Long parseLong(Object value) {
        try {
            return value == null ? null : Long.valueOf(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }
}
