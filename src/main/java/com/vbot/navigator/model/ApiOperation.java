package com.vbot.navigator.model;

import java.util.Collections;
import java.util.List;

public class ApiOperation {
    private final String path;
    private final String httpMethod;
    private final String operationId;
    private final String summary;
    private final String description;
    private final List<String> tags;

    public ApiOperation(String path,
                        String httpMethod,
                        String operationId,
                        String summary,
                        String description,
                        List<String> tags) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.operationId = operationId;
        this.summary = summary;
        this.description = description;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public String path() {
        return path;
    }

    public String httpMethod() {
        return httpMethod;
    }

    public String operationId() {
        return operationId;
    }

    public String summary() {
        return summary;
    }

    public String description() {
        return description;
    }

    public List<String> tags() {
        return Collections.unmodifiableList(tags);
    }

    public String searchableText() {
        String tagText = String.join(" ", tags);
        return (path + " " + nullToEmpty(summary) + " " + nullToEmpty(description) + " " + tagText).toLowerCase();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
