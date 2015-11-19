package org.roda.rodain.rules.sip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.utils.TreeVisitor;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipSingle extends Observable implements TreeVisitor, SipPreviewCreator {
    private String startPath;
    // This map is returned, in full, to the SipPreviewNode when there's an update
    private Map<String, SipPreview> sipsMap;
    // This ArrayList is used to keep the SIPs ordered.
    // We need them ordered because we have to keep track of which SIPs have already been loaded
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;
    private Deque<TreeNode> nodes;
    private Set<TreeNode> files;

    private String id;
    private Set<ContentFilter> filters;
    private MetadataTypes metaType;
    private Path metadataPath;
    private String metadataContent;

    public SipSingle(String id, Set<ContentFilter> filters, MetadataTypes metaType, Path metadataPath, String metadataContent){
        this.filters = filters;
        sipsMap = new HashMap<>();
        sips = new ArrayList<>();
        nodes = new ArrayDeque<>();
        this.id = id;
        this.metaType = metaType;
        this.metadataPath = metadataPath;
        this.metadataContent = metadataContent;
        files = new HashSet<>();
    }

    @Override
    public Map<String, SipPreview> getSips() {
        return sipsMap;
    }
    @Override
    public int getCount(){
        return added;
    }
    @Override
    public SipPreview getNext(){
        return sips.get(returned++);
    }
    @Override
    public boolean hasNext(){
        return returned < added;
    }

    private boolean filter(Path path){
        String pathString = path.toString();
        for(ContentFilter cf: filters){
            if(cf.filter(pathString))
                return true;
        }
        return false;
    }

    @Override
    public void setStartPath(String st){
        startPath = st;
    }

    @Override
    public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
        if(filter(path))
            return;
        TreeNode newNode = new TreeNode(path);
        nodes.add(newNode);
    }

    @Override
    public void postVisitDirectory(Path path) {
        if(filter(path))
            return;
        //pop the node of this directory and add it to its parent (if it exists)
        TreeNode node = nodes.removeLast();
        if(!nodes.isEmpty())
            nodes.peekLast().add(node);
        else files.add(node);
    }

    @Override
    public void visitFile(Path path, BasicFileAttributes attrs) {
        if(filter(path)) {
            return;
        }
        if(nodes.isEmpty())
            files.add(new TreeNode(path));
        else nodes.peekLast().add(path);
    }

    @Override
    public void visitFileFailed(Path path) {
    }

    @Override
    public void end() {
        Path metaPath = getMetadata();
        //create a new Sip
        Path path = Paths.get(startPath);
        SipPreview sipPreview = new SipPreview(path.getFileName().toString(), files, metaPath, metadataContent);

        for(TreeNode tn: files){
            tn.addObserver(sipPreview);
        }

        sips.add(sipPreview);
        sipsMap.put(sipPreview.getId(), sipPreview);
        added++;

        setChanged();
        notifyObservers();
    }

    private Path getMetadata(){
        Path result = null;
        if(metaType == MetadataTypes.SINGLEFILE)
            result = metadataPath;
        return result;
    }

    @Override
    public String getId() {
        return id;
    }
}
