package org.roda.rodain.source.ui.items;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public interface SourceTreeItem {
    String getPath();
    SourceTreeItemState getState();
    void addIgnore();
    void removeIgnore();
    void addMapping(String ruleId);
    void removeMapping(String ruleId);
}
