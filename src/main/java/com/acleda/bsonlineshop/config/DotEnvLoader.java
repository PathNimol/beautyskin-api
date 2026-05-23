package com.acleda.bsonlineshop.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads {@code .env} from the project working directory into system properties
 * when not already set in the OS environment (local IntelliJ / IDE runs).
 */
public final class DotEnvLoader {

    private DotEnvLoader() {}

    public static void loadIfPresent() {
        Path envFile = Path.of(System.getProperty("user.dir")).resolve(".env");
        if (!Files.isRegularFile(envFile)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // Missing or unreadable .env — rely on OS env / CI secrets
        }
    }
}
