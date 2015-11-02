package org.roda.rodain.source.representation;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceDirectory implements SourceItem {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SourceDirectory.class.getName());
    private Path path;
    private int itemsToLoad = 0;
    private TreeMap<String, SourceItem> children;
    private DirectoryStream<Path> directoryStream;
    private Iterator<Path> iterator;
    private boolean showFiles;

    public SourceDirectory(Path path, boolean showFiles) {
        this.path = path;
        this.showFiles = showFiles;
        children = new TreeMap<>();
    }

    public Path getPath() {
        return path;
    }

    public SortedMap<String, SourceItem> getChildren() {
        return children;
    }

    public SourceItem getChild(Path p){
        return children.get(p.toString());
    }

    public SourceDirectory getChildDirectory(Path p){
        if(Files.isDirectory(p))
            return (SourceDirectory)children.get(p.toString());
        return null;
    }

    public boolean isStreamOpen(){
        return iterator.hasNext();
    }

    public void addChild(Path p, SourceItem item){
        children.put(p.toString(), item);
    }

    public void closeDirectoryStream(){
        try {
            if(directoryStream != null)
                directoryStream.close();
        } catch (IOException e) {
            log.error("Error closing directory stream", e);
        }
    }

    private void startDirectoryStream(){
        if(directoryStream != null)
            return;

        try {
            directoryStream = Files.newDirectoryStream(path);
            iterator = directoryStream.iterator();
        }
        catch (AccessDeniedException e){
            log.info("No access to file", e);
        }
        catch (IOException e) {
            log.error("Error accessing file", e);
        }
    }

    public boolean hasFirstLoaded(){
        return children.size() > 1;
    }

    public SortedMap<String, SourceItem> loadMore(){
        startDirectoryStream();
        int loaded = 0, childrenSize = children.size();
        TreeMap<String, SourceItem> result = new TreeMap<>();
        itemsToLoad += 50;
        while(iterator.hasNext() && (childrenSize + loaded < itemsToLoad)){
            Path file = iterator.next();
            if(!showFiles && !Files.isDirectory(file))
                continue;
            SourceItem added = loadChild(file);
            result.put(file.toString(), added);
            loaded++;
        }
        //we can close the directory stream if there's no more files to load in the iterator
        if(!iterator.hasNext())
            closeDirectoryStream();

        return result;
    }

    private SourceItem loadChild(Path file){
        SourceItem item;
        if (Files.isDirectory(file)) {
            item = new SourceDirectory(file, showFiles);
            addChild(file, item);

        }else{
            item = new SourceFile(file);
            addChild(file, item);
        }
        return item;
    }
}
