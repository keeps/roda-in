package rodain.source.ui.items;

/**
 * Created by adrapereira on 28-09-2015.
 */
public interface SourceTreeItem {
    String getPath();
    SourceTreeItemState getState();
    void ignore();
    void map(String ruleId);
    void unignore();
    void unmap(String ruleId);
}
