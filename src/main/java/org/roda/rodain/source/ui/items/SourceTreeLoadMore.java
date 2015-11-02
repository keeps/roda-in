package org.roda.rodain.source.ui.items;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoadMore extends TreeItem<String> implements SourceTreeItem{
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/list-add.png"));

    public SourceTreeLoadMore(){
        super("Load More ...");
        this.setGraphic(new ImageView(fileImage));
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
    public void removeMapping(String ruleId) {

    }

    @Override
    public void removeIgnore() {

    }
}
