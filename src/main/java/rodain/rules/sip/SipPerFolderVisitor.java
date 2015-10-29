package rodain.rules.sip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import rodain.rules.MetadataTypes;
import rodain.rules.TreeNode;
import rodain.rules.filters.ContentFilter;
import rodain.utils.TreeVisitor;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipPerFolderVisitor extends Observable implements TreeVisitor, SipCreator {
    private static final int UPDATEFREQUENCY = 500; //in milliseconds
    private long lastUIUpdate = 0;
    private String startPath;
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;
    private Deque<TreeNode> nodes;

    private String id;
    private int maxLevel;
    private Set<ContentFilter> filters;
    private MetadataTypes metaType;
    private String metadata;

    public SipPerFolderVisitor(String id, int maxLevel, Set<ContentFilter> filters, MetadataTypes metaType, String metadata){
        this.maxLevel = maxLevel;
        this.filters = filters;
        this.metaType = metaType;
        this.metadata = metadata;
        sips = new ArrayList<>();
        nodes = new ArrayDeque<>();
        this.id = id;
    }

    @Override
    public List<SipPreview> getSips() {
        return sips;
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
        TreeNode newNode = new TreeNode(path.toString());
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
            String meta = getMetadata();
            //create a new Sip
            Set<TreeNode> files = new HashSet<>();
            files.add(node);
            sips.add(new SipPreview(path.toString(), files, meta));
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
        nodes.peekLast().add(path.toString());
    }

    @Override
    public void visitFileFailed(Path path) {
    }

    private String getMetadata(){
        if(metaType == MetadataTypes.SINGLEFILE)
            return metadata;
        return "";
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
