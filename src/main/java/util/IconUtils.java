package util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Objects;

public class IconUtils {

    public static ImageView load(String nome) {
        ImageView image = new ImageView(loadImage(nome));
        image.setFitWidth(18);
        image.setFitHeight(18);
        return image;
    }

    public static Image loadImage(String nome) {
        InputStream stream = Objects.requireNonNull(
                IconUtils.class.getResourceAsStream("/icons/" + nome),
                "Ícone não encontrado: /icons/" + nome
        );

        return new Image(stream);
    }
}