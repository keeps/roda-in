package org.roda.rodain.inspection;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 21-09-2015.
 */
public class ContentClickedEventHandler implements EventHandler<MouseEvent> {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ContentClickedEventHandler.class.getName());
  private static String OS = System.getProperty("os.name").toLowerCase();
  private TreeView<Object> treeView;
  private InspectionPane ipane;

  /**
   * Creates an EventHandler to handle mouse events on the items of the SIP's content.
   *
   * @param tree The TreeView with the SIP's content
   * @param pane A reference to the InspectionPane
   */
  public ContentClickedEventHandler(TreeView<Object> tree, InspectionPane pane) {
    this.treeView = tree;
    ipane = pane;
  }

  /**
   * Enables or disables the content buttons when an item is selected.
   * Attempts to open the file when an item is double-clicked.
   *
   * @param mouseEvent The mouse event.
   */
  @Override
  public void handle(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 1) {
      TreeItem<Object> item = treeView.getSelectionModel().getSelectedItem();
      if (item instanceof SipContentDirectory) {
        ipane.setStateContentButtons(false);
      } else if (item instanceof SipContentFile) {
        ipane.setStateContentButtons(true);
      }
    } else if (mouseEvent.getClickCount() == 2) {
      Object source = treeView.getSelectionModel().getSelectedItem();
      if (source instanceof SipContentFile) {
        SipContentFile sipFile = (SipContentFile) source;
        String fileName = sipFile.getPath().toString();
        // Different commands for different operating systems
        if (isWindows()) {
          executeCommand("explorer", fileName);
        } else if (isMac()) {
          executeCommand("open", fileName);
        } else if (isUnix()) {
          boolean result = executeCommand("xdg-open", fileName);
          if (!result) {
            executeCommand("gnome-open", fileName);
          }
        }
      }
    }
  }

  private boolean executeCommand(String command, String file) {
    try {
      ProcessBuilder pb = new ProcessBuilder(command, file);
      pb.start();
    } catch (IOException e) {
      log.info("Error opening file from SIP content", e);
      return false;
    }
    return true;
  }

  private static boolean isWindows() {
    return OS.contains("win");
  }

  private static boolean isMac() {
    return OS.contains("mac");
  }

  private static boolean isUnix() {
    return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
  }
}
