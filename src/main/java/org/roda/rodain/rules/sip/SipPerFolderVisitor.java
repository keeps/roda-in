package org.roda.rodain.rules.sip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.utils.TreeVisitor;
import org.roda.rodain.rules.MetadataTypes;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPerFolderVisitor extends Observable implements TreeVisitor, SipCreator {
    private static final int UPDATEFREQUENCY = 500; //in milliseconds
    private long lastUIUpdate = 0;
    private String startPath;
    // This map is returned, in full, to the SipPreviewNode when there's an update
    private Map<String, SipPreview> sipsMap;
    // This ArrayList is used to keep the SIPs ordered.
    // We need them ordered because we have to keep track of which SIPs have already been loaded
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;
    private Deque<TreeNode> nodes;

    private String id;
    private int maxLevel;
    private Set<ContentFilter> filters;
    private MetadataTypes metaType;
    private Path metadataPath;
    private String metadataContent;

    public SipPerFolderVisitor(String id, int maxLevel, Set<ContentFilter> filters, MetadataTypes metaType,
                               Path metadataPath, String metadataContent){
        this.maxLevel = maxLevel;
        this.filters = filters;
        this.metaType = metaType;
        this.metadataPath = metadataPath;
        this.metadataContent = metadataContent;
        sips = new ArrayList<>();
        sipsMap = new HashMap<>();
        nodes = new ArrayDeque<>();
        this.id = id;
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

        //Check if we create a new SIP using this node
        //every directory is a sub-directory of the start path, so if we remove it, we get the relative path to it
        String relative = path.toString().replace(startPath, "");
        Path relativePath = Paths.get(relative);
        int relativeLevel = relativePath.getNameCount();

        if(relativeLevel <= maxLevel){
            Path metaPath = getMetadataPath();
            //create a new Sip
            Set<TreeNode> files = new HashSet<>();
            files.add(node);

            SipPreview sipPreview = new SipPreview(path.getFileName().toString(), files, metaPath, metadataContent);
            node.addObserver(sipPreview);

            sips.add(sipPreview);
            sipsMap.put(sipPreview.getId(), sipPreview);
            added++;

            long now = System.currentTimeMillis();
            if(now - lastUIUpdate > UPDATEFREQUENCY) {
                setChanged();
                notifyObservers();
                lastUIUpdate = now;
            }
        }
    }

    @Override
    public void visitFile(Path path, BasicFileAttributes attrs) {
        if(filter(path))
            return;
        nodes.peekLast().add(path);
    }

    @Override
    public void visitFileFailed(Path path) {
    }

    private Path getMetadataPath(){
        Path result = null;
        if(metaType == MetadataTypes.SINGLEFILE)
            result = metadataPath;
        return result;
    }

    @Override
    public void end() {
        setChanged();
        notifyObservers();
    }

    @Override
    public String getId() {
        return id;
    }
}
