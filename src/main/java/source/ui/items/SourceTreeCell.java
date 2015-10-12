package source.ui.items;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import org.slf4j.LoggerFactory;

/**
 * Created by adrapereira on 12-10-2015.
 */
public class SourceTreeCell extends TreeCell<String> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SourceTreeCell.class.getName());
    public SourceTreeCell(){}

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setStyle("-fx-text-fill:orange;");

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            log.info(getTreeItem().getClass().toString());
            HBox hbox = new HBox();
            Label lab = new Label(item);
            Image icon = null;

            //Get the correct item
            TreeItem<String> treeItem = getTreeItem();
            if(treeItem instanceof SourceTreeDirectory){
                if(treeItem.isExpanded()) icon = SourceTreeDirectory.folderExpandImage;
                else icon = SourceTreeDirectory.folderCollapseImage;
            }else{
                if(treeItem instanceof SourceTreeFile) icon = SourceTreeFile.fileImage;
                else if(treeItem instanceof SourceTreeLoadMore) icon = SourceTreeLoadMore.fileImage;
            }
            hbox.getChildren().addAll(new ImageView(icon), lab);
            setGraphic(hbox);
            setText(item);
        }
    }


}
