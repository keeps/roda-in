package rodain.rules.sip;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import rodain.rules.TreeNode;
import rodain.rules.filters.ContentFilter;
import rodain.utils.TreeVisitor;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipPerFileVisitor extends Observable implements TreeVisitor, SipCreator {
    private static final int UPDATEFREQUENCY = 500; //in milliseconds
    private ArrayList<SipPreview> sips;
    private Set<ContentFilter> filters;
    private int added = 0, returned = 0;
    private long lastUIUpdate = 0;
    private String id;

    public SipPerFileVisitor(String id, Set<ContentFilter> filters){
        sips = new ArrayList<>();
        this.id = id;
        this.filters = filters;
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
    @Override
    public void setStartPath(String st){
    }

    @Override
    public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
    }

    @Override
    public void postVisitDirectory(Path path) {

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
    public void visitFile(Path path, BasicFileAttributes attrs) {
        if(filter(path))
            return;

        String name = "sip_" + path.getFileName().toString();
        TreeNode node = new TreeNode(path.toString());
        Set<TreeNode> files = new HashSet<>();
        files.add(node);
        sips.add(new SipPreview(name, path.toString(), files));
        added ++;

        long now = System.currentTimeMillis();
        if(now - lastUIUpdate > UPDATEFREQUENCY) {
            setChanged();
            notifyObservers();
            lastUIUpdate = now;
        }
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
    public String getId(){
        return id;
    }
}
