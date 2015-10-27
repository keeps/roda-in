package rodain.rules;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by adrapereira on 05-10-2015.
 *
 * Used in the Handlers to make a representation of the documents tree in a SIP
 */
public class TreeNode {
    private String path;
    private Map<String, TreeNode> files;

    public TreeNode(String path){
        this.path = path;
        files = new HashMap<>();
    }

    public void flatten(){
        Map<String, TreeNode> newFiles = new HashMap<>();
        for(String path: files.keySet()){
            if(Files.isDirectory(Paths.get(path))){
                files.get(path).flatten(); //flatten the children
                newFiles.putAll(files.get(path).getOnlyFiles()); //add its files to the new Map
            }else newFiles.put(path, files.get(path));
        }
        files = newFiles;
    }

    public Map<String, TreeNode> getAllFiles(){
        return files;
    }

    public Map<String, TreeNode> getOnlyFiles(){
        Map<String, TreeNode> result = new HashMap<>();
        for(String path: files.keySet()){
            if(!Files.isDirectory(Paths.get(path))) //add to result if it's a file
                result.put(path, files.get(path));
        }
        return result;
    }

    public void addAll(Map<String, TreeNode> map){
        files.putAll(map);
    }
    public void add(TreeNode node){
        files.put(node.getPath(), node);
    }
    public void add(String node){
        files.put(node, new TreeNode(node));
    }
    public void remove(String path){
        files.remove(path);
    }
    public String getPath(){
        return path;
    }
    public Set<String> getKeys(){
        return files.keySet();
    }
    public TreeNode get(String key){
        return files.get(key);
    }
}
