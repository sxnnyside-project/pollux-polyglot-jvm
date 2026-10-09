package com.sxnnysideproject.pollux;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable evaluation verdict returned by PolluxEngine.
 */
public record EvaluationResult(
        boolean allowed,
        String outcome,
        String reason,
        String traceJson,
        Map<String, Object> traceDetails
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public EvaluationResult(boolean allowed, String outcome, String traceJson, Map<String, Object> traceDetails) {
        this(allowed, outcome, "", traceJson, traceDetails);
    }

    public static EvaluationResult fromTraceJson(String traceJson) {
        try {
            JsonNode root = MAPPER.readTree(traceJson);
            JsonNode decisionNode = root.get("decision");
            
            String outcome = "deny";
            String reason = "";
            if (decisionNode != null) {
                if (decisionNode.isObject()) {
                    outcome = decisionNode.path("outcome").asText("deny");
                    reason = decisionNode.path("reason").asText("");
                } else if (decisionNode.isTextual()) {
                    outcome = decisionNode.asText("deny");
                }
            }
            
            boolean allowed = "allow".equalsIgnoreCase(outcome);
            Map<String, Object> map = MAPPER.convertValue(root, new TypeReference<>() {});
            return new EvaluationResult(allowed, outcome, reason, traceJson, map);
        } catch (Exception e) {
            // Resilient fallback for any non-standard payload
            boolean allowed = traceJson.contains("\"outcome\":\"allow\"") || traceJson.contains("\"outcome\": \"allow\"");
            String outcome = allowed ? "allow" : "deny";
            return new EvaluationResult(allowed, outcome, "", traceJson, Collections.emptyMap());
        }
    }
}
