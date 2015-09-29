package schema.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import schema.DescriptionObject;
import schema.ui.descriptionlevel.DescriptionLevelImageCreator;

import java.util.ResourceBundle;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> {
    public DescriptionObject dob;
    private Image icon;

    public SchemaNode(DescriptionObject dobject) {
        super(dobject.getTitle());
        dob = dobject;

        ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
        String category = hierarchyConfig.getString("category." + dobject.getDescriptionlevel());
        String unicode = hierarchyConfig.getString("icon."+category);

        DescriptionLevelImageCreator dlic = new DescriptionLevelImageCreator(unicode);
        Image im = dlic.generate();
        icon = im;
        this.setGraphic(new ImageView(im));

        for(DescriptionObject obj: dob.getChildren()){
            SchemaNode child = new SchemaNode(obj);
            this.getChildren().add(child);
        }
    }

    public Image getImage(){return icon;}
}
