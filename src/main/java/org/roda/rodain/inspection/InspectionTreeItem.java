package org.roda.rodain.inspection;

import java.nio.file.Path;

import javafx.scene.control.TreeItem;

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
}
