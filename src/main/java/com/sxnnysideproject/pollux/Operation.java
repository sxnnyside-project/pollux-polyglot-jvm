package com.sxnnysideproject.pollux;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strongly typed capability operation conforming to Pollux RFC-001 / pollux-protocol/1 Wire specification.
 */
public record Operation(
        @JsonProperty("capability") String capability,
        @JsonProperty("resource_domain") String resourceDomain,
        @JsonProperty("resource_value") String resourceValue
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Operation fileRead(String path) {
        return new Operation("read", "filesystem", path);
    }

    public static Operation fileWrite(String path) {
        return new Operation("write", "filesystem", path);
    }

    public static Operation netConnect(String host) {
        return new Operation("connect", "network", host);
    }

    public static Operation procSpawn(String command) {
        return new Operation("spawn", "process", command);
    }

    public static Operation custom(String capability, String resourceDomain, String resourceValue) {
        return new Operation(capability, resourceDomain, resourceValue);
    }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new PolluxException.OperationException("Failed to serialize operation to JSON: " + e.getMessage());
        }
    }
}
