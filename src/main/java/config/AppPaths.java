package config;

import java.nio.file.Path;

public final class AppPaths {

    public static final Path APPLICATION_DIR = Path.of(System.getProperty("user.home"), ".financeiro");
    public static final Path DATABASE_FILE = APPLICATION_DIR.resolve("financeiro.db");
    public static final Path BACKUP_DIR = APPLICATION_DIR.resolve("backups");

    private AppPaths() {
    }
}