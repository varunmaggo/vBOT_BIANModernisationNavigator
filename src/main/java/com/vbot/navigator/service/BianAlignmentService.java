package com.vbot.navigator.service;

import com.vbot.navigator.config.BianDomainCatalogue;
import com.vbot.navigator.model.ApiOperation;
import com.vbot.navigator.model.BianServiceDomain;
import com.vbot.navigator.model.OperationAlignment;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class BianAlignmentService {

    private static final double MIN_SCORE = 0.3;

    private final BianDomainCatalogue catalogue;
    private final JaroWinklerDistance distance = new JaroWinklerDistance();

    public BianAlignmentService(BianDomainCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    public List<OperationAlignment> align(List<ApiOperation> operations) {
        List<OperationAlignment> aligned = new ArrayList<>();
        for (ApiOperation op : operations) {
            aligned.add(bestDomainFor(op));
        }
        return aligned;
    }

    private OperationAlignment bestDomainFor(ApiOperation op) {
        String searchableText = op.searchableText();
        double bestScore = Double.NEGATIVE_INFINITY;
        BianServiceDomain bestDomain = catalogue.fallbackDomain();
        List<String> matchedKeywords = List.of();

        for (BianServiceDomain domain : catalogue.domains()) {
            ScoredMatch match = score(domain, searchableText, op.tags());
            if (match.score() > bestScore) {
                bestScore = match.score();
                bestDomain = domain;
                matchedKeywords = match.keywords();
            }
        }

        if (bestScore < MIN_SCORE) {
            return new OperationAlignment(op, catalogue.fallbackDomain(), 0, Collections.emptyList());
        }
        return new OperationAlignment(op, bestDomain, bestScore, matchedKeywords);
    }

    private ScoredMatch score(BianServiceDomain domain, String searchableText, List<String> tags) {
        List<String> matched = new ArrayList<>();
        double score = 0.0;

        for (String keyword : domain.getKeywords()) {
            String normalized = keyword.toLowerCase();
            if (searchableText.contains(normalized)) {
                matched.add(keyword);
                score += 1.2;
            } else {
                double jw = distance.apply(searchableText, normalized);
                if (jw != null && jw > 0.9) {
                    matched.add(keyword);
                    score += 0.8;
                }
            }
        }

        if (tags != null) {
            for (String tag : tags) {
                for (String keyword : domain.getKeywords()) {
                    if (tag != null && tag.toLowerCase().contains(keyword.toLowerCase())) {
                        matched.add(keyword);
                        score += 0.8;
                    }
                }
            }
        }

        score += similarityToName(searchableText, domain);

        return new ScoredMatch(score, matched);
    }

    private double similarityToName(String searchableText, BianServiceDomain domain) {
        double nameScore = 0.0;
        if (domain.getName() != null && !domain.getName().isBlank()) {
            String name = domain.getName().toLowerCase();
            if (searchableText.contains(name)) {
                nameScore += 1.0;
            } else {
                Double jw = distance.apply(searchableText, name);
                if (jw != null) {
                    nameScore += jw / 2;
                }
            }
        }
        if (domain.getCode() != null && !domain.getCode().isBlank() && searchableText.contains(domain.getCode().toLowerCase())) {
            nameScore += 0.5;
        }
        return nameScore;
    }

    private record ScoredMatch(double score, List<String> keywords) {
    }
}
