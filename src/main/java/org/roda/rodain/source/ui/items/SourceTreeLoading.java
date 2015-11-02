package org.roda.rodain.source.ui.items;

import javafx.scene.control.TreeItem;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoading extends TreeItem<String> implements SourceTreeItem{
    public SourceTreeLoading(){
        super("Loading...");
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public SourceTreeItemState getState(){
        return SourceTreeItemState.NORMAL;
    }

    @Override
    public void addIgnore(){
    }

    @Override
    public void addMapping(String s){
    }

    @Override
    public void removeIgnore() {

    }

    @Override
    public void removeMapping(String ruleId) {

    }
}
