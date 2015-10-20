package rules.sip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import rules.TreeNode;
import rules.filters.ContentFilter;
import utils.TreeVisitor;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipSingle extends Observable implements TreeVisitor, SipCreator {
    private String startPath;
    private Set<ContentFilter> filters;
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;
    private Deque<TreeNode> nodes;
    private String id;
    private Set<TreeNode> files;

    public SipSingle(String id, Set<ContentFilter> filters){
        this.filters = filters;
        sips = new ArrayList<>();
        nodes = new ArrayDeque<>();
        this.id = id;
        files = new HashSet<>();
    }

    public List<SipPreview> getSips() {
        return sips;
    }
    public int getCount(){
        return added;
    }
    public SipPreview getNext(){
        return sips.get(returned++);
    }
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
        if(filter(path)) return;
        TreeNode newNode = new TreeNode(path.toString());
        nodes.add(newNode);
    }

    @Override
    public void postVisitDirectory(Path path) {
        if(filter(path)) return;
        //pop the node of this directory and add it to its parent (if it exists)
        TreeNode node = nodes.pop();
        if(!nodes.isEmpty())
            nodes.peek().add(node);
        else files.add(node);
    }

    @Override
    public void visitFile(Path path, BasicFileAttributes attrs) {
        if(filter(path)) return;
        if(nodes.isEmpty())
            files.add(new TreeNode(path.toString()));
        else nodes.peek().add(path.toString());
    }

    @Override
    public void visitFileFailed(Path path) {
    }

    @Override
    public void end() {
        //create a new Sip
        Path path = Paths.get(startPath);
        String name = "sip_" + path.getFileName().toString();
        sips.add(new SipPreview(name, path.toString(), files));
        added++;

        setChanged();
        notifyObservers();
    }

    @Override
    public String getId() {
        return id;
    }
}
