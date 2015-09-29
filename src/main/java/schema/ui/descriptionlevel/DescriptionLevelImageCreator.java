package schema.ui.descriptionlevel;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;

import java.io.InputStream;

/**
 * Created by adrapereira on 22-09-2015.
 */
public class DescriptionLevelImageCreator {
    private DescriptionLevelConfig config;

    public DescriptionLevelImageCreator(String descriptionlevel) {
        config = DescriptionLevels.getConfig(descriptionlevel);
    }

    public Image generate(){
        InputStream font = getClass().getResourceAsStream("/fontawesome-webfont.ttf");
        Font fontAwesome = Font.loadFont(font, 16);
        final String ICON_ANDROID = "\uf17b";
        Label label = new Label(ICON_ANDROID);
        label.setFont(fontAwesome);

        int width = 24;
        label.setMinSize(width, 16);
        label.setMaxSize(width, 16);
        label.setPrefSize(width, 16);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);

        StringBuilder style = new StringBuilder();
        style.append("-fx-background-color: ");
        style.append("transparent;");
        style.append("-fx-text-fill:");
        style.append(config.getFontColor()).append(";");
        label.setStyle(style.toString());

        Scene scene = new Scene(new Group(label));
        WritableImage img = new WritableImage(width, 16) ;
        scene.snapshot(img);
        return img ;
    }
}
