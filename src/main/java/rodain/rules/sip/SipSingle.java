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
public class SipSingle extends Observable implements TreeVisitor, SipCreator {
    private String startPath;
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;
    private Deque<TreeNode> nodes;
    private Set<TreeNode> files;

    private String id;
    private Set<ContentFilter> filters;
    private MetadataTypes metaType;
    private String metadata;

    public SipSingle(String id, Set<ContentFilter> filters, MetadataTypes metaType, String metadata){
        this.filters = filters;
        sips = new ArrayList<>();
        nodes = new ArrayDeque<>();
        this.id = id;
        this.metaType = metaType;
        this.metadata = metadata;
        files = new HashSet<>();
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
        else files.add(node);
    }

    @Override
    public void visitFile(Path path, BasicFileAttributes attrs) {
        if(filter(path))
            return;
        if(nodes.isEmpty())
            files.add(new TreeNode(path.toString()));
        else nodes.peekLast().add(path.toString());
    }

    @Override
    public void visitFileFailed(Path path) {
    }

    @Override
    public void end() {
        String meta = getMetadata();
        //create a new Sip
        Path path = Paths.get(startPath);
        sips.add(new SipPreview(path.toString(), files, meta));
        added++;

        setChanged();
        notifyObservers();
    }

    private String getMetadata(){
        if(metaType == MetadataTypes.SINGLEFILE)
            return metadata;
        return "";
    }

    @Override
    public String getId() {
        return id;
    }
}
