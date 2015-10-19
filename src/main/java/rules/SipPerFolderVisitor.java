package rules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import utils.TreeVisitor;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipPerFolderVisitor extends Observable implements TreeVisitor {
    private static final int UPDATEFREQUENCY = 500; //in milliseconds
    private long lastUIUpdate = 0;
    private String startPath;
    private ArrayList<SipPreview> sips;
    private int maxLevel;
    private int added = 0, returned = 0;
    private Deque<TreeNode> nodes;
    private String id;

    public SipPerFolderVisitor(String startPath, String id, int maxLevel){
        this.startPath = startPath;
        this.maxLevel = maxLevel;
        sips = new ArrayList<>();
        nodes = new ArrayDeque<>();
        this.id = id;
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

    @Override
    public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
        TreeNode newNode = new TreeNode(path.toString());
        nodes.add(newNode);
    }

    @Override
    public void postVisitDirectory(Path path) {
        //pop the node of this directory and add it to its parent (if it exists)
        TreeNode node = nodes.pop();
        if(!nodes.isEmpty())
            nodes.peek().add(node);

        //Check if we create a new SIP using this node
        //every directory is a sub-directory of the start path, so if we remove it, we get the relative path to it
        String relative = path.toString().replace(startPath, "");
        Path relativePath = Paths.get(relative);
        int relativeLevel = relativePath.getNameCount();

        if(relativeLevel <= maxLevel){
            //create a new Sip
            String name = "sip_" + path.getFileName().toString();
            sips.add(new SipPreview(name, path.toString(), node));
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
        nodes.peek().add(path.toString());
    }

    @Override
    public void visitFileFailed(Path path) {
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
