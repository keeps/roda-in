package schema;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by adrapereira on 22-09-2015.
 */
public class DescriptionObject {
    private static final Logger log = Logger.getLogger(DescriptionObject.class.getName());
    private String id;
    private String title;
    private String parentId;
    private String level;
    private String descriptionlevel;
    private ArrayList<DescriptionObject> children;

    public DescriptionObject() {
    }

    public DescriptionObject(String id, String title, String parentId, String level, String descriptionlevel, ArrayList<DescriptionObject> children) {
        this.id = id;
        this.title = title;
        this.parentId = parentId;
        this.level = level;
        this.descriptionlevel = descriptionlevel;
        this.children = children;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescriptionlevel() {
        return descriptionlevel;
    }

    public void setDescriptionlevel(String descriptionlevel) {
        this.descriptionlevel = descriptionlevel;
    }

    public ArrayList<DescriptionObject> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<DescriptionObject> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "DescriptionObject{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", parentId='" + parentId + '\'' +
                ", level='" + level + '\'' +
                ", descriptionlevel='" + descriptionlevel + '\'' +
                ", children=" + children +
                "}\n\n";
    }
}
