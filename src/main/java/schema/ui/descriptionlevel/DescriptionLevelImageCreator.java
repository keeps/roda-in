package schema.ui.descriptionlevel;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Created by adrapereira on 22-09-2015.
 */
public class DescriptionLevelImageCreator {
    private DescriptionLevelConfig config;

    public DescriptionLevelImageCreator(String descriptionlevel) {
        config = DescriptionLevels.getConfig(descriptionlevel);
    }

    public Image generate(){
        Label label = new Label(config.getAbbreviation());
        int width = config.getWidth();
        label.setMinSize(width, 16);
        label.setMaxSize(width, 16);
        label.setPrefSize(width, 16);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);

        StringBuilder style = new StringBuilder();
        style.append("-fx-background-color: ");
        style.append(config.getBackgroundColor()).append(";");
        style.append("-fx-text-fill:");
        style.append(config.getFontColor()).append(";");
        label.setStyle(style.toString());

        Scene scene = new Scene(new Group(label));
        WritableImage img = new WritableImage(width, 16) ;
        scene.snapshot(img);
        return img ;
    }
}
