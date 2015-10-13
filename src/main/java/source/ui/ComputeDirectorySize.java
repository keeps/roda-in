package source.ui;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Observable;

import utils.TreeVisitor;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class ComputeDirectorySize extends Observable implements TreeVisitor {
    private static final int UPDATE_FREQUENCY = 500; //in milliseconds
    private long filesCount = 0, directoryCount, size = 0;
    private long lastUIUpdate = 0;

    public ComputeDirectorySize(){
    }

    @Override

    public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
        /*
        * This FileVisitor doesn't need to need to do anything before visiting a directory
        */
    }

    @Override
    public void postVisitDirectory(Path path) {
        directoryCount++;
        update();
    }

    @Override
    public void visitFile(Path path, BasicFileAttributes attrs) {
        size += attrs.size();
        filesCount++;
        update();
    }

    @Override

    public void visitFileFailed(Path path) {
        /*
        * This FileVisitor doesn't need to need to do anything if the visit failed
        */
    }

    @Override
    public void end() {
        setChanged();
        notifyObservers();
    }

    private void update(){
        long now = System.currentTimeMillis();
        if(now - lastUIUpdate > UPDATE_FREQUENCY) {
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

    @Override
    public String getId(){
        return "dirSize";
    }
}
