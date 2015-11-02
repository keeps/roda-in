package org.roda.rodain.rules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 *
 * Used in the Handlers to make a representation of the documents tree in a SIP
 */
public class TreeNode {
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

    public void addAll(Map<String, TreeNode> map){
        files.putAll(map);
    }
    public void add(TreeNode node){
        files.put(node.getPath().toString(), node);
    }
    public void add(Path node){
        files.put(node.toString(), new TreeNode(node));
    }
    public void remove(Path path){
        files.remove(path.toString());
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
}
