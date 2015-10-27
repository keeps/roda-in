package rodain.rules.sip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.apache.commons.io.FilenameUtils;

import rodain.rules.MetadataTypes;
import rodain.rules.TreeNode;
import rodain.rules.filters.ContentFilter;
import rodain.utils.TreeVisitor;
import rodain.utils.Utils;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipPerFileVisitor extends Observable implements TreeVisitor, SipCreator {
    private static final int UPDATEFREQUENCY = 500; //in milliseconds
    private ArrayList<SipPreview> sips;
    private int added = 0, returned = 0;
    private long lastUIUpdate = 0;

    private String id;
    private Set<ContentFilter> filters;
    private MetadataTypes metaType;
    private String metadata;

    public SipPerFileVisitor(String id, Set<ContentFilter> filters, MetadataTypes metaType, String metadata){
        sips = new ArrayList<>();
        this.id = id;
        this.filters = filters;
        this.metaType = metaType;
        this.metadata = metadata;
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

        String meta = getMetadata(path);

        String name = "sip_" + path.getFileName().toString();
        TreeNode node = new TreeNode(path.toString());
        Set<TreeNode> files = new HashSet<>();
        files.add(node);
        sips.add(new SipPreview(name, path.toString(), files, meta));
        added ++;

        long now = System.currentTimeMillis();
        if(now - lastUIUpdate > UPDATEFREQUENCY) {
            setChanged();
            notifyObservers();
            lastUIUpdate = now;
        }
    }

    private String getMetadata(Path path){
        switch (metaType){
            case SINGLEFILE:
                return metadata;
            case DIFFDIRECTORY: // uses the same logic as the next case
            case SAMEDIRECTORY:
                Path metaFile = getFileFromDir(path);
                if(metaFile != null){
                    return metaFile.toString();
                }
                break;
            default:
                return "";
        }
        return "";
    }

    private Path getFileFromDir(Path path){
        String fileName = FilenameUtils.removeExtension(path.getFileName().toString());
        Path newPath = Paths.get(metadata + "/" + fileName + ".xml");
        if(Files.exists(newPath)){
            return newPath;
        }
        newPath = Paths.get(metadata + "/" + path.getFileName() + ".xml");
        if(Files.exists(newPath)){
            return newPath;
        }
        return null;
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
