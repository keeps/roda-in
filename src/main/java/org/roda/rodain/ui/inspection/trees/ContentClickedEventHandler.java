package org.roda.rodain.ui.inspection.trees;

import org.roda.rodain.core.utils.OpenPathInExplorer;

import javafx.event.EventHandler;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 21-09-2015.
 */
public class ContentClickedEventHandler implements EventHandler<MouseEvent> {
  private TreeView<Object> treeView;

  /**
   * Creates an EventHandler to handle mouse events on the items of the SIP's
   * content.
   *
   * @param tree
   *          The TreeView with the SIP's content
   */
  public ContentClickedEventHandler(TreeView<Object> tree) {
    this.treeView = tree;
  }

  /**
   * Enables or disables the content buttons when an item is selected. Attempts
   * to open the file when an item is double-clicked.
   *
   * @param mouseEvent
   *          The mouse event.
   */
  @Override
  public void handle(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 2) {
      Object source = treeView.getSelectionModel().getSelectedItem();
      if (source instanceof SipContentFile) {
        SipContentFile sipFile = (SipContentFile) source;
        OpenPathInExplorer.open(sipFile.getPath());
      }
    }
  }
}
