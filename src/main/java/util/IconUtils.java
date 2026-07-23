package util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class IconUtils {

    public static ImageView load(String nome) {

        InputStream stream = IconUtils.class.getResourceAsStream("/icons/" + nome);

        if (stream == null) {
            System.out.println("Não encontrou: /icons/" + nome);
            throw new RuntimeException("Ícone não encontrado: /icons/" + nome);
        }

        ImageView image = new ImageView(new Image(stream));
        image.setFitWidth(18);
        image.setFitHeight(18);

        return image;
    }
}