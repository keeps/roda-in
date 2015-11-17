package org.roda.rodain.rules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 *
 * Used in the Handlers to make a representation of the documents tree in a SIP
 */
public class TreeNode extends Observable{
    private Path path;
    private Map<String, TreeNode> files;

    public TreeNode(Path path){
        this.path = path;
        files = new HashMap<>();
    }

    public void flatten(){
        Map<String, TreeNode> newFiles = new HashMap<>();
        for(String file: files.keySet()){
            if(Files.isDirectory(Paths.get(file))){
                files.get(file).flatten(); //flatten the children
                newFiles.putAll(files.get(file).getOnlyFiles()); //add its files to the new Map
            }else newFiles.put(file, files.get(file));
        }
        files = newFiles;

        changed();
    }

    public Set<String> getFullTreePaths(){
        Set<String> result = new HashSet<>();
        result.add(path.toString());
        for(TreeNode tn: files.values())
            result.addAll(tn.getFullTreePaths());
        return result;
    }

    public Map<String, TreeNode> getAllFiles(){
        return files;
    }

    public Map<String, TreeNode> getOnlyFiles(){
        Map<String, TreeNode> result = new HashMap<>();
        for(String file: files.keySet()){
            if(!Files.isDirectory(Paths.get(file))) //add to result if it's a file
                result.put(file, files.get(file));
        }
        return result;
    }

    /**
     *
     * If an item's path is in the selected paths to be ignored, this method returns a set of the item's full tree.
     * Else, calls this method in all its children, returning all the paths removed in the children.
     *
     * @param paths is a Set of the selected paths to be ignored
     * @return a Set of all the paths removed
     */
    public Set<String> ignoreContent(Set<Path> paths) {
        Set<String> result = new HashSet<>();
        if(paths.contains(path)){
            //this item and all its children
            result.addAll(getFullTreePaths());
        }else{
            Set<Path> toRemove = new HashSet<>();
            for(TreeNode tn: files.values()) {
                result.addAll(tn.ignoreContent(paths));
                if(paths.contains(tn.path))
                    toRemove.add(tn.path);
            }
            if(! toRemove.isEmpty()){
                for(Path p: toRemove)
                    remove(p);
            }
        }
        return result;
    }

    public void addAll(Map<String, TreeNode> map){
        files.putAll(map);
        changed();
    }
    public void add(TreeNode node){
        files.put(node.getPath().toString(), node);
        changed();
    }
    public void add(Path node){
        files.put(node.toString(), new TreeNode(node));
        changed();
    }
    public TreeNode remove(Path path){
        TreeNode result = files.get(path.toString());
        files.remove(path.toString());
        changed();
        return result;
    }
    public Path getPath(){
        return path;
    }
    public Set<String> getKeys(){
        return files.keySet();
    }
    public TreeNode get(String key){
        return files.get(key);
    }

    private void changed(){
        setChanged();
        notifyObservers();
    }

    @Override
    public void addObserver(Observer o){
        super.addObserver(o);
        for(TreeNode tn: files.values()){
            tn.addObserver(o);
        }
    }
}
