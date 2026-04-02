package com.example.demo.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public final class ResultSnapshotSupport {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ResultSnapshotSupport() {
    }

    public static List<ResultQuestionDetail> readDetails(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            return new ArrayList<>();
        }
    }

    public static String writeDetails(List<ResultQuestionDetail> details) {
        try {
            return OBJECT_MAPPER.writeValueAsString(details != null ? details : List.of());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize result details", ex);
        }
    }
}
