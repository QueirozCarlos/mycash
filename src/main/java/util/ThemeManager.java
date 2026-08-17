package util;

import javafx.scene.Scene;
import service.ConfigurationService;

import java.util.Objects;
import java.util.prefs.Preferences;

public final class ThemeManager {

    public static final String THEME_KEY = "ui.theme";
    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);

    public enum Theme {
        DARK,
        LIGHT
    }

    private static Scene scene;
    private static Theme current = Theme.DARK;
    private static ConfigurationService configurationService;

    private ThemeManager() {
    }

    /**
     * Retorna o tema salvo via Preferences do Java (sem tocar no banco).
     * Ideal para o Splash Screen!
     */
    public static Theme getSavedThemeFast() {
        String savedTheme = PREFS.get(THEME_KEY, "dark");
        return "light".equalsIgnoreCase(savedTheme) ? Theme.LIGHT : Theme.DARK;
    }

    public static void bind(Scene applicationScene) {
        scene = Objects.requireNonNull(applicationScene, "scene");
        apply(getSavedThemeFast());
    }

    public static Theme current() {
        return current;
    }

    public static boolean isDark() {
        return current == Theme.DARK;
    }

    public static void setTheme(Theme theme) {
        Theme selected = theme == null ? Theme.DARK : theme;
        current = selected;

        // 1. Salva no Preferences (Rápido/Sem banco)
        PREFS.put(THEME_KEY, selected.name().toLowerCase());

        // 2. Salva no Banco de Dados se o serviço estiver pronto
        try {
            if (configurationService == null) {
                configurationService = new ConfigurationService();
            }
            configurationService.set(THEME_KEY, selected.name().toLowerCase());
        } catch (Exception e) {
            // Ignora se o banco ainda não subiu
        }

        apply(selected);
    }

    public static void toggle() {
        setTheme(isDark() ? Theme.LIGHT : Theme.DARK);
    }

    public static void apply(Theme theme) {
        current = theme;
        if (scene == null) {
            return;
        }

        String darkCss = ThemeManager.class.getResource("/css/app.css").toExternalForm();
        String lightCss = ThemeManager.class.getResource("/css/app-light.css").toExternalForm();

        scene.getStylesheets().removeIf(sheet ->
                sheet.endsWith("/css/app.css") || sheet.endsWith("/css/app-light.css"));

        scene.getStylesheets().add(darkCss);
        if (theme == Theme.LIGHT) {
            scene.getStylesheets().add(lightCss);
        }
    }
}