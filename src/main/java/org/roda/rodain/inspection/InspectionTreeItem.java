package org.roda.rodain.inspection;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public interface InspectionTreeItem {
  Path getPath();

  TreeItem getParentDir();
}
