BOT ModernisationNavigator
=========================

Purpose
-------
- Read an OpenAPI/Swagger spec, map endpoints to BIAN service domains/capabilities, and produce a modernisation plan.
- Optionally scaffold BIAN-aligned Spring Boot microservice stubs for each aligned domain.

Quick start
-----------
- Prereqs: Java 17+, Maven.
- Run ingestion only: `mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments="--spec=path/to/spec.yaml"`.
- Run with scaffold output: `mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments="--spec=path/to/spec.yaml --out=generated --scaffold"`.

What it does
------------
- Parses an OpenAPI v3 spec (YAML/JSON) and extracts operations (path, verb, tags, summary).
- Aligns operations to a small BIAN domain catalogue using keyword heuristics.
- Writes an alignment report and, when `--scaffold` is present, generates Spring Boot microservice stubs per domain under the chosen output directory.

Key flags
---------
- `--spec=` (required): path to the OpenAPI/Swagger file.
- `--out=` (optional): output directory for reports and scaffolds. Defaults to `generated`.
- `--scaffold` (optional): emit Java microservice stubs for aligned domains.

Repo layout
-----------
- `src/main/java/`: Spring Boot CLI + services.
- `src/main/resources/bian-domains.yml`: seed BIAN service domain catalogue (extend with your mappings).
- Output (alignment + scaffolds) is written to `generated/` by default and not tracked in git.

Next iterations
---------------
- Enrich the BIAN catalogue with full domain/capability lists.
- Swap keyword heuristics for a persistent mapping table or NLP classifier.
- Add pruning rules to split monolith specs into clean microservice boundaries.
