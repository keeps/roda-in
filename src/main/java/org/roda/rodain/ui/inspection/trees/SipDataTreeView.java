package org.roda.rodain.ui.inspection.trees;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.utils.AutoscrollTreeView;

import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-03-2016.
 */
public class SipDataTreeView extends AutoscrollTreeView {

  public SipDataTreeView() {
    setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
      @Override
      public TreeCell<String> call(TreeView<String> p) {
        InspectionTreeCell cell = new InspectionTreeCell();
        setDragnDropEvent(cell);
        return cell;
      }
    });
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    
    EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
      if(getSelectionModel().getSelectedItem()!=null && getSelectionModel().getSelectedItem().getClass()==SipContentRepresentation.class){
        SipContentRepresentation scr = (SipContentRepresentation)getSelectionModel().getSelectedItem();
        RodaInApplication.getInspectionPane().setCurrentRepresentation(scr);
        RodaInApplication.getInspectionPane().showEditRepresentationTypeButton(scr);
      }else{
        RodaInApplication.getInspectionPane().setCurrentRepresentation(null);
        RodaInApplication.getInspectionPane().hideEditRepresentationTypeButton();
      }
    };

    setOnMouseClicked(mouseEventHandle);

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
      if (item != null && !(item instanceof SipContentRepresentation)) {
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
      Dragboard db = event.getDragboard();
      TreeItem treeItem = cell.getTreeItem();
      if (treeItem instanceof SipContentDirectory || treeItem instanceof SipContentRepresentation) {
        if (event.getGestureSource() != cell && db.hasString()) {
          event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
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
          cell.getStyleClass().add(Constants.CSS_SCHEMANODEHOVERED);
        }
      }
      event.consume();
    });
  }

  private void setOnDragExited(final InspectionTreeCell cell) {
    // on a Target
    cell.setOnDragExited(event -> {
      cell.getStyleClass().remove(Constants.CSS_SCHEMANODEHOVERED);
      cell.updateItem(cell.getItem(), false);
      event.consume();
    });
  }

  private void setOnDragDropped(final InspectionTreeCell cell) {
    // on a Target
    cell.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        success = true;
        InspectionTreeCell targetCell = (InspectionTreeCell) event.getGestureTarget();
        TreeItem targetRaw = targetCell.getTreeItem();
        Set<Path> paths = new HashSet<>();
        for (File file : db.getFiles()) {
          paths.add(file.toPath());
        }
        RodaInApplication.getInspectionPane().addDataToSIP(targetRaw, paths);
      } else {
        if (db.hasString()) {
          if (db.getString().startsWith("item")) {
            move(event);
            success = true;
          } else if (db.getString().startsWith("source")) {
            InspectionTreeCell targetCell = (InspectionTreeCell) event.getGestureTarget();
            RodaInApplication.getInspectionPane().addDataToSIP(targetCell.getTreeItem());
            success = true;
          }
        } else {
          success = false;
        }
      }
      event.setDropCompleted(success);
      event.consume();
    });

  }

  private void move(DragEvent event) {
    InspectionTreeCell targetCell = (InspectionTreeCell) event.getGestureTarget();
    TreeItem targetRaw = targetCell.getTreeItem();
    List<InspectionTreeItem> selectedItems = RodaInApplication.getInspectionPane().getDataSelectedItems();
    for (InspectionTreeItem source : selectedItems) {
      TreeItem sourceRaw = (TreeItem) source;
      TreeItem sourceParent = sourceRaw.getParent();

      // If the target item is a descendant of the source item, they would
      // both disappear since there would be no remaining connection to
      // the rest of the tree
      if (checkTargetIsDescendant(sourceRaw, targetRaw)) {
        return;
      }

      // Remove the path from the parent
      if (sourceParent instanceof SipContentDirectory) {
        SipContentDirectory castedSourceParent = (SipContentDirectory) sourceParent;
        castedSourceParent.getTreeNode().remove(source.getPath());
      }
      if (sourceParent instanceof SipContentRepresentation) {
        SipContentRepresentation castedSourceParent = (SipContentRepresentation) sourceParent;
        castedSourceParent.getRepresentation().remove(source.getPath());
      }
      sourceParent.getChildren().remove(source);

      // Add the path to the target
      if (targetRaw instanceof SipContentDirectory) {
        SipContentDirectory target = (SipContentDirectory) targetRaw;
        if (source instanceof SipContentDirectory)
          target.getTreeNode().add(((SipContentDirectory) source).getTreeNode());
        if (source instanceof SipContentFile)
          target.getTreeNode().add(source.getPath());
      }
      if (targetRaw instanceof SipContentRepresentation) {
        SipContentRepresentation scr = (SipContentRepresentation) targetRaw;
        if (source instanceof SipContentDirectory)
          scr.getRepresentation().addFile(((SipContentDirectory) source).getTreeNode());
        if (source instanceof SipContentFile)
          scr.getRepresentation().addFile(source.getPath());
      }
      source.setParentDir(targetRaw);

      targetRaw.getChildren().add(source);
    }
  }

  private boolean checkTargetIsDescendant(TreeItem source, TreeItem target) {
    TreeItem aux = target.getParent();
    boolean isChild = false;
    while (aux != null) {
      if (aux == source) {
        isChild = true;
        break;
      }
      aux = aux.getParent();
    }
    return isChild;
  }
}
