package com.vbot.navigator.service;

import com.vbot.navigator.model.ApiOperation;
import com.vbot.navigator.model.DomainAlignment;
import com.vbot.navigator.model.NavigatorRequest;
import com.vbot.navigator.model.OperationAlignment;
import com.vbot.navigator.util.NameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NavigatorService {

    private static final Logger log = LoggerFactory.getLogger(NavigatorService.class);

    private final OpenApiSpecReader specReader;
    private final BianAlignmentService alignmentService;
    private final GenerationPlanWriter planWriter;
    private final MicroserviceScaffolder scaffolder;

    public NavigatorService(OpenApiSpecReader specReader,
                            BianAlignmentService alignmentService,
                            GenerationPlanWriter planWriter,
                            MicroserviceScaffolder scaffolder) {
        this.specReader = specReader;
        this.alignmentService = alignmentService;
        this.planWriter = planWriter;
        this.scaffolder = scaffolder;
    }

    public void execute(NavigatorRequest request) {
        log.info("Starting alignment for spec {}", request.specPath().toAbsolutePath());
        List<ApiOperation> operations = specReader.readOperations(request.specPath());
        List<OperationAlignment> aligned = alignmentService.align(operations);
        List<DomainAlignment> domainAlignments = groupByDomain(aligned);

        planWriter.write(request.outputDir(), request.specPath(), domainAlignments);

        if (request.generateScaffolds()) {
            Path scaffoldDir = request.outputDir().resolve("microservices");
            scaffolder.scaffold(scaffoldDir, domainAlignments);
        } else {
            log.info("Skipping microservice scaffold generation (use --scaffold to enable).");
        }

        log.info("Alignment complete. Output under {}", request.outputDir().toAbsolutePath());
    }

    private List<DomainAlignment> groupByDomain(List<OperationAlignment> alignments) {
        Map<String, List<OperationAlignment>> byCode = new HashMap<>();
        Map<String, OperationAlignment> representative = new HashMap<>();

        for (OperationAlignment alignment : alignments) {
            String code = NameUtils.safeCode(alignment.domain().getCode());
            byCode.computeIfAbsent(code, k -> new ArrayList<>()).add(alignment);
            representative.putIfAbsent(code, alignment);
        }

        List<DomainAlignment> grouped = new ArrayList<>();
        for (Map.Entry<String, List<OperationAlignment>> entry : byCode.entrySet()) {
            grouped.add(new DomainAlignment(representative.get(entry.getKey()).domain(), entry.getValue()));
        }
        return grouped;
    }
}
