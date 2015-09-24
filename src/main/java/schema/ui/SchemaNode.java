package schema.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import schema.DescriptionObject;
import schema.ui.DescriptionLevel.DescriptionLevelImageCreator;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> {
    public DescriptionObject dob;

    public SchemaNode(DescriptionObject dobject) {
        super(dobject.getTitle());
        dob = dobject;

        DescriptionLevelImageCreator dlic = new DescriptionLevelImageCreator(dob.getDescriptionlevel());
        Image im = dlic.generate();
        this.setGraphic(new ImageView(im));

        for(DescriptionObject obj: dob.getChildren()){
            SchemaNode child = new SchemaNode(obj);
            this.getChildren().add(child);
        }
    }
}
