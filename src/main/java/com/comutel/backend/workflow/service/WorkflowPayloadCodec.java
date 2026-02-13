package com.comutel.backend.workflow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class WorkflowPayloadCodec {

    private WorkflowPayloadCodec() {
    }

    public static String encode(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String key = escape(entry.getKey());
            String value = escape(String.valueOf(entry.getValue()));
            joiner.add(key + "=" + value);
        }
        return joiner.toString();
    }

    public static Map<String, String> decode(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return map;
        }

        StringBuilder token = new StringBuilder();
        boolean escaped = false;
        java.util.List<String> parts = new java.util.ArrayList<>();
        for (char c : raw.toCharArray()) {
            if (escaped) {
                token.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == ';') {
                parts.add(token.toString());
                token.setLength(0);
                continue;
            }
            token.append(c);
        }
        parts.add(token.toString());

        for (String part : parts) {
            if (part.isBlank() || !part.contains("=")) {
                continue;
            }
            int idx = part.indexOf('=');
            String k = unescape(part.substring(0, idx));
            String v = unescape(part.substring(idx + 1));
            map.put(k, v);
        }

        return map;
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace(";", "\\;").replace("=", "\\=");
    }

    private static String unescape(String input) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (char c : input.toCharArray()) {
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
