package com.vbot.navigator.model;

import java.util.List;

public class OperationAlignment {
    private final ApiOperation operation;
    private final BianServiceDomain domain;
    private final double score;
    private final List<String> matchedKeywords;

    public OperationAlignment(ApiOperation operation, BianServiceDomain domain, double score, List<String> matchedKeywords) {
        this.operation = operation;
        this.domain = domain;
        this.score = score;
        this.matchedKeywords = matchedKeywords;
    }

    public ApiOperation operation() {
        return operation;
    }

    public BianServiceDomain domain() {
        return domain;
    }

    public double score() {
        return score;
    }

    public List<String> matchedKeywords() {
        return matchedKeywords;
    }
}
