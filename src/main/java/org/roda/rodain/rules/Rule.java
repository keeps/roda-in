package org.roda.rodain.rules;

import javafx.scene.image.Image;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.rules.sip.*;
import org.roda.rodain.utils.FontAwesomeImageCreator;
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

    // map of SipPreview id -> SipPreview
    private Map<String, SipPreview> sips;
    // map of SipPreview id -> SipPreviewNode
    private HashMap<String, SipPreviewNode> sipNodes = new HashMap<>();
    private TreeVisitor visitor;
    private Image icon;
    private int added = 0;
    private int level;
    private String id;

    // removed items
    private Set<String> removed;

    /*
     Keep a set of mapped paths to be used when the source tree needs to find if a file has been mapped.
     This set will only be filled when the first search is made.
     Due to the nature of the visitor, we need to save the sips map size to know if between each search there was no modification.
     If there was a modification then we rebuild the mapped set.
    */
    private Set<String> mapped;
    private int lastSipsSize = -1;

    public Rule(Set<SourceTreeItem> source, RuleTypes assocType, int level, Path metadataPath, String metadataContent, MetadataTypes metaType){
        this.source = source;
        this.assocType = assocType;
        this.level = level;
        this.metadataContent = metadataContent;
        this.metadataPath = metadataPath;
        this.metaType = metaType;
        filters = new HashSet<>();
        removed = new HashSet<>();
        id = UUID.randomUUID().toString();

        createIcon();
        createFilters();
    }

    private void createIcon(){
        ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
        String category = hierarchyConfig.getString("category.item");
        String unicode = hierarchyConfig.getString("icon." + category);

        icon = FontAwesomeImageCreator.generate(unicode);
    }

    private void createFilters(){
        ContentFilter filter = new ContentFilter();
        for(SourceTreeItem sti: source) {
            // add this item to the filter if it's ignored or mapped
            if(sti.getState() == SourceTreeItemState.IGNORED)
                filter.addIgnored(sti.getPath());
            else if(sti.getState() == SourceTreeItemState.MAPPED)
                filter.addMapped(sti.getPath());
            //if it's a directory, get all its mapped and ignored children and add to the filters
            if(sti instanceof SourceTreeDirectory) {
                Set<String> filterIgnored = ((SourceTreeDirectory) sti).getIgnored();
                filter.addAllIgnored(filterIgnored);
                Set<String> filterMapped = ((SourceTreeDirectory) sti).getMapped();
                filter.addAllMapped(filterMapped);
            }
        }
        filters.add(filter);
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

    public Collection<SipPreview> getSips() {
        return sips.values();
    }

    public void setSource(Set<SourceTreeItem> source) {
        this.source = source;
    }

    public TreeVisitor getVisitor(){
        return visitor;
    }
    public int getSipCount() {
        return sips.size();
    }

    public Collection<SipPreviewNode> getSipNodes(){
        return sipNodes.values();
    }

    public TreeVisitor apply(){
        return apply(assocType, level);
    }

    public TreeVisitor apply(RuleTypes type, int level){
        this.assocType = type;
        added = 0;
        sips = new HashMap<>();
        sipNodes = new HashMap<>();

        switch (type){
            case SIPPERFOLDER:
                SipPerFolderVisitor visitorFolder = new SipPerFolderVisitor(id, level, filters, metaType, metadataPath, metadataContent);
                visitorFolder.addObserver(this);
                visitor = visitorFolder;
                break;
            case SIPPERSELECTION:
                //create a set with the paths of the selected items
                Set<String> selection = new HashSet<>();
                for(SourceTreeItem sti: source) {
                    selection.add(sti.getPath());
                }
                SipPerSelection visitorSelection = new SipPerSelection(id, selection, filters, metaType, metadataPath, metadataContent);
                visitorSelection.addObserver(this);
                visitor = visitorSelection;
                break;
            case SIPPERFILE:
                SipPerFileVisitor visitorFile = new SipPerFileVisitor(id, filters, metaType, metadataPath, metadataContent);
                visitorFile.addObserver(this);
                visitor = visitorFile;
                break;
            default:
            case SINGLESIP:
                SipSingle visitorSingle = new SipSingle(id, filters, metaType, metadataPath, metadataContent);
                visitorSingle.addObserver(this);
                visitor = visitorSingle;
                break;
        }
        return visitor;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof SipCreator){
            SipCreator visit = (SipCreator) o;
            sips = visit.getSips();
            while(visit.hasNext() && added < 100){
                added++;
                SipPreview sipPreview = visit.getNext();
                SipPreviewNode sipNode = new SipPreviewNode(sipPreview, icon);
                sipPreview.addObserver(sipNode);
                sipPreview.addObserver(this);
                sipNodes.put(sipPreview.getId(), sipNode);
            }
        }else if(o instanceof SipPreview){
            SipPreview sip = (SipPreview) o;
            if(sip.isRemoved()){
                sipNodes.remove(sip.getId());
                sips.remove(sip.getId());
                removed = new HashSet<>();
                for(TreeNode tn: sip.getFiles()){
                    removed.addAll(tn.getFullTreePaths());
                }
            }
        }

        setChanged();
        notifyObservers("Removed SIP");
    }

    public Set<String> getRemoved(){
        return removed;
    }

    public boolean isMapped(String path){
        if(lastSipsSize != sips.size()){
            //create the set of mapped paths
            mapped = new HashSet<>();
            lastSipsSize = sips.size();
            for(SipPreview sp: sips.values()){
                for(TreeNode tn: sp.getFiles()){
                    mapped.addAll(tn.getFullTreePaths());
                }
            }
        }
        return mapped.contains(path);
    }
}
