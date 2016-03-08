package org.roda.rodain.inspection;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.roda.rodain.rules.TreeNode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SipContentDirectory extends TreeItem<Object> implements InspectionTreeItem {
  public static final Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder.png"));
  public static final Image folderExpandImage = new Image(
    ClassLoader.getSystemResourceAsStream("icons/folder-open.png"));
  private static final Comparator comparator = createComparator();
  // this stores the full path to the file or directory
  private Path fullPath;
  private TreeNode treeNode;
  private TreeItem parent;

  /**
   * Creates a new TreeItem, representing a directory.
   *
   * @param treeNode
   *          The TreeNode that will be associated to the item.
   * @param parent
   *          The item's parent.
   */
  public SipContentDirectory(TreeNode treeNode, TreeItem parent) {
    super(treeNode.getPath());
    this.treeNode = treeNode;
    this.fullPath = treeNode.getPath();
    this.parent = parent;
    this.setGraphic(new ImageView(folderCollapseImage));

    Path name = fullPath.getFileName();
    if (name != null) {
      this.setValue(name.toString());
    } else {
      this.setValue(fullPath.toString());
    }

    this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
      @Override
      public void handle(TreeModificationEvent<Object> e) {
        Object sourceObject = e.getSource();
        if (sourceObject instanceof SipContentDirectory) {
          SipContentDirectory source = (SipContentDirectory) sourceObject;
          if (source.isExpanded()) {
            ImageView iv = (ImageView) source.getGraphic();
            iv.setImage(folderExpandImage);
          }
        }
      }
    });

    this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
      @Override
      public void handle(TreeModificationEvent<Object> e) {
        Object sourceObject = e.getSource();
        if (sourceObject instanceof SipContentDirectory) {
          SipContentDirectory source = (SipContentDirectory) sourceObject;
          if (!source.isExpanded()) {
            ImageView iv = (ImageView) source.getGraphic();
            iv.setImage(folderCollapseImage);
          }
        }
      }
    });
  }

  /**
   * @return The TreeNode of this item..
   */
  public TreeNode getTreeNode() {
    return treeNode;
  }

  /**
   * @return The parent item of this item.
   */
  @Override
  public TreeItem getParentDir() {
    return parent;
  }

  /**
   * Skips the directory.
   * <p/>
   * <p>
   * This method removes this node from its parent and adds all its children to
   * the parent, effectively skipping the directory.
   * </p>
   */
  public void skip() {
    SipContentDirectory par = (SipContentDirectory) this.parent;
    TreeNode parentTreeNode = par.getTreeNode();
    parentTreeNode.remove(treeNode.getPath()); // remove this treeNode from the
    // parent
    parentTreeNode.addAll(treeNode.getAllFiles()); // add this treeNode's
    // children to this node's
    // parent
    par.sortChildren();
  }

  /**
   * Flattens the directory.
   * <p/>
   * <p>
   * This method flattens the item's file tree, i.e., moves all it's child nodes
   * to one level.
   * </p>
   */
  public void flatten() {
    treeNode.flatten();
    getChildren().clear();
    for (String path : treeNode.getKeys()) {
      SipContentFile file = new SipContentFile(Paths.get(path), this);
      getChildren().add(file);
    }
    sortChildren();
  }

  /**
   * Sorts the item's children.
   * <p/>
   * <p>
   * The comparator used by this method forces the directories to appear before
   * the files. Between items of the same class the sorting is done comparing
   * the items' values.
   * </p>
   */
  public void sortChildren() {
    ArrayList<TreeItem<Object>> aux = new ArrayList<>(getChildren());
    Collections.sort(aux, comparator);
    getChildren().setAll(aux);

    for (TreeItem ti : getChildren()) {
      if (ti instanceof SipContentDirectory)
        ((SipContentDirectory) ti).sortChildren();
    }
  }

  private static Comparator createComparator() {
    return new Comparator<TreeItem>() {
      @Override
      public int compare(TreeItem o1, TreeItem o2) {
        if (o1.getClass() == o2.getClass()) { // sort items of the same class by
          // value
          String s1 = (String) o1.getValue();
          String s2 = (String) o2.getValue();
          return s1.compareToIgnoreCase(s2);
        }
        // directories must appear first
        if (o1 instanceof SipContentDirectory)
          return -1;
        return 1;
      }
    };
  }

  /**
   * @return The path of this item.
   */
  @Override
  public Path getPath() {
    return this.fullPath;
  }
}
