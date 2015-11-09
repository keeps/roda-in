package org.roda.rodain.source.ui.items;

import org.roda.rodain.rules.Rule;

import java.util.Observer;

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
    void addMapping(Rule r);
    void removeMapping(Rule r);
    void forceUpdate();
}
