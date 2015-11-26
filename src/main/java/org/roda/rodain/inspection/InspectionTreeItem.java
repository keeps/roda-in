package org.roda.rodain.inspection;

import java.nio.file.Path;

import javafx.scene.control.TreeItem;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public interface InspectionTreeItem {
  Path getPath();

  TreeItem getParentDir();
}
