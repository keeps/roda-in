package org.roda.rodain.inspection;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SipContentFile extends TreeItem<Object> implements InspectionTreeItem {
  public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));
  private Path fullPath;
  private TreeItem parent;

  /**
   * Creates a new TreeItem, representing a file.
   *
   * @param file   The Path that will be associated to the item.
   * @param parent The item's parent.
   */
  public SipContentFile(Path file, TreeItem parent) {
    super(file.toString());
    this.fullPath = file;
    this.parent = parent;
    this.setGraphic(new ImageView(fileImage));

    Path name = fullPath.getFileName();
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
   * @return The path of this item.
   */
  @Override
  public Path getPath() {
    return this.fullPath;
  }
}
