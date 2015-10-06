package source.ui;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Observable;

import org.slf4j.LoggerFactory;
import utils.TreeVisitor;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class ComputeDirectorySize extends Observable implements TreeVisitor {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ComputeDirectorySize.class.getName());
    private final int UPDATEFREQUENCY = 500; //in milliseconds
    private long filesCount = 0, directoryCount, size = 0;
    private long lastUIUpdate = 0;

    public ComputeDirectorySize(){ }

    public void preVisitDirectory(Path path, BasicFileAttributes attrs) {}

    public void postVisitDirectory(Path path) {
        directoryCount++;
        update();
    }

    public void visitFile(Path path, BasicFileAttributes attrs) {
        size += attrs.size();
        filesCount++;
        update();
    }

    public void visitFileFailed(Path path) {}

    public void end() {
        setChanged();
        notifyObservers();
    }

    private void update(){
        long now = System.currentTimeMillis();
        if(now - lastUIUpdate > UPDATEFREQUENCY) {
            setChanged();
            notifyObservers();
            lastUIUpdate = now;
        }
    }

    public long getFilesCount() {
        return filesCount;
    }

    public long getDirectoryCount() {
        return directoryCount;
    }

    public long getSize() {
        return size;
    }
    public String getId(){return "dirSize";}
}
