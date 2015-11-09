package org.roda.rodain.source.ui.items;

import javafx.scene.control.TreeItem;
import org.roda.rodain.rules.Rule;

import java.util.Observable;

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
    public void addMapping(Rule r){
    }

    @Override
    public void removeIgnore() {

    }

    @Override
    public void removeMapping(Rule r) {

    }

    @Override
    public void forceUpdate() {

    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
