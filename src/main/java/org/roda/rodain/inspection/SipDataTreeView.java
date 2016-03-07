package org.roda.rodain.inspection;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

import org.roda.rodain.rules.TreeNode;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-03-2016.
 */
public class SipDataTreeView extends TreeView {

  public SipDataTreeView() {
    setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
      @Override
      public TreeCell<String> call(TreeView<String> p) {
        InspectionTreeCell cell = new InspectionTreeCell();
        setDragnDropEvent(cell);
        return cell;
      }
    });
    setOnMouseClicked(new ContentClickedEventHandler(this));
    setShowRoot(false);
  }

  private void setDragnDropEvent(InspectionTreeCell cell) {
    setOnDragDetected(cell);
    setOnDragOver(cell);
    setOnDragEntered(cell);
    setOnDragExited(cell);
    setOnDragDropped(cell);
  }

  private void setOnDragDetected(InspectionTreeCell cell) {
    cell.setOnDragDetected(event -> {
      TreeItem item = cell.getTreeItem();
      if (item != null) {
        Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        String s = "item - " + item.getValue();
        content.putString(s);
        db.setContent(content);
        event.consume();
      }
    });
  }

  private void setOnDragOver(final InspectionTreeCell cell) {
    // on a Target
    cell.setOnDragOver(event -> {
      TreeItem treeItem = cell.getTreeItem();
      if (treeItem == null || treeItem instanceof SipContentDirectory) {
        if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
          event.acceptTransferModes(TransferMode.MOVE);
        }
      }
      event.consume();
    });
  }

  private void setOnDragEntered(final InspectionTreeCell cell) {
    // on a Target
    cell.setOnDragEntered(event -> {
      TreeItem<String> treeItem = cell.getTreeItem();
      if (treeItem instanceof InspectionTreeItem) {
        InspectionTreeItem item = (InspectionTreeItem) cell.getTreeItem();
        if (item != null && event.getGestureSource() != cell && event.getDragboard().hasString()) {
          cell.getStyleClass().add("schemaNodeHovered");
        }
      }
      event.consume();
    });
  }

  private void setOnDragExited(final InspectionTreeCell cell) {
    // on a Target
    cell.setOnDragExited(event -> {
      cell.getStyleClass().remove("schemaNodeHovered");
      cell.updateItem(cell.getItem(), false);
      event.consume();
    });
  }

  private void setOnDragDropped(final InspectionTreeCell cell) {
    // on a Target
    cell.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasString()) {
        // edit the classification scheme
        if (db.getString().startsWith("item")) {
          InspectionTreeCell targetCell = (InspectionTreeCell) event.getGestureTarget();
          TreeItem targetRaw = targetCell.getTreeItem();
          // if(targetRaw == null){
          // // Dropped in the root of the tree
          // getRoot().getChildren().add(source);
          // }
          if (targetRaw instanceof SipContentDirectory) {
            SipContentDirectory target = (SipContentDirectory) targetRaw;
            InspectionTreeCell sourceCell = (InspectionTreeCell) event.getGestureSource();
            TreeItem source = sourceCell.getTreeItem();
            TreeItem sourceParent = source.getParent();

            // remove from the parent
            sourceParent.getChildren().remove(source);
            TreeNode targetTreeNode = target.getTreeNode();
            if (target != null) {
              target.getChildren().add(source);
            } else

              success = true;
          } else {
            success = false;
          }
        }
      }
      event.setDropCompleted(success);
      event.consume();
    });
  }
}
