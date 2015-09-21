package Source;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceDirectory implements SourceItem {

    public Path path;
    private int itemsToLoad = 0;
    private TreeMap<String, SourceItem> children;
    private DirectoryStream<Path> directoryStream;
    private Iterator<Path> iterator;

    public SourceDirectory(Path path) {
        this.path = path;
        children = new TreeMap<String, SourceItem>();
    }

    public Path getPath() {
        return path;
    }

    public TreeMap<String, SourceItem> getChildren() {
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

    public boolean isStreamOpen(){return iterator.hasNext();}

    public void addChild(Path p, SourceItem item){
        children.put(p.toString(), item);
    }

    public void closeDirectoryStream(){
        try {
            if(directoryStream != null)
                directoryStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDirectoryStream(){
        if(directoryStream != null) return;

        try {
            directoryStream = Files.newDirectoryStream(path);
            iterator = directoryStream.iterator();
        }
        catch (AccessDeniedException e){}
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int firstLoad(){
        if(itemsToLoad != 0) return 0;
        return loadMore().size();
    }

    public boolean hasFirstLoaded(){return children.size() > 1;}

    public TreeMap<String, SourceItem> loadMore(){
        startDirectoryStream();
        int loaded = 0, childrenSize = children.size();
        TreeMap<String, SourceItem> result = new TreeMap<String, SourceItem>();
        itemsToLoad += 50;

        while(iterator.hasNext() && (childrenSize + loaded < itemsToLoad)){
            Path file = iterator.next();
            SourceItem added = loadChild(file);
            result.put(file.toString(), added);
            loaded++;
        }

        //we can close de directory stream if there's no more files to load in the iterator
        if(!iterator.hasNext()) closeDirectoryStream();

        return result;
    }

    private SourceItem loadChild(Path file){
        SourceItem item;
        if (Files.isDirectory(file)) {
            item = new SourceDirectory(file);
            addChild(file, item);

        }else{
            item = new SourceFile(file);
            addChild(file, item);
        }
        return item;
    }
}
