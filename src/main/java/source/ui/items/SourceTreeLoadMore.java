package source.ui.items;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeLoadMore extends TreeItem<Object> implements SourceTreeItem{
    public static Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("list-add.png"));

    public SourceTreeLoadMore(){
        super("Load More ...");
        this.setGraphic(new ImageView(fileImage));
    }

    public String getPath() {
        return null;
    }
}
