package rules;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by adrapereira on 05-10-2015.
 *
 * Used in the Handlers to make a representation of the documents tree in a SIP
 */
public class TreeNode {
    private String path;
    private HashMap<String, TreeNode> files;

    public TreeNode(String path){
        this.path = path;
        files = new HashMap<String, TreeNode>();
    }
    public void add(TreeNode node){
        files.put(node.getPath(), node);
    }
    public void add(String node){files.put(node, new TreeNode(node));}
    public String getPath(){return path;}
    public Set<String> getKeys(){return files.keySet();}
    public TreeNode get(String key){return files.get(key);}
}
