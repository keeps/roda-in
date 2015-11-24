package org.roda.rodain.source.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.roda.rodain.source.ui.items.*;

import java.util.Properties;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class SourceTreeCell extends TreeCell<String> {
    private ContextMenu menu = new ContextMenu();
    private static Properties style;

    public SourceTreeCell(){
        MenuItem removeIgnore = new MenuItem("Remove Ignore");
        menu.getItems().add(removeIgnore);
        removeIgnore.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TreeItem<String> treeItem = getTreeItem();
                SourceTreeItem sti = (SourceTreeItem) treeItem;
                sti.removeIgnore();
                //force update
                String value = treeItem.getValue();
                treeItem.setValue("");
                treeItem.setValue(value);
            }
        });
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            HBox hbox = new HBox(5);
            Label lab = new Label(item);
            lab.getStyleClass().add("cellText");
            lab.setId("");
            Image icon = null;

            //Remove the context menu
            setContextMenu(null);

            //Get the correct item
            TreeItem<String> treeItem = getTreeItem();
            SourceTreeItem sti = (SourceTreeItem) treeItem;

            //if the item is a file and we're not showing files, clear the cell and return
            if(sti instanceof SourceTreeFile && !FileExplorerPane.isShowFiles()){
                empty();
                return;
            }
            if(sti.getState() == SourceTreeItemState.IGNORED ) {
                //the context menu only makes sense if the item is ignored
                setContextMenu(menu);
                lab.setId("ignored");
            }
            if(sti.getState() == SourceTreeItemState.MAPPED) {
                lab.setStyle(style.getProperty("source.cell.mapped"));
            }


            if(treeItem instanceof SourceTreeDirectory){
                if(treeItem.isExpanded())
                    icon = SourceTreeDirectory.folderExpandImage;
                else icon = SourceTreeDirectory.folderCollapseImage;
            }else{
                if(treeItem instanceof SourceTreeFile)
                    icon = SourceTreeFile.fileImage;
                else if(treeItem instanceof SourceTreeLoadMore)
                    icon = SourceTreeLoadMore.fileImage;
            }
            hbox.getChildren().addAll(new ImageView(icon), lab);
            setGraphic(hbox);
        }else empty();
    }

    private void empty(){
        setText(null);
        setGraphic(null);
    }

    public static void setStyleProperties(Properties style){
        SourceTreeCell.style = style;
    }
}
