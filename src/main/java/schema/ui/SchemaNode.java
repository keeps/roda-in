package schema.ui;

import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.LoggerFactory;

import rules.Rule;
import schema.DescriptionObject;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SchemaNode.class.getName());
    public DescriptionObject dob;
    private HashMap<String, Integer> rules;
    private Image icon;

    public SchemaNode(DescriptionObject dobject) {
        super(dobject.getTitle());
        dob = dobject;
        rules = new HashMap<String, Integer>();

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

    public void addRule(Rule r){
        int count = r.getSipCount();
        String hash = "" + r.hashCode();
        rules.put(hash, count);
        setValue(dob.getTitle() + "  (" + getSipCount() + " items)");
    }

    public void removeRule(Rule r){
        String hash = "" + r.hashCode();
        rules.remove(hash);
        setValue(dob.getTitle() + "  (" + getSipCount() + " items)");
    }

    public int getSipCount(){
        int result = 0;
        for(int i: rules.values()) result += i;
        return result;
    }

    public Image getImage(){return icon;}
}
