package org.roda.rodain.ui.inspection.trees;

import java.nio.file.Path;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.shallowSipManager.UriCreator;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SipContentFile extends TreeItem<Object> implements InspectionTreeItem {
  public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream(Constants.RSC_ICON_FILE));
  public static final Image file_export = new Image(
    ClassLoader.getSystemResourceAsStream(Constants.RSC_ICON_FILE_EXPORT));
  private Path fullPath;
  private TreeItem parent;

  /**
   * Creates a new TreeItem, representing a file.
   *
   * @param file
   *          The Path that will be associated to the item.
   * @param parent
   *          The item's parent.
   */
  public SipContentFile(Path file, TreeItem parent) {
    super(file.toString());
    this.fullPath = file;
    this.parent = parent;
    final Image icon;
    if (UriCreator.partOfConfiguration(file)) {
      icon = file_export;
    } else {
      icon = fileImage;
    }
    this.setGraphic(new ImageView(icon));

    final Path name = fullPath.getFileName();
    if (name != null) {
      this.setValue(name.toString());
    } else {
      this.setValue(fullPath.toString());
    }
  }

  /**
   * @return This item's parent.
   */
  @Override
  public TreeItem getParentDir() {
    return parent;
  }

  /**
   * Sets the parent directory
   *
   * @param t
   *          the new parent directory
   */
  @Override
  public void setParentDir(TreeItem t) {
    parent = t;
  }

  /**
   * @return The path of this item.
   */
  @Override
  public Path getPath() {
    return this.fullPath;
  }
}
