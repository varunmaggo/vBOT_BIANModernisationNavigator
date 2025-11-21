package com.vbot.navigator.util;

import java.text.Normalizer;

public final class NameUtils {

    private NameUtils() {
    }

    public static String safeCode(String code) {
        if (code == null || code.isBlank()) {
            return "UNMAPPED";
        }
        return code.trim().toUpperCase();
    }

    public static String slug(String name) {
        String normalized = normalize(name);
        return normalized.toLowerCase().replace('_', '-').replaceAll("-{2,}", "-");
    }

    public static String packageName(String base, String domainName) {
        String normalized = normalize(domainName).toLowerCase().replace('-', '.');
        String sanitized = normalized.replaceAll("[^a-z0-9.]", ".");
        return base + "." + sanitized.replaceAll("\\.{2,}", ".");
    }

    public static String className(String name) {
        String[] parts = normalize(name).replace('-', ' ').split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    public static String methodName(String rawName) {
        String normalized = normalize(rawName).toLowerCase().replace('-', ' ');
        String[] parts = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (first) {
                builder.append(part);
                first = false;
            } else {
                builder.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return builder.isEmpty() ? "handle" : builder.toString();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unmapped";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}a-zA-Z0-9\\s-]", "").trim().replaceAll("\\s+", "-");
    }
}
