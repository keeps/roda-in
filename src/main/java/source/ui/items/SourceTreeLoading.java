package source.ui.items;

import javafx.scene.control.TreeItem;

import org.slf4j.LoggerFactory;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeLoading extends TreeItem<String> implements SourceTreeItem{
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SourceTreeLoading.class.getName());
    public SourceTreeLoading(){
        super("Loading...");
    }

    public String getPath() {
        return null;
    }
}
