package com.comutel.backend.workflow.engine;

import com.comutel.backend.workflow.engine.dto.FireTransitionCommand;
import com.comutel.backend.workflow.engine.dto.StartWorkflowCommand;
import com.comutel.backend.workflow.engine.dto.TransitionOption;
import com.comutel.backend.workflow.engine.dto.TransitionResult;
import com.comutel.backend.workflow.model.WorkflowInstance;

import java.util.List;

public interface WorkflowEngine {
    WorkflowInstance start(StartWorkflowCommand command);
    TransitionResult fire(FireTransitionCommand command);
    List<TransitionOption> getAvailableTransitions(Long instanceId, Long actorUserId);
}
