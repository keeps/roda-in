package org.roda.rodain.schema.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class SchemaTreeCell extends TreeCell<String> {
    public SchemaTreeCell(){
        getStyleClass().add("schemaNode");
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.BOTTOM_LEFT);
            Label lab = new Label(item);
            lab.getStyleClass().add("cellText");
            Image icon = null;

            //Get the correct item
            TreeItem<String> treeItem = getTreeItem();
            if(treeItem instanceof SchemaNode){
                icon = ((SchemaNode) treeItem).getImage();
                int begin = item.lastIndexOf("(");
                int end = item.lastIndexOf("items)");
                if(end > begin){
                    //Example: from "Test ABC (145 items)" we get title="Test ABC" and numItems="145 "
                    String title = item.substring(0, begin -1);
                    String numItems = item.substring(begin + 1, end);
                    lab = new Label(title);
                    setText(numItems + "items");
                }
            }else{
                if(treeItem instanceof SipPreviewNode){
                    SipPreviewNode sipNode = (SipPreviewNode) treeItem;
                    icon = sipNode.getIcon();
                    if(sipNode.isMetaModified() || sipNode.isContentModified()) {
                        setText("*");
                    }else setText("");
                }
            }
            hbox.getChildren().addAll(new ImageView(icon), lab);
            setGraphic(hbox);
        }
    }
}
