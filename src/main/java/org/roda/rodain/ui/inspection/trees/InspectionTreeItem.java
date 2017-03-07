package org.roda.rodain.ui.inspection.trees;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public interface InspectionTreeItem {
  /**
   * @return The path of the inspection tree item.
   */
  Path getPath();

  /**
   * @return The parent directory of the inspection tree item.
   */
  TreeItem getParentDir();

  /**
   * Sets the parent directory
   * 
   * @param t
   *          the new parent directory
   */
  void setParentDir(TreeItem t);
}
