package schema.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import schema.DescriptionObject;

/**
 * Created by adrap on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> {
    public static Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("folder.png"));
    public DescriptionObject dob;

    public SchemaNode(DescriptionObject dobject) {
        super(dobject.getTitle());
        dob = dobject;

        this.setGraphic(new ImageView(folderCollapseImage));

        for(DescriptionObject obj: dob.getChildren()){
            SchemaNode child = new SchemaNode(obj);
            this.getChildren().add(child);
        }
    }
}
