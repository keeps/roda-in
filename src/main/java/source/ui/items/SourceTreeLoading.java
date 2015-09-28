package source.ui.items;

import javafx.scene.control.TreeItem;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeLoading extends TreeItem<String> implements SourceTreeItem{
    public SourceTreeLoading(){
        super("Loading...");
    }

    public String getPath() {
        return null;
    }
}
