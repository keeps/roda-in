package schema.ui;

import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.LoggerFactory;

import rules.Rule;
import schema.DescriptionObject;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SchemaNode.class.getName());
    public DescriptionObject dob;
    private HashMap<String, Integer> rules;
    private HashMap<String, HashSet<SipPreviewNode>> sips;
    private Image icon;

    public SchemaNode(DescriptionObject dobject) {
        super(dobject.getTitle());
        dob = dobject;
        rules = new HashMap<String, Integer>();
        sips = new HashMap<String, HashSet<SipPreviewNode>>();

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

    public void update(final Observable o, Object arg) {
        if(o instanceof Rule){
            Platform.runLater(new Runnable() {
                public void run() {
                    Rule rule = (Rule) o;
                    String id = rule.getId();

                    //replace sips from this rule
                    if(sips.get(id) != null) {
                        getChildren().removeAll(sips.get(id));
                    }
                    sips.put(id, rule.getSipNodes());

                    getChildren().addAll(rule.getSipNodes());

                    //set the title with the sip count
                    int count = rule.getSipCount();
                    rules.put(id, count);
                    setValue(dob.getTitle() + "  (" + getSipCount() + " items)");
                }
            });
        }
    }

    public void addRule(Rule r){
        int count = r.getSipCount();
        String id = r.getId();
        rules.put(id, count);
        setValue(dob.getTitle() + "  (" + getSipCount() + " items)");
    }

    public void removeRule(Rule r){
        String id = r.getId();
        if(sips.get(id) != null) {
            getChildren().removeAll(sips.get(id));
        }
        rules.remove(id);
        sips.remove(id);

        String text = dob.getTitle();
        int count = getSipCount();
        if(count > 0) text += "  (" + count + " items)";
        setValue(text);
    }

    public int getSipCount(){
        int result = 0;
        for(int i: rules.values()) result += i;
        return result;
    }

    public Image getImage(){return icon;}
}
