package com.vbot.navigator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vbot.navigator.model.BianServiceDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BianDomainCatalogue {

    private static final Logger log = LoggerFactory.getLogger(BianDomainCatalogue.class);

    private final List<BianServiceDomain> domains;
    private final BianServiceDomain fallbackDomain;

    public BianDomainCatalogue() {
        this(new ClassPathResource("bian-domains.yml"));
    }

    public BianDomainCatalogue(Resource resource) {
        this.domains = load(resource);
        this.fallbackDomain = buildFallback();
    }

    public List<BianServiceDomain> domains() {
        return domains;
    }

    public BianServiceDomain fallbackDomain() {
        return fallbackDomain;
    }

    private List<BianServiceDomain> load(Resource resource) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            CatalogueFile data = mapper.readValue(resource.getInputStream(), CatalogueFile.class);
            if (data.domains == null || data.domains.isEmpty()) {
                log.warn("No BIAN domains loaded from {}. Add entries to bian-domains.yml.", resource.getFilename());
                return new ArrayList<>();
            }
            return data.domains;
        } catch (IOException e) {
            log.warn("Failed to read BIAN catalogue from {}. Proceeding with empty set.", resource.getFilename(), e);
            return new ArrayList<>();
        }
    }

    private BianServiceDomain buildFallback() {
        BianServiceDomain domain = new BianServiceDomain();
        domain.setCode("UNMAPPED");
        domain.setName("Unmapped");
        domain.setKeywords(List.of());
        domain.setCapabilities(List.of("No aligned BIAN domain found"));
        return domain;
    }

    private static class CatalogueFile {
        public List<BianServiceDomain> domains;
        public Map<String, List<String>> capabilitiesByDomain;
    }
}
