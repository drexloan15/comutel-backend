package com.comutel.backend.workflow.service;

import com.comutel.backend.workflow.engine.WorkflowEngine;
import com.comutel.backend.workflow.engine.dto.FireTransitionCommand;
import com.comutel.backend.workflow.model.WorkflowInstance;
import com.comutel.backend.workflow.model.WorkflowInstanceStatus;
import com.comutel.backend.workflow.model.WorkflowStateDefinition;
import com.comutel.backend.workflow.repository.WorkflowInstanceRepository;
import com.comutel.backend.workflow.repository.WorkflowStateDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkflowSlaSchedulerService {

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowStateDefinitionRepository stateRepository;

    @Autowired
    private WorkflowEngine workflowEngine;

    @Scheduled(fixedRate = 60000)
    public void processExpiredSla() {
        List<WorkflowInstance> expired = instanceRepository.findByStatusAndDueAtBefore(
                WorkflowInstanceStatus.RUNNING,
                LocalDateTime.now()
        );

        for (WorkflowInstance instance : expired) {
            WorkflowStateDefinition state = stateRepository
                    .findByWorkflowDefinitionIdAndStateKey(
                            instance.getWorkflowDefinition().getId(),
                            instance.getCurrentStateKey()
                    )
                    .orElse(null);

            if (state == null || state.getSlaPolicy() == null || state.getSlaPolicy().getEscalationEventKey() == null) {
                continue;
            }

            try {
                instance.setDueAt(LocalDateTime.now().plusMinutes(5));
                instanceRepository.save(instance);

                FireTransitionCommand cmd = new FireTransitionCommand();
                cmd.setInstanceId(instance.getId());
                cmd.setEventKey(state.getSlaPolicy().getEscalationEventKey());
                cmd.setActorUserId(null);

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("reason", "SLA_EXPIRED");
                if (state.getSlaPolicy().getEscalationGroup() != null) {
                    payload.put("grupoId", state.getSlaPolicy().getEscalationGroup().getId());
                }
                cmd.setPayload(payload);

                workflowEngine.fire(cmd);
            } catch (Exception ex) {
                System.err.println("Error ejecutando escalamiento SLA para instance " + instance.getId() + ": " + ex.getMessage());
            }
        }
    }
}
