package com.vbot.navigator.cli;

import com.vbot.navigator.model.NavigatorRequest;
import com.vbot.navigator.service.NavigatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class NavigatorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NavigatorRunner.class);
    private static final String SPEC_OPTION = "spec";
    private static final String OUT_OPTION = "out";
    private static final String SCAFFOLD_OPTION = "scaffold";

    private final NavigatorService navigatorService;

    public NavigatorRunner(NavigatorService navigatorService) {
        this.navigatorService = navigatorService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption(SPEC_OPTION)) {
            log.warn("Missing required --spec=path/to/openapi.(yaml|json) argument. Exiting.");
            return;
        }

        List<String> specArgs = args.getOptionValues(SPEC_OPTION);
        Path specPath = Path.of(specArgs.get(0));

        Path outputDir = args.containsOption(OUT_OPTION)
                ? Path.of(args.getOptionValues(OUT_OPTION).get(0))
                : Path.of("generated");

        boolean scaffold = args.containsOption(SCAFFOLD_OPTION);

        NavigatorRequest request = new NavigatorRequest(specPath, outputDir, scaffold);
        navigatorService.execute(request);
    }
}
