package org.roda.rodain.core;

import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-11-2015.
 */
public class PathCollection{
    private static Map<String, SourceTreeItemState> states = new HashMap<>();
    private static Map<String, SourceTreeItem> items = new HashMap<>();


    private PathCollection(){
    }

    public static void addPath(String path, SourceTreeItemState st){
        states.put(path, st);
        checkStateParents(path);
        //if there's an item with this path and it's states don't match
        if(items.containsKey(path)){
            SourceTreeItem item = items.get(path);
            if(item.getState() != states.get(path)){
                item.setState(states.get(path));
                checkStateParents(path);
            }
        }
    }

    public static void addPaths(Set<String> paths, SourceTreeItemState st){
        for(String path: paths)
            addPath(path, st);
    }

    public static void addItem(SourceTreeItem item){
        String path = item.getPath();
        if(! states.containsKey(path)){
            states.put(path, item.getState());
        }
        items.put(path, item);
    }

    public static SourceTreeItemState getState(String path){
        SourceTreeItemState result = SourceTreeItemState.NORMAL;
        if(states.containsKey(path))
            result = states.get(path);
        return result;
    }

    public static SourceTreeItem getItem(String path){
        return items.get(path);
    }

    private static void checkStateParents(String path){
        int index = 0, end = path.length();
        String separator = File.separator;

        while (index >= 0) { //while we still have string to read and haven't found a matching path
            index = path.lastIndexOf(separator, end); //get the path until the slash we're checking
            if (index == -1) {
                break;
            } else {
                String sub = path.substring(0, index);
                end = index - 1; // move the starting index for the next iteration so it's before the slash
                if(items.containsKey(sub) && items.get(sub) instanceof SourceTreeDirectory){
                    SourceTreeDirectory dir = (SourceTreeDirectory) items.get(sub);
                    dir.verifyState();
                    if(states.get(path) == SourceTreeItemState.NORMAL && dir.getState() == SourceTreeItemState.MAPPED){
                        addPath(sub, SourceTreeItemState.NORMAL);
                    }
                }
            }
        }
    }
}