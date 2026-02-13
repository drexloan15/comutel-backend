package com.comutel.backend.workflow.actions.impl;

import com.comutel.backend.workflow.actions.ActionDispatcher;
import com.comutel.backend.workflow.actions.WorkflowActionHandler;
import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.WorkflowTransitionAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultActionDispatcher implements ActionDispatcher {

    @Autowired(required = false)
    private List<WorkflowActionHandler> handlers;

    @Override
    public void dispatch(List<WorkflowTransitionAction> actions, RuleContext context) {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (WorkflowTransitionAction action : actions) {
            boolean handled = false;
            if (handlers != null) {
                for (WorkflowActionHandler handler : handlers) {
                    if (handler.supports(action.getActionKey())) {
                        handler.execute(action, context);
                        handled = true;
                        break;
                    }
                }
            }

            if (!handled) {
                // Accion desconocida: no bloquear transicion por compatibilidad.
                System.out.println("Workflow action no implementada: " + action.getActionKey());
            }
        }
    }
}
