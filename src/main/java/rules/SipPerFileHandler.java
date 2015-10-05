package rules;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import org.slf4j.LoggerFactory;

import schema.SipPreview;
import utils.TreeWalkHandler;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipPerFileHandler extends Observable implements TreeWalkHandler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipPerFileHandler.class.getName());
    private String startPath;
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;

    public SipPerFileHandler(String startPath){
        this.startPath = startPath;
        sips = new ArrayList<SipPreview>();

        //filesTree =  new HashMap<String, HashMap<String, Object>>();
    }

    public ArrayList<SipPreview> getSips() {return sips;}
    public int getCount(){return added;}
    public SipPreview getNext(){return sips.get(returned++);}
    public boolean hasNext(){return returned < added;}

    public void preVisitDirectory(Path path) {    }

    public void postVisitDirectory(Path path) {    }

    public void visitFile(Path path, BasicFileAttributes attrs) {
        String name = "sip_" + path.getFileName().toString();
        HashMap<String, TreeNode> files = new HashMap<String, TreeNode>();
        files.put(path.toString(), null);
        sips.add(new SipPreview(name, path.toString(), files));
        added ++;
        if(added % 1000 == 0){ //update every 1000 sips
            setChanged();
            notifyObservers();
        }
    }

    public void visitFileFailed(Path path) {    }

    public void end() {
        setChanged();
        notifyObservers();
    }
}
