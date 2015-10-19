package rules;

/**
 * Created by adrapereira on 01-10-2015.
 */
public class SipPreview {
    private String name;
    private String path;
    private TreeNode files;

    public SipPreview(String name, String path, TreeNode files){
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

    public TreeNode getFiles() {
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
