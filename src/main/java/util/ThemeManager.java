package util;

import javafx.scene.Scene;
import service.ConfigurationService;

import java.util.Objects;

public final class ThemeManager {

    public static final String THEME_KEY = "ui.theme";

    public enum Theme {
        DARK,
        LIGHT
    }

    private static Scene scene;
    private static Theme current = Theme.DARK;
    private static final ConfigurationService CONFIGURATION_SERVICE = new ConfigurationService();

    private ThemeManager() {
    }

    public static void bind(Scene applicationScene) {
        scene = Objects.requireNonNull(applicationScene, "scene");
        apply(loadPersistedTheme());
    }

    public static Theme current() {
        return current;
    }

    public static boolean isDark() {
        return current == Theme.DARK;
    }

    public static void setTheme(Theme theme) {
        Theme selected = theme == null ? Theme.DARK : theme;
        CONFIGURATION_SERVICE.set(THEME_KEY, selected.name().toLowerCase());
        apply(selected);
    }

    public static void toggle() {
        setTheme(isDark() ? Theme.LIGHT : Theme.DARK);
    }

    private static Theme loadPersistedTheme() {
        return CONFIGURATION_SERVICE.get(THEME_KEY)
                .map(value -> "light".equalsIgnoreCase(value) ? Theme.LIGHT : Theme.DARK)
                .orElse(Theme.DARK);
    }

    private static void apply(Theme theme) {
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
