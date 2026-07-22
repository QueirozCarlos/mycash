package service;

import config.ApplicationBootstrap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationLifecycleServiceTest {

    @Test
    void shouldCreateApplicationDirectories(@TempDir Path tempDir) {
        Path applicationDir = tempDir.resolve("financeiro-app");

        ApplicationBootstrap.initialize(applicationDir);

        assertTrue(applicationDir.toFile().isDirectory());
        assertTrue(applicationDir.resolve("backups").toFile().isDirectory());
    }
}