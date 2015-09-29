package schema.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import schema.DescriptionObject;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> {
    private static final Logger log = Logger.getLogger(SchemaNode.class.getName());
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
