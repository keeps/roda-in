package org.roda.rodain.inspection;

import javafx.scene.control.TreeCell;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class InspectionTreeCell extends TreeCell<String> {
    public InspectionTreeCell(){
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setStyle("-fx-text-fill:black;");

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);
            setGraphic(getTreeItem().getGraphic());
        }
    }
}
