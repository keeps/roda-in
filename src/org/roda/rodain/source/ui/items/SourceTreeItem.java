package rodain.source.ui.items;

/**
 * Created by adrapereira on 28-09-2015.
 */
public interface SourceTreeItem {
    String getPath();
    SourceTreeItemState getState();
    void ignore();
    void map();
    void toNormal();
}
