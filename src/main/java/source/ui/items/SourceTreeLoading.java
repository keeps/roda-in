package source.ui.items;

import javafx.scene.control.TreeItem;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeLoading extends TreeItem<Object> implements SourceTreeItem{
    private static final Logger log = Logger.getLogger(SourceTreeLoading.class.getName());
    public SourceTreeLoading(){
        super("Loading...");
    }

    public String getPath() {
        return null;
    }
}
