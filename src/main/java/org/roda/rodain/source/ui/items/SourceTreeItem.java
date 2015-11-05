package org.roda.rodain.source.ui.items;

import java.util.Observer;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */

// TODO change this to a class
public interface SourceTreeItem extends Observer {
    String getPath();
    SourceTreeItemState getState();
    void addIgnore();
    void removeIgnore();
    void addMapping();
    void removeMapping(Set<String> removed);
    void forceUpdate();
}
