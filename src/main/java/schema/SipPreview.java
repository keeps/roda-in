package schema;

import org.slf4j.LoggerFactory;
import rules.TreeNode;

import java.util.Map;

/**
 * Created by adrapereira on 01-10-2015.
 */
public class SipPreview {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipPreview.class.getName());
    private String name;
    private String path;
    private Map<String, TreeNode> files;

    public SipPreview(String name, String path, Map<String, TreeNode> files){
        this.name = name;
        this.path = path;
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Map<String, TreeNode> getFiles() {
        return files;
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
