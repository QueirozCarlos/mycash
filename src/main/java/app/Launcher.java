package app;

public class Launcher {
    public static void main(String[] args) {
        // Alinha o WM_CLASS exato com o que o GNOME/Ubuntu espera
        System.setProperty("javafx.application.id", "app.MainApplication");
        MainApplication.main(args);
    }
}
