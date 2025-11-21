package com.vbot.navigator.model;

import java.util.Collections;
import java.util.List;

public class DomainAlignment {
    private final BianServiceDomain domain;
    private final List<OperationAlignment> operations;

    public DomainAlignment(BianServiceDomain domain, List<OperationAlignment> operations) {
        this.domain = domain;
        this.operations = operations;
    }

    public BianServiceDomain domain() {
        return domain;
    }

    public List<OperationAlignment> operations() {
        return Collections.unmodifiableList(operations);
    }

    public boolean isFallback() {
        return "UNMAPPED".equalsIgnoreCase(domain.getCode());
    }
}
