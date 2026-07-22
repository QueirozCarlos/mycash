package service;

import config.AppPaths;
import config.DatabaseBootstrap;
import config.JpaSupport;
import exception.ApplicationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class BackupService {

    private static final DateTimeFormatter BACKUP_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public Path createBackup() {
        try {
            Files.createDirectories(AppPaths.BACKUP_DIR);
            if (!Files.exists(AppPaths.DATABASE_FILE)) {
                throw new ApplicationException("Arquivo de banco de dados não encontrado: " + AppPaths.DATABASE_FILE);
            }

            String fileName = "financeiro_backup_" + LocalDateTime.now().format(BACKUP_TIMESTAMP) + ".db";
            Path destination = AppPaths.BACKUP_DIR.resolve(fileName);
            Files.copy(AppPaths.DATABASE_FILE, destination, StandardCopyOption.REPLACE_EXISTING);

            recordBackupHistory(destination, "MANUAL");
            return destination;
        } catch (IOException exception) {
            throw new ApplicationException("Falha ao criar backup.", exception);
        }
    }

    public void restoreBackup(Path backupFile) {
        if (backupFile == null || !Files.exists(backupFile)) {
            throw new ApplicationException("Arquivo de backup não encontrado: " + backupFile);
        }

        try {
            Files.createDirectories(AppPaths.APPLICATION_DIR);
            DatabaseBootstrap.shutdown();
            Files.copy(backupFile, AppPaths.DATABASE_FILE, StandardCopyOption.REPLACE_EXISTING);
            DatabaseBootstrap.initialize();
        } catch (IOException exception) {
            try {
                DatabaseBootstrap.initialize();
            } catch (RuntimeException ignored) {
                // best effort re-init after failure
            }
            throw new ApplicationException("Falha ao restaurar backup a partir de " + backupFile, exception);
        }
    }

    public List<Path> listBackups() {
        try {
            Files.createDirectories(AppPaths.BACKUP_DIR);
            try (Stream<Path> stream = Files.list(AppPaths.BACKUP_DIR)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String name = path.getFileName().toString().toLowerCase();
                            return name.endsWith(".db");
                        })
                        .sorted(Comparator.comparing(this::lastModified).reversed())
                        .toList();
            }
        } catch (IOException exception) {
            throw new ApplicationException("Falha ao listar backups.", exception);
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            return 0L;
        }
    }

    private void recordBackupHistory(Path destination, String origin) {
        try {
            JpaSupport.inTransaction(em -> {
                em.createNativeQuery(
                                """
                                insert into backup_historico (arquivo, origem, created_at)
                                values (?1, ?2, ?3)
                                """)
                        .setParameter(1, destination.getFileName().toString())
                        .setParameter(2, origin)
                        .setParameter(3, LocalDateTime.now().toString())
                        .executeUpdate();
            });
        } catch (RuntimeException ignored) {
            // History table is optional for backup success.
        }
    }
}
