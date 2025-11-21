package com.vbot.navigator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vbot.navigator.model.DomainAlignment;
import com.vbot.navigator.model.OperationAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GenerationPlanWriter {

    private static final Logger log = LoggerFactory.getLogger(GenerationPlanWriter.class);

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void write(Path outputDir, Path specPath, List<DomainAlignment> alignments) {
        try {
            Files.createDirectories(outputDir);
            writeMarkdown(outputDir.resolve("alignment-report.md"), specPath, alignments);
            writeJson(outputDir.resolve("alignment.json"), specPath, alignments);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write plan to " + outputDir, e);
        }
    }

    private void writeMarkdown(Path file, Path specPath, List<DomainAlignment> alignments) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("# Modernisation alignment report\n\n");
        builder.append("- Source spec: ").append(specPath.toAbsolutePath()).append("\n");
        builder.append("- Generated at: ").append(LocalDateTime.now()).append("\n");
        builder.append("- Domains discovered: ").append(alignments.size()).append("\n\n");

        for (DomainAlignment domain : alignments) {
            builder.append("## ").append(domain.domain().getName())
                    .append(" (").append(domain.domain().getCode()).append(")").append("\n");
            if (!domain.domain().getCapabilities().isEmpty()) {
                builder.append("- Capabilities: ").append(String.join(", ", domain.domain().getCapabilities())).append("\n");
            }
            builder.append("- Operations: ").append(domain.operations().size()).append("\n\n");
            for (OperationAlignment op : domain.operations()) {
                builder.append("  - ").append(op.operation().httpMethod())
                        .append(" ").append(op.operation().path())
                        .append(" | id: ").append(op.operation().operationId())
                        .append(" | score: ").append(String.format("%.2f", op.score()));
                if (!op.matchedKeywords().isEmpty()) {
                    builder.append(" | keywords: ").append(String.join(", ", op.matchedKeywords()));
                }
                if (op.operation().summary() != null && !op.operation().summary().isBlank()) {
                    builder.append("\n    summary: ").append(op.operation().summary());
                }
                builder.append("\n");
            }
            builder.append("\n");
        }

        Files.writeString(file, builder.toString());
        log.info("Wrote {}", file.toAbsolutePath());
    }

    private void writeJson(Path file, Path specPath, List<DomainAlignment> alignments) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("specPath", specPath.toAbsolutePath().toString());
        payload.put("generatedAt", LocalDateTime.now().toString());

        List<Map<String, Object>> domains = new ArrayList<>();
        for (DomainAlignment alignment : alignments) {
            Map<String, Object> domain = new HashMap<>();
            domain.put("code", alignment.domain().getCode());
            domain.put("name", alignment.domain().getName());
            domain.put("capabilities", alignment.domain().getCapabilities());

            List<Map<String, Object>> ops = new ArrayList<>();
            for (OperationAlignment op : alignment.operations()) {
                Map<String, Object> opNode = new HashMap<>();
                opNode.put("path", op.operation().path());
                opNode.put("httpMethod", op.operation().httpMethod());
                opNode.put("operationId", op.operation().operationId());
                opNode.put("summary", op.operation().summary());
                opNode.put("score", op.score());
                opNode.put("matchedKeywords", op.matchedKeywords());
                ops.add(opNode);
            }
            domain.put("operations", ops);
            domains.add(domain);
        }

        payload.put("domains", domains);
        mapper.writeValue(file.toFile(), payload);
        log.info("Wrote {}", file.toAbsolutePath());
    }
}
