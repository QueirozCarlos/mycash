package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApplicationBootstrap {

    private ApplicationBootstrap() {
    }

    public static void initialize() {
        initialize(AppPaths.APPLICATION_DIR);
    }

    public static void initialize(Path applicationDir) {
        try {
            Files.createDirectories(applicationDir);
            Files.createDirectories(applicationDir.resolve("backups"));
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao preparar diretórios da aplicação.", exception);
        }
    }
}