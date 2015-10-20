package rules;

import java.util.*;

import javafx.scene.image.Image;

import rules.filters.ContentFilter;
import rules.filters.FilterIgnored;
import schema.ui.DescriptionLevelImageCreator;
import schema.ui.SipPreviewNode;
import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeItem;
import source.ui.items.SourceTreeItemState;
import utils.RandomIdGenerator;
import utils.TreeVisitor;

/**
 * Created by adrapereira on 29-09-2015.
 */
public class Rule extends Observable implements Observer {
    private Set<SourceTreeItem> source;
    private String metadata;
    private RuleTypes assocType;
    private MetadataTypes metaType;
    private Set<ContentFilter> filters;

    private List<SipPreview> sips;
    private HashSet<SipPreviewNode> sipNodes = new HashSet<>();
    private TreeVisitor visitor;
    private Image icon;
    private int sipCount = 0, added = 0;
    private int level;
    private String id;

    public Rule(Set<SourceTreeItem> source, RuleTypes assocType, int level, String metadata, MetadataTypes metaType){
        this.source = source;
        this.assocType = assocType;
        this.level = level;
        this.metadata = metadata;
        this.metaType = metaType;
        filters = new HashSet<>();
        id = RandomIdGenerator.getBase62(5);

        createIcon();
        createFilters();
    }

    private void createIcon(){
        ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
        String category = hierarchyConfig.getString("category.item");
        String unicode = hierarchyConfig.getString("icon." + category);

        DescriptionLevelImageCreator dlic = new DescriptionLevelImageCreator(unicode);
        icon = dlic.generate();
    }

    private void createFilters(){
        FilterIgnored ignored = new FilterIgnored();
        for(SourceTreeItem sti: source) {
            recursiveIgnore(sti, ignored);
        }
        filters.add(ignored);
    }

    private void recursiveIgnore(SourceTreeItem sti, FilterIgnored ignored){
        if(sti.getState() == SourceTreeItemState.IGNORED)
            ignored.add(sti.getPath());
        if(sti instanceof SourceTreeDirectory) {
            Set<String> ignoredChildren = ((SourceTreeDirectory) sti).getIgnored();
            for(String child: ignoredChildren)
                ignored.add(child);
        }
    }

    /*public Rule(Path source, RuleTypes assocType, int level) {
        this.source = source;
        this.assocType = assocType;
        this.level = level;
        id = RandomIdGenerator.getBase62(5);
    }*/

    public Set<SourceTreeItem> getSource() {
        return source;
    }
    public String getSourceString() {
        return source.toString();
    }

    public String getId() {
        return id;
    }

    public List<SipPreview> getSips() {
        return sips;
    }

    public void setSource(Set<SourceTreeItem> source) {
        this.source = source;
    }

    /*public String getFolderName(){
        return source.getFileName().toString();
    }*/

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
        return apply(assocType, level);
    }

    public TreeVisitor apply(RuleTypes type, int level){
        this.assocType = type;
        sipCount = 0;
        added = 0;
        sips = new ArrayList<>();
        sipNodes = new HashSet<>();

        switch (type){
            case SINGLESIP:
                SipSingle visitorSingle = new SipSingle(id, filters);
                visitorSingle.addObserver(this);
                visitor = visitorSingle;
                break;
            case SIPPERFILE:
                SipPerFileVisitor visitorFile = new SipPerFileVisitor(id, filters);
                visitorFile.addObserver(this);
                visitor = visitorFile;
                break;
            default:
            case SIPPERFOLDER:
                SipPerFolderVisitor visitorFolder = new SipPerFolderVisitor(id, level, filters);
                visitorFolder.addObserver(this);
                visitor = visitorFolder;
                break;
        }
        return visitor;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof SipCreator){
            SipCreator visit = (SipCreator) o;
            sipCount = visit.getCount();
            while(visit.hasNext() && added < 100){
                added++;
                sipNodes.add(new SipPreviewNode(visit.getNext(), icon));
                sips = visit.getSips();
            }
        }

        setChanged();
        notifyObservers();
    }
}
