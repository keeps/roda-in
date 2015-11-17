package org.roda.rodain.core;

import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-11-2015.
 */
public class PathCollection {
    private static Map<String, SourceTreeItemState> states = new HashMap<>();
    private static Map<String, SourceTreeItem> items = new HashMap<>();

    private PathCollection(){
    }

    public static void addPath(String path, SourceTreeItemState st){
        states.put(path, st);
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
}