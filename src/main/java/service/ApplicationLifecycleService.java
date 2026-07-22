package service;

import config.ApplicationBootstrap;
import config.DatabaseBootstrap;
import model.CategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationLifecycleService {

    public static final String AUTO_BACKUP_KEY = "backup.auto.enabled";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationLifecycleService.class);

    private final UserService userService = new UserService();
    private final CategoryService categoryService = new CategoryService();
    private final RecurringAccountService recurringAccountService = new RecurringAccountService();
    private final ConfigurationService configurationService = new ConfigurationService();
    private final BackupService backupService = new BackupService();

    public void start() {
        ApplicationBootstrap.initialize();
        DatabaseBootstrap.initialize();
        userService.ensureDefaultUser();
        seedDefaultCategories();
        int generated = recurringAccountService.generateDueOccurrences();
        maybeAutoBackup();
        LOGGER.info("Aplicação inicializada. Ocorrências recorrentes geradas: {}", generated);
    }

    public void stop() {
        DatabaseBootstrap.shutdown();
        LOGGER.info("Aplicação finalizada.");
    }

    private void maybeAutoBackup() {
        boolean enabled = configurationService.get(AUTO_BACKUP_KEY)
                .map(value -> "true".equalsIgnoreCase(value) || "1".equals(value))
                .orElse(false);
        if (!enabled) {
            return;
        }
        try {
            backupService.createBackup();
            LOGGER.info("Backup automático executado.");
        } catch (RuntimeException exception) {
            LOGGER.warn("Falha no backup automático: {}", exception.getMessage());
        }
    }

    private void seedDefaultCategories() {
        if (!categoryService.list("").isEmpty()) {
            return;
        }

        categoryService.create("Alimentação", CategoryType.ALIMENTACAO, "#F97316");
        categoryService.create("Transporte", CategoryType.TRANSPORTE, "#0EA5E9");
        categoryService.create("Moradia", CategoryType.MORADIA, "#8B5CF6");
        categoryService.create("Saúde", CategoryType.SAUDE, "#22C55E");
        categoryService.create("Educação", CategoryType.EDUCACAO, "#EAB308");
        categoryService.create("Lazer", CategoryType.LAZER, "#EC4899");
        categoryService.create("Salário", CategoryType.SALARIO, "#14B8A6");
        categoryService.create("Investimentos", CategoryType.INVESTIMENTOS, "#6366F1");
        categoryService.create("Outros", CategoryType.OUTROS, "#94A3B8");
    }
}
