package source.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import source.ui.items.*;

/**
 * Created by adrapereira on 12-10-2015.
 */
public class SourceTreeCell extends TreeCell<String> {
    private ContextMenu addMenu = new ContextMenu();

    public SourceTreeCell(){
        MenuItem ignoreMenu = new MenuItem("Set Ignored");
        addMenu.getItems().add(ignoreMenu);
        ignoreMenu.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TreeItem<String> treeItem = getTreeItem();
                SourceTreeItem sti = (SourceTreeItem) treeItem;
                sti.ignore();
                updateItem(treeItem.getValue(), false);
            }
        });

        MenuItem mapMenu = new MenuItem("Set Mapped");
        addMenu.getItems().add(mapMenu);
        mapMenu.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TreeItem<String> treeItem = getTreeItem();
                SourceTreeItem sti = (SourceTreeItem) treeItem;
                sti.map();
                updateItem(treeItem.getValue(), false);
            }
        });
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setStyle("-fx-text-fill:orange;");

        if (!empty) {
            HBox hbox = new HBox();
            Label lab = new Label(item);
            lab.setStyle("-fx-text-fill: black");
            Image icon = null;

            //Get the correct item
            TreeItem<String> treeItem = getTreeItem();
            SourceTreeItem sti = (SourceTreeItem) treeItem;

            //if the item is a file and we're not showing files, clear the cell and return
            if(sti instanceof SourceTreeFile && !FileExplorerPane.showFiles){
                empty();
                return;
            }
            //if the item is ignored and we're not showing ignored items, clear the cell and return
            if(sti.getState() == SourceTreeItemState.IGNORED )
                if(!FileExplorerPane.showIgnored){
                    empty();
                    return;
                }else lab.setStyle("-fx-text-fill: red");

            //if the item is mapped and we're not showing mapped items, clear the cell and return
            if(sti.getState() == SourceTreeItemState.MAPPED)
                if(!FileExplorerPane.showMapped){
                    empty();
                    return;
                }else lab.setStyle("-fx-text-fill: blue");


            if(treeItem instanceof SourceTreeDirectory){
                if(treeItem.isExpanded())
                    icon = SourceTreeDirectory.folderExpandImage;
                else icon = SourceTreeDirectory.folderCollapseImage;
            }else{
                if(treeItem instanceof SourceTreeFile)
                    icon = SourceTreeFile.fileImage;
                else if(treeItem instanceof SourceTreeLoadMore) icon = SourceTreeLoadMore.fileImage;
            }
            hbox.getChildren().addAll(new ImageView(icon), lab);
            setGraphic(hbox);

            setContextMenu(addMenu);
        }else empty();
    }

    private void empty(){
        setText(null);
        setGraphic(null);
        setDisclosureNode(new HBox());
    }

}
