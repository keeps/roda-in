package org.roda.rodain.rules.sip;

import java.nio.file.Path;
import java.util.*;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview extends DescriptionObject implements Observer {
  private Set<TreeNode> files;
  private boolean contentModified = false;
  private boolean removed = false;

  /**
   * Creates a new SipPreview object.
   *
   * @param name
   *          The name of the SIP
   * @param files
   *          The set of files to be added to the SIP's content
   * @param metadata
   *          The metadata of the SIP
   */
  public SipPreview(String name, Set<TreeNode> files, DescObjMetadata metadata) {
    this.files = files;
    setTitle(name);
    List<DescObjMetadata> tempList = new ArrayList<>();
    tempList.add(metadata);
    setMetadata(tempList);
    setDescriptionlevel("item");
    // metadata = new SipMetadata(metaType, metadataPath, templateType);
    setId(UUID.randomUUID().toString());

    // set paths as mapped
    for (TreeNode tn : files) {
      PathCollection.addPath(tn.getPath().toString(), SourceTreeItemState.MAPPED);
    }
  }

  /**
   * @return The paths of the files of the SIP.
   */
  public Set<TreeNode> getFiles() {
    return files;
  }

  /**
   * Sets the TreeNodes of the SIP
   * 
   * @param newFiles
   *          A Set with the TreeNodes
   */
  public void setFiles(Set<TreeNode> newFiles) {
    contentModified = true;
    files = newFiles;
  }

  /**
   * Removes from the SIP's content the set of paths received as parameter.
   *
   * @param paths
   *          The set of paths to be removed
   */
  public void ignoreContent(Set<Path> paths) {
    Set<String> ignored = new HashSet<>();
    Set<TreeNode> toRemove = new HashSet<>();
    for (TreeNode tn : files) {
      ignored.addAll(tn.ignoreContent(paths));
      if (paths.contains(tn.getPath()))
        toRemove.add(tn);
    }
    files.removeAll(toRemove);
    PathCollection.addPaths(ignored, SourceTreeItemState.NORMAL);
  }

  /**
   * @return True if the content has been modified (nodes removed, flattened or
   *         skipped), false otherwise.
   */
  public boolean isContentModified() {
    return contentModified;
  }

  /**
   * @return True if the SIP has been removed by the user, false otherwise.
   */
  public boolean isRemoved() {
    return removed;
  }

  /**
   * Sets the removed state of the SIP as true.
   */
  public void setRemoved() {
    removed = true;
  }

  /**
   * Removes the SIP, setting its content as NORMAL.
   */
  public void removeSIP() {
    float paths = 0, removedPaths = 0;
    // prepare
    for (TreeNode tn : getFiles()) {
      paths += tn.getFullTreePaths().size();
    }
    // remove
    int update = 0;
    for (TreeNode tn : getFiles()) {
      for (String path : tn.getFullTreePaths()) {
        PathCollection.addPath(path, SourceTreeItemState.NORMAL);
        removedPaths++;
        update++;
        if (update >= 10) {
          update = 0;
          setChanged();
          notifyObservers(removedPaths / paths);
        }
      }
    }
    removed = true;
    setChanged();
    notifyObservers();
  }

  /**
   * Sets the content modified state as true if it receives a notification from
   * any TreeNode in the files Set.
   *
   * @param o
   *          The observable object that is modified.
   * @param arg
   *          The arguments sent by the observable object.
   */
  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof TreeNode) {
      contentModified = true;
      setChanged();
      notifyObservers();
    }
  }
}
