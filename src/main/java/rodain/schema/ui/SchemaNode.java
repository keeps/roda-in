package rodain.schema.ui;

import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import rodain.rules.Rule;
import rodain.rules.sip.SipPreview;
import rodain.schema.DescriptionObject;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> implements Observer {
    private DescriptionObject dob;
    private Map<String, Integer> rules;
    private Map<String, Rule> ruleObjects;
    private Map<String, Set<SipPreviewNode>> sips;
    private Image icon;

    private List<SchemaNode> schemaNodes;

    public SchemaNode(DescriptionObject dobject) {
        super(dobject.getTitle());
        dob = dobject;
        rules = new HashMap<>();
        sips = new HashMap<>();
        ruleObjects = new HashMap<>();
        schemaNodes = new ArrayList<>();

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
            schemaNodes.add(child);
        }
    }

    @Override
    public void update(final Observable o, Object arg) {
        if(o instanceof Rule){
            Platform.runLater(new Runnable() {
                @Override
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
        ruleObjects.put(id, r);
        int sipCount = getSipCount();
        if(sipCount > 0)
            setValue(dob.getTitle() + "  (" + sipCount + " items)");
        setExpanded(true);
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
        if(count > 0)
            text += "  (" + count + " items)";
        setValue(text);
    }

    public int getSipCount(){
        int result = 0;
        for(int i: rules.values())
            result += i;
        return result;
    }

    public Image getImage(){
        return icon;
    }

    public DescriptionObject getDob() {
        return dob;
    }

    public Map<SipPreview, String> getSipPreviews(){
        Map<SipPreview, String> result = new HashMap<>();
        //this node's sips
        for(Rule r: ruleObjects.values())
            for(SipPreview sp: r.getSips())
                result.put(sp, dob.getId());

        //children sips
        for(SchemaNode sn: schemaNodes)
            result.putAll(sn.getSipPreviews());
        return result;
    }
}
