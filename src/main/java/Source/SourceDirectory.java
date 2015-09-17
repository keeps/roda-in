package Source;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceDirectory implements SourceItem {

    private Path path;
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
        return loadMore();
    }

    public int loadMore(){
        startDirectoryStream();
        int loaded = 0, childrenSize = children.size();
        itemsToLoad += 50;

        while(iterator.hasNext() && (childrenSize + loaded < itemsToLoad)){
            loadChild();
            loaded++;
        }

        //we can close de directory stream if there's no more files to load in the iterator
        if(!iterator.hasNext()) closeDirectoryStream();

        return loaded;
    }

    private void loadChild(){
        Path file = iterator.next();
        if (Files.isDirectory(file)) {
            addChild(file, new SourceDirectory(file));
        }else addChild(file, new SourceFile(file));
    }

    public void loadChildren(){
        //System.out.println(path);
        try {
            DirectoryStream<Path> dir = Files.newDirectoryStream(path);
            Iterator<Path> iterator = dir.iterator();
            int addedFiles = 0;
            Path file;
            while(iterator.hasNext() && addedFiles < itemsToLoad){
                file = iterator.next();
                if (Files.isDirectory(file)) {
                    if(file.getNameCount() <= 5)
                        addChild(file, new SourceDirectory(file));
                }else addChild(file, new SourceFile(file));
                addedFiles++;
            }
            dir.close();
        }
        catch (AccessDeniedException e){}
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
