package com.comutel.backend.workflow.rules;

import com.comutel.backend.workflow.engine.dto.RuleContext;

public interface RuleEvaluator {
    boolean evaluate(String expression, RuleContext context);
}
