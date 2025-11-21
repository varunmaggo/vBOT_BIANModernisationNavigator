package com.vbot.navigator.model;

import java.nio.file.Path;

public record NavigatorRequest(Path specPath, Path outputDir, boolean generateScaffolds) {
}
