package com.vbot.navigator.service;

import com.vbot.navigator.model.DomainAlignment;
import com.vbot.navigator.model.OperationAlignment;
import com.vbot.navigator.util.NameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class MicroserviceScaffolder {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceScaffolder.class);

    public void scaffold(Path baseDir, List<DomainAlignment> alignments) {
        try {
            Files.createDirectories(baseDir);
            for (DomainAlignment alignment : alignments) {
                if ("UNMAPPED".equalsIgnoreCase(alignment.domain().getCode())) {
                    continue; // do not generate a service for unmapped operations
                }
                createService(baseDir, alignment);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scaffold microservices under " + baseDir, e);
        }
    }

    private void createService(Path baseDir, DomainAlignment alignment) throws IOException {
        String slug = NameUtils.slug(alignment.domain().getName());
        String packageName = NameUtils.packageName("com.generated", slug);
        String className = NameUtils.className(alignment.domain().getName()) + "Application";
        String controllerName = NameUtils.className(alignment.domain().getName()) + "Controller";

        Path serviceDir = baseDir.resolve(slug);
        Path javaDir = serviceDir.resolve("src/main/java/" + packageName.replace('.', '/'));
        Path controllerDir = javaDir.resolve("api");
        Path resourcesDir = serviceDir.resolve("src/main/resources");

        Files.createDirectories(controllerDir);
        Files.createDirectories(resourcesDir);

        writeServicePom(serviceDir, slug, alignment.domain().getName());
        writeApplicationClass(javaDir.resolve(className + ".java"), packageName, className);
        writeController(controllerDir.resolve(controllerName + ".java"), packageName + ".api", controllerName, alignment);
        writeServiceReadme(serviceDir, alignment);

        log.info("Scaffolded microservice {}", serviceDir.toAbsolutePath());
    }

    private void writeServicePom(Path serviceDir, String artifactId, String serviceName) throws IOException {
        String pom = """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.3.5</version>
                        <relativePath/>
                    </parent>
                    <groupId>com.generated</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.1.0-SNAPSHOT</version>
                    <name>%s</name>
                    <properties>
                        <java.version>17</java.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-actuator</artifactId>
                        </dependency>
                    </dependencies>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(artifactId, serviceName);

        Files.writeString(serviceDir.resolve("pom.xml"), pom);
    }

    private void writeApplicationClass(Path file, String packageName, String className) throws IOException {
        String content = """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class %s {
                    public static void main(String[] args) {
                        SpringApplication.run(%s.class, args);
                    }
                }
                """.formatted(packageName, className, className);

        Files.writeString(file, content);
    }

    private void writeController(Path file,
                                 String packageName,
                                 String controllerName,
                                 DomainAlignment alignment) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n")
                .append("import org.springframework.http.HttpStatus;\n")
                .append("import org.springframework.http.ResponseEntity;\n")
                .append("import org.springframework.web.bind.annotation.*;\n\n")
                .append("@RestController\n")
                .append("@RequestMapping(\"/").append(NameUtils.slug(alignment.domain().getName())).append("\")\n")
                .append("public class ").append(controllerName).append(" {\n\n");

        for (OperationAlignment op : alignment.operations()) {
            appendEndpoint(builder, op);
        }

        builder.append("}\n");
        Files.writeString(file, builder.toString());
    }

    private void appendEndpoint(StringBuilder builder, OperationAlignment alignment) {
        String method = alignment.operation().httpMethod().toUpperCase();
        String endpointPath = alignment.operation().path();
        String methodName = NameUtils.methodName(alignment.operation().operationId());

        builder.append("    ")
                .append(mappingFor(method, endpointPath))
                .append("\n");

        builder.append("    public ResponseEntity<String> ").append(methodName).append("() {\n");
        if (alignment.operation().summary() != null && !alignment.operation().summary().isBlank()) {
            builder.append("        // ").append(alignment.operation().summary()).append("\n");
        }
        builder.append("        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)\n")
                .append("                .body(\"Not yet implemented - align to BIAN domain ")
                .append(alignment.domain().getName())
                .append("\");\n");
        builder.append("    }\n\n");
    }

    private String mappingFor(String httpMethod, String path) {
        return switch (httpMethod) {
            case "GET" -> "@GetMapping(\"" + path + "\")";
            case "POST" -> "@PostMapping(\"" + path + "\")";
            case "PUT" -> "@PutMapping(\"" + path + "\")";
            case "PATCH" -> "@PatchMapping(\"" + path + "\")";
            case "DELETE" -> "@DeleteMapping(\"" + path + "\")";
            default -> "@RequestMapping(method = RequestMethod." + httpMethod + ", value = \"" + path + "\")";
        };
    }

    private void writeServiceReadme(Path serviceDir, DomainAlignment alignment) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(alignment.domain().getName()).append(" service\n\n");
        builder.append("- BIAN domain: ").append(alignment.domain().getCode()).append("\n");
        builder.append("- Operations scaffolded: ").append(alignment.operations().size()).append("\n\n");
        builder.append("Endpoints\n");
        for (OperationAlignment op : alignment.operations()) {
            builder.append("- ").append(op.operation().httpMethod()).append(" ").append(op.operation().path());
            if (op.operation().summary() != null) {
                builder.append(" — ").append(op.operation().summary());
            }
            builder.append("\n");
        }
        Files.writeString(serviceDir.resolve("README.md"), builder.toString());
    }
}
