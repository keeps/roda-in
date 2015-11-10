package org.roda.rodain.schema.ui;

import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.utils.FontAwesomeImageCreator;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
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

        Image im = FontAwesomeImageCreator.generate(unicode);
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
            final Rule rule = (Rule) o;
            final String id = rule.getId();
            //set the title with the sip count
            int count = rule.getSipCount();
            rules.put(id, count);
            updateValue();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //replace sips from this rule
                    if (sips.get(id) != null) {
                        getChildren().removeAll(sips.get(id));
                    }
                    HashSet<SipPreviewNode> nodes = new HashSet<>(rule.getSipNodes());
                    sips.put(id, nodes);
                    getChildren().addAll(rule.getSipNodes());
                }
            });
        }
    }

    private void updateValue(){
        int sipCount = getSipCount();
        if(sipCount > 0)
            setValue(String.format("%s  (%d items)", dob.getTitle(), getSipCount()));
        else setValue(dob.getTitle());
    }

    public void addRule(Rule r){
        int count = r.getSipCount();
        String id = r.getId();
        rules.put(id, count);
        ruleObjects.put(id, r);
        int sipCount = getSipCount();
        if(sipCount > 0)
            setValue(String.format("%s  (%d items)", dob.getTitle(), getSipCount()));
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
            text += String.format("  (%d items)", count);
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
