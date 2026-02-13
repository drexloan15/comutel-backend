package com.comutel.backend.workflow.rules.impl;

import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.rules.RuleEvaluator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

@Component
public class ExpressionRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean evaluate(String expression, RuleContext context) {
        if (expression == null || expression.isBlank()) {
            return true;
        }

        String[] clauses = expression.split("&&");
        for (String rawClause : clauses) {
            String clause = rawClause.trim();
            if (clause.isEmpty()) {
                continue;
            }
            if (!evaluateClause(clause, context)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateClause(String clause, RuleContext context) {
        String[] ops = new String[]{">=", "<=", "!=", "==", ">", "<", "~="};
        for (String op : ops) {
            int idx = clause.indexOf(op);
            if (idx > 0) {
                String left = clause.substring(0, idx).trim();
                String right = clause.substring(idx + op.length()).trim();
                return compare(resolveValue(left, context), right, op);
            }
        }
        return false;
    }

    private String resolveValue(String key, RuleContext context) {
        if (context == null) return "";

        Map<String, Object> payload = context.getPayload();
        if (payload != null && payload.containsKey(key)) {
            return String.valueOf(payload.get(key));
        }

        if ("actorId".equalsIgnoreCase(key) && context.getActor() != null) {
            return String.valueOf(context.getActor().getId());
        }

        if ("currentState".equalsIgnoreCase(key) && context.getInstance() != null) {
            return context.getInstance().getCurrentStateKey();
        }

        if ("processType".equalsIgnoreCase(key) && context.getDefinition() != null) {
            return context.getDefinition().getProcessType();
        }

        return "";
    }

    private boolean compare(String leftRaw, String rightRaw, String op) {
        String right = stripQuotes(rightRaw);
        String left = stripQuotes(leftRaw);

        if (">".equals(op) || "<".equals(op) || ">=".equals(op) || "<=".equals(op)) {
            try {
                BigDecimal l = new BigDecimal(left);
                BigDecimal r = new BigDecimal(right);
                return switch (op) {
                    case ">" -> l.compareTo(r) > 0;
                    case "<" -> l.compareTo(r) < 0;
                    case ">=" -> l.compareTo(r) >= 0;
                    case "<=" -> l.compareTo(r) <= 0;
                    default -> false;
                };
            } catch (Exception ignored) {
                return false;
            }
        }

        if ("~=".equals(op)) {
            return left.toLowerCase(Locale.ROOT).contains(right.toLowerCase(Locale.ROOT));
        }

        if ("==".equals(op)) {
            return left.equalsIgnoreCase(right);
        }

        if ("!=".equals(op)) {
            return !left.equalsIgnoreCase(right);
        }

        return false;
    }

    private String stripQuotes(String value) {
        String trimmed = value == null ? "" : value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
                (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
