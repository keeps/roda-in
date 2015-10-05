package rules;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.scene.image.Image;

import org.slf4j.LoggerFactory;

import schema.SipPreview;
import schema.ui.DescriptionLevelImageCreator;
import schema.ui.SchemaNode;
import schema.ui.SipPreviewNode;
import source.ui.items.SourceTreeDirectory;
import utils.TreeWalkHandler;
import utils.WalkFileTree;

/**
 * Created by adrapereira on 29-09-2015.
 */
public class Rule extends Observable implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Rule.class.getName());
    private SourceTreeDirectory source;
    private SchemaNode schemaNode;
    private RuleTypes type;
    private HashSet<SipPreview> sips;
    private HashSet<SipPreviewNode> sipNodes = new HashSet<SipPreviewNode>();
    private TreeWalkHandler sipCreator;
    private WalkFileTree treeWalker;
    private Image icon;
    private int sipCount = 0, added = 0;

    public Rule(SourceTreeDirectory source, SchemaNode schemaNode){
        this.source = source;
        this.schemaNode = schemaNode;
        this.addObserver(schemaNode);

        ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
        String category = hierarchyConfig.getString("category.item");
        String unicode = hierarchyConfig.getString("icon." + category);

        DescriptionLevelImageCreator dlic = new DescriptionLevelImageCreator(unicode);
        icon = dlic.generate();
    }

    public SourceTreeDirectory getSource() {
        return source;
    }

    public void setSource(SourceTreeDirectory source) {
        this.source = source;
    }

    public SchemaNode getSchemaNode() {
        return schemaNode;
    }

    public void setSchemaNode(SchemaNode schemaNode) {
        this.schemaNode = schemaNode;
    }

    public String getFolderName(){
        return source.getValue().toString();
    }

    public String getDescObjName(){
        return schemaNode.dob.getTitle();
    }

    public int getSipCount() {
        return sipCount;
    }

    public HashSet<SipPreviewNode> getSipNodes(){return sipNodes;}

    public void apply(RuleTypes type, int level){
        this.type = type;
        sipCount = 0; added = 0;
        sips = new HashSet<SipPreview>();

        switch (type){
            case SIPPERFILE:
                SipPerFileHandler creator = new SipPerFileHandler(source.getPath());
                creator.addObserver(this);
                sipCreator = creator;
                treeWalker = new WalkFileTree(source.getPath(), sipCreator);
                treeWalker.start();
                break;
            case SIPPERFOLDER:
                SipPerFolderHandler perFolder = new SipPerFolderHandler(source.getPath(), level);
                perFolder.addObserver(this);
                sipCreator = perFolder;
                treeWalker = new WalkFileTree(source.getPath(), sipCreator);
                treeWalker.start();
                break;
        }

        schemaNode.addRule(this);
    }

    public void update(Observable o, Object arg) {
        if(o instanceof SipPerFileHandler){
            SipPerFileHandler handler = (SipPerFileHandler) o;
            sipCount = handler.getCount();
            while(handler.hasNext() && added < 100){
                added++;
                sipNodes.add(new SipPreviewNode(handler.getNext(), icon));
            }
        }else if(o instanceof SipPerFolderHandler){
            SipPerFolderHandler handler = (SipPerFolderHandler) o;
            sipCount = handler.getCount();
            while(handler.hasNext() && added < 100) {
                added++;
                sipNodes.add(new SipPreviewNode(handler.getNext(), icon));
            }
        }
        setChanged();
        notifyObservers();
    }
}
