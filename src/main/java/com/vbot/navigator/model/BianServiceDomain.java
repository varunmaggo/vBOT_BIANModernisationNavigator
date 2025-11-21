package com.vbot.navigator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BianServiceDomain {

    private String code;
    private String name;
    private List<String> keywords = new ArrayList<>();
    private List<String> capabilities = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords == null ? new ArrayList<>() : new ArrayList<>(keywords);
    }

    public List<String> getCapabilities() {
        return Collections.unmodifiableList(capabilities);
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities == null ? new ArrayList<>() : new ArrayList<>(capabilities);
    }

    @JsonProperty("searchLabel")
    public String searchLabel() {
        return (code + " " + name).toLowerCase();
    }
}
