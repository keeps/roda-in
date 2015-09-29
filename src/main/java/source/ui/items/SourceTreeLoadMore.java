package source.ui.items;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeLoadMore extends TreeItem<Object> implements SourceTreeItem{
    private static final Logger log = Logger.getLogger(SourceTreeLoadMore.class.getName());
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/list-add.png"));

    public SourceTreeLoadMore(){
        super("Load More ...");
        this.setGraphic(new ImageView(fileImage));
    }

    public String getPath() {
        return null;
    }
}
