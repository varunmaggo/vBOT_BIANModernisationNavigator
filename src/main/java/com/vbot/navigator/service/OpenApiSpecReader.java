package com.vbot.navigator.service;

import com.vbot.navigator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenApiSpecReader {

    private static final Logger log = LoggerFactory.getLogger(OpenApiSpecReader.class);

    public List<ApiOperation> readOperations(Path specPath) {
        if (!Files.exists(specPath)) {
            throw new IllegalArgumentException("Spec not found: " + specPath.toAbsolutePath());
        }

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specPath.toAbsolutePath().toString(), null, options);
        OpenAPI openAPI = result == null ? null : result.getOpenAPI();
        if (openAPI == null || openAPI.getPaths() == null) {
            throw new IllegalStateException("Could not parse OpenAPI spec at " + specPath + ". Messages: " + (result != null ? result.getMessages() : "none"));
        }

        List<ApiOperation> operations = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem item = entry.getValue();
            if (item == null) {
                continue;
            }
            Map<PathItem.HttpMethod, Operation> opMap = item.readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> opEntry : opMap.entrySet()) {
                PathItem.HttpMethod method = opEntry.getKey();
                Operation operation = opEntry.getValue();
                operations.add(toApiOperation(path, method, operation));
            }
        }

        log.info("Parsed {} operations from {}", operations.size(), specPath);
        return operations;
    }

    private ApiOperation toApiOperation(String path, PathItem.HttpMethod method, Operation operation) {
        String operationId = operation.getOperationId();
        if (operationId == null || operationId.isBlank()) {
            operationId = (method.name().toLowerCase() + "_" + path.replace("/", "_")).replaceAll("[^a-zA-Z0-9_]", "");
        }

        return new ApiOperation(
                path,
                method.name(),
                operationId,
                operation.getSummary(),
                operation.getDescription(),
                operation.getTags()
        );
    }
}
