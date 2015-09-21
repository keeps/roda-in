package SourceUI.Items;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceTreeLoadMore extends TreeItem<String> {
    public static Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("list-add.png"));

    public SourceTreeLoadMore(){
        super("Load More ...");
        this.setGraphic(new ImageView(fileImage));
    }
}
