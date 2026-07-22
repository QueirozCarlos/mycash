package config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class DatabaseBootstrap {

    private static EntityManagerFactory entityManagerFactory;

    private DatabaseBootstrap() {
    }

    public static synchronized void initialize() {
        initialize(AppPaths.APPLICATION_DIR);
    }

    public static synchronized void initialize(Path applicationDir) {
        Path databaseFile = applicationDir.resolve("financeiro.db");
        String jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();

        migrateSchema(jdbcUrl);

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
        properties.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
        properties.put("jakarta.persistence.jdbc.user", "");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.hbm2ddl.auto", "none");

        entityManagerFactory = new HibernatePersistenceProvider()
                .createEntityManagerFactory("financeiroPU", properties);
    }

    private static void migrateSchema(String jdbcUrl) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(DatabaseBootstrap.class.getClassLoader());
            try {
                runFlyway(jdbcUrl, "classpath:db/migration");
            } catch (RuntimeException classpathFailure) {
                if (!isMissingMigrationResource(classpathFailure)) {
                    throw classpathFailure;
                }
                Path migrationPath = resolveMigrationPath();
                runFlyway(jdbcUrl, "filesystem:" + migrationPath.toAbsolutePath());
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | URISyntaxException exception) {
            throw new IllegalStateException("Falha ao executar as migrations Flyway.", exception);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private static void runFlyway(String jdbcUrl, String location)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        try {
            Class<?> flywayClass = Class.forName("org.flywaydb.core.Flyway");
            Object configuration = flywayClass.getMethod("configure").invoke(null);
            Object configuredDataSource = configuration.getClass()
                    .getMethod("dataSource", String.class, String.class, String.class)
                    .invoke(configuration, jdbcUrl, "", "");
            Object configuredLocations = configuredDataSource.getClass()
                    .getMethod("locations", String[].class)
                    .invoke(configuredDataSource, (Object) new String[]{location});
            Object flyway = configuredLocations.getClass().getMethod("load").invoke(configuredLocations);
            flyway.getClass().getMethod("migrate").invoke(flyway);
        } catch (java.lang.reflect.InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Path resolveMigrationPath() throws URISyntaxException {
        Path codeSource = Paths.get(DatabaseBootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path candidate = codeSource.resolve("db/migration");
        if (candidate.toFile().exists()) {
            return candidate;
        }

        return Paths.get("src/main/resources/db/migration");
    }

    private static boolean isMissingMigrationResource(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if ("org.flywaydb.core.api.FlywayException".equals(current.getClass().getName())
                    && current.getMessage() != null
                    && current.getMessage().contains("Unable to obtain inputstream for resource")) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }

    public static synchronized EntityManagerFactory entityManagerFactory() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("Banco de dados ainda não foi inicializado.");
        }

        return entityManagerFactory;
    }

    public static synchronized void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }
}