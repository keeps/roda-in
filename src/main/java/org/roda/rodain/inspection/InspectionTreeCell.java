package org.roda.rodain.inspection;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class InspectionTreeCell extends TreeCell<String> {
  /**
   * Creates a new InspectionTreeCell
   */
  public InspectionTreeCell() {
  }

  /**
   * Updates the item with the text and icon.
   * @param item The value of the item
   * @param empty Flag to check whether the item is empty or not
   */
  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      setText(item);
      TreeItem treeItem = getTreeItem();
      if (treeItem != null) {
        setGraphic(treeItem.getGraphic());
      }
    }
  }
}
