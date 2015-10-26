package rodain.rules.sip;

import rodain.rules.TreeNode;

import java.util.Set;

/**
 * Created by adrapereira on 01-10-2015.
 */
public class SipPreview {
    private String name;
    private String path;
    private String metadata;
    private Set<TreeNode> files;

    public SipPreview(String name, String path, Set<TreeNode> files, String metadata){
        this.name = name;
        this.path = path;
        this.files = files;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Set<TreeNode> getFiles() {
        return files;
    }

    public String getMetadata(){
        return metadata;
    }

    @Override
    public String toString() {
        return "SipPreview{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", files=" + files +
                '}';
    }
}
