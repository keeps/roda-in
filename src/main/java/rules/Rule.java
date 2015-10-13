package rules;

import java.util.*;

import javafx.scene.image.Image;

import schema.SipPreview;
import schema.ui.DescriptionLevelImageCreator;
import schema.ui.SipPreviewNode;
import source.ui.items.SourceTreeDirectory;
import utils.RandomIdGenerator;
import utils.TreeVisitor;

/**
 * Created by adrapereira on 29-09-2015.
 */
public class Rule extends Observable implements Observer {
    private SourceTreeDirectory source;
    private RuleTypes type;
    private List<SipPreview> sips;
    private HashSet<SipPreviewNode> sipNodes = new HashSet<>();
    private TreeVisitor visitor;
    private Image icon;
    private int sipCount = 0, added = 0;
    private int level;
    private String id;

    public Rule(SourceTreeDirectory source, RuleTypes type, int level) {
        this.source = source;
        this.type = type;
        this.level = level;
        id = RandomIdGenerator.getBase62(5);
    }

    public Rule(SourceTreeDirectory source){
        this.source = source;
        id = RandomIdGenerator.getBase62(5);

        ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
        String category = hierarchyConfig.getString("category.item");
        String unicode = hierarchyConfig.getString("icon." + category);

        DescriptionLevelImageCreator dlic = new DescriptionLevelImageCreator(unicode);
        icon = dlic.generate();
    }

    public SourceTreeDirectory getSource() {
        return source;
    }

    public String getId() {
        return id;
    }

    public List<SipPreview> getSips() {
        return sips;
    }

    public void setSource(SourceTreeDirectory source) {
        this.source = source;
    }

    public String getFolderName(){
        return source.getValue().toString();
    }

    public TreeVisitor getVisitor(){
        return visitor;
    }
    public int getSipCount() {
        return sipCount;
    }

    public Set<SipPreviewNode> getSipNodes(){
        return sipNodes;
    }

    public TreeVisitor apply(){
        return apply(type, level);
    }

    public TreeVisitor apply(RuleTypes type, int level){
        this.type = type;
        sipCount = 0;
        added = 0;
        sips = new ArrayList<>();
        sipNodes = new HashSet<>();

        switch (type){
            case SIPPERFILE:
                SipPerFileVisitor visitorFile = new SipPerFileVisitor(id);
                visitorFile.addObserver(this);
                visitor = visitorFile;
                break;
            default:
            case SIPPERFOLDER:
                SipPerFolderVisitor visitorFolder = new SipPerFolderVisitor(source.getPath(), id, level);
                visitorFolder.addObserver(this);
                visitor = visitorFolder;
                break;
        }
        return visitor;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof SipPerFileVisitor){
            SipPerFileVisitor visit = (SipPerFileVisitor) o;
            sipCount = visit.getCount();
            while(visit.hasNext() && added < 100){
                added++;
                sipNodes.add(new SipPreviewNode(visit.getNext(), icon));
                sips = visit.getSips();
            }
        }else if(o instanceof SipPerFolderVisitor){
            SipPerFolderVisitor visit = (SipPerFolderVisitor) o;
            sipCount = visit.getCount();
            while(visit.hasNext() && added < 100) {
                added++;
                sipNodes.add(new SipPreviewNode(visit.getNext(), icon));
                sips = visit.getSips();
            }
        }
        setChanged();
        notifyObservers();
    }
}
