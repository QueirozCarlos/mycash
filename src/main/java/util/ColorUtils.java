package util;

import javafx.scene.paint.Color;

public final class ColorUtils {

    private static final Color FALLBACK = Color.web("#5B6CFF");

    private ColorUtils() {
    }

    public static Color fromHex(String hex) {
        if (hex == null || hex.isBlank()) {
            return FALLBACK;
        }
        try {
            String value = hex.trim();
            if (!value.startsWith("#")) {
                value = "#" + value;
            }
            return Color.web(value);
        } catch (IllegalArgumentException exception) {
            return FALLBACK;
        }
    }

    public static String toHex(Color color) {
        if (color == null) {
            return "#5B6CFF";
        }
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static String css(String hex) {
        return "-fx-background-color: " + toHex(fromHex(hex)) + ";";
    }
}
