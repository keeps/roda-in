package org.roda.rodain.rules;

import javafx.scene.image.Image;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.rules.filters.FilterIgnored;
import org.roda.rodain.rules.filters.FilterMapped;
import org.roda.rodain.rules.sip.*;
import org.roda.rodain.schema.ui.DescriptionLevelImageCreator;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.TreeVisitor;

import java.nio.file.Path;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-09-2015.
 */
public class Rule extends Observable implements Observer {
    private Set<SourceTreeItem> source;
    private String metadataContent;
    private Path metadataPath;
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

    public Rule(Set<SourceTreeItem> source, RuleTypes assocType, int level, Path metadataPath, String metadataContent, MetadataTypes metaType){
        this.source = source;
        this.assocType = assocType;
        this.level = level;
        this.metadataContent = metadataContent;
        this.metadataPath = metadataPath;
        this.metaType = metaType;
        filters = new HashSet<>();
        id = UUID.randomUUID().toString();

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
        FilterMapped mapped = new FilterMapped();
        for(SourceTreeItem sti: source) {
            // add this item to the filter if it's ignored or mapped
            if(sti.getState() == SourceTreeItemState.IGNORED)
                ignored.add(sti.getPath());
            else if(sti.getState() == SourceTreeItemState.MAPPED)
                mapped.add(sti.getPath());
            //if it's a directory, get all its mapped and ignored children and add to the filters
            if(sti instanceof SourceTreeDirectory) {
                Set<String> filterIgnored = ((SourceTreeDirectory) sti).getIgnored();
                ignored.addAll(filterIgnored);
                Set<String> filterMapped = ((SourceTreeDirectory) sti).getMapped();
                mapped.addAll(filterMapped);
            }
        }
        filters.add(ignored);
        filters.add(mapped);
    }

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
                SipSingle visitorSingle = new SipSingle(id, filters, metaType, metadataPath, metadataContent);
                visitorSingle.addObserver(this);
                visitor = visitorSingle;
                break;
            case SIPPERFILE:
                SipPerFileVisitor visitorFile = new SipPerFileVisitor(id, filters, metaType, metadataPath, metadataContent);
                visitorFile.addObserver(this);
                visitor = visitorFile;
                break;
            default:
            case SIPPERFOLDER:
                SipPerFolderVisitor visitorFolder = new SipPerFolderVisitor(id, level, filters, metaType, metadataPath, metadataContent);
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
                SipPreview sipPreview = visit.getNext();
                SipPreviewNode sipNode = new SipPreviewNode(sipPreview, icon);
                sipPreview.addObserver(sipNode);
                sipNodes.add(sipNode);
                sips = visit.getSips();
            }
        }

        setChanged();
        notifyObservers();
    }
}
