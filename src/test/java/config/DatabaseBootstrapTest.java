package config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseBootstrapTest {

    @Test
    void shouldMigrateSchemaAndInitializeJpa(@TempDir Path tempDir) throws Exception {
        ApplicationBootstrap.initialize(tempDir);
        DatabaseBootstrap.initialize(tempDir);

        Path databaseFile = tempDir.resolve("financeiro.db");
        assertTrue(databaseFile.toFile().exists());

        Set<String> tables = new HashSet<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath());
             ResultSet resultSet = connection.createStatement().executeQuery(
                     "select name from sqlite_master where type='table'")) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("name"));
            }
        }

        assertTrue(tables.contains("usuario"));
        assertTrue(tables.contains("categoria"));
        assertTrue(tables.contains("movimentacao"));
        assertTrue(tables.contains("configuracao"));
        assertTrue(tables.contains("conta_recorrente"));
        assertTrue(tables.contains("cartao"));
        assertTrue(tables.contains("parcelamento"));
        assertTrue(tables.contains("meta"));
        assertTrue(tables.contains("backup_historico"));

        DatabaseBootstrap.shutdown();
    }
}