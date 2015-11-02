package org.roda.rodain.source.ui.items;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public interface SourceTreeItem {
    String getPath();
    SourceTreeItemState getState();
    void ignore();
    void map(String ruleId);
    void unignore();
    void unmap(String ruleId);
}
