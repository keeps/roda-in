package org.roda.rodain.ui.schema.ui;

import org.roda.rodain.core.Constants;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class SchemaTreeCell extends TreeCell<String> {
  /**
   * Creates a new SchemaTreeCell
   */
  public SchemaTreeCell() {
    super();
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    if (getStyleClass().contains(Constants.CSS_SCHEMANODEEMPTY))
      getStyleClass().remove(Constants.CSS_SCHEMANODEEMPTY);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
      // To hide the hover color in the empty nodes
      if (getStyleClass().contains(Constants.CSS_SCHEMANODE))
        getStyleClass().remove(Constants.CSS_SCHEMANODE);
      if (getStyleClass().contains(Constants.CSS_TREE_CELL))
        getStyleClass().remove(Constants.CSS_TREE_CELL);
    } else {
      if (!getStyleClass().contains(Constants.CSS_TREE_CELL))
        getStyleClass().add(Constants.CSS_TREE_CELL);

      HBox hbox = new HBox();
      hbox.setAlignment(Pos.CENTER_LEFT);
      Label lab = new Label(item);
      lab.getStyleClass().add(Constants.CSS_CELLTEXT);
      ImageView icon = null;

      // Get the correct item
      TreeItem<String> treeItem = getTreeItem();
      if (treeItem == null)
        return;

      boolean addHbox = false;
      if (treeItem instanceof SchemaNode) {
        if (!getStyleClass().contains(Constants.CSS_SCHEMANODE))
          getStyleClass().add(Constants.CSS_SCHEMANODE);
        SchemaNode itemNode = (SchemaNode) treeItem;
        icon = new ImageView(itemNode.getIcon());
        updateDObj(item);
        addHbox = true;
      } else {
        if (treeItem instanceof SipPreviewNode) {
          addHbox = true;
          SipPreviewNode sipNode = (SipPreviewNode) treeItem;
          icon = new ImageView(sipNode.getIcon());
          if (sipNode.isContentModified()) {
            setText("*");
          } else
            setText("");
        }
      }
      if (addHbox) {
        hbox.getChildren().addAll(icon, lab);
        setGraphic(hbox);
      }
    }
  }

  private void updateDObj(String title) {
    TreeItem<String> item = getTreeItem();
    if (item instanceof SchemaNode) {
      SchemaNode node = (SchemaNode) item;
      node.getDob().setTitle(title);
    }
  }
}
