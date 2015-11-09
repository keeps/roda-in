package org.roda.rodain.source.ui.items;

import javafx.scene.control.TreeItem;
import org.roda.rodain.rules.Rule;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */

public class SourceTreeItem extends TreeItem<String> implements Observer {
    private String path;
    private SourceTreeItemState state;

    protected SourceTreeItem(String path){
        this.path = path;
        state = SourceTreeItemState.NORMAL;
    }

    public String getPath(){
        return path;
    }

    public SourceTreeItemState getState(){
        return state;
    }

    public void addIgnore(){

    }
    public void removeIgnore(){

    }
    public void addMapping(Rule r){

    }
    public void removeMapping(Rule r){

    }

    public void forceUpdate() {
        String value = getValue();
        if(value != null && !"".equals(value)) {
            setValue("");
            setValue(value);
        }
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
