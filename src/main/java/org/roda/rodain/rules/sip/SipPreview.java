package org.roda.rodain.rules.sip;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview extends Observable implements Observer {
  private String id;
  private String name;
  private SipMetadata metadata;
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
  public SipPreview(String name, Set<TreeNode> files, SipMetadata metadata) {
    this.name = name;
    this.files = files;
    this.metadata = metadata;
    // metadata = new SipMetadata(metaType, metadataPath, templateType);
    id = UUID.randomUUID().toString();

    // set paths as mapped
    for (TreeNode tn : files) {
      PathCollection.addPath(tn.getPath().toString(), SourceTreeItemState.MAPPED);
    }
  }

  /**
   * @return The name of the SIP.
   */
  public String getName() {
    return name;
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
   * @return The metadata content of the SIP.
   * @see SipMetadata#getMetadataContent()
   */
  public String getMetadataContent() {
    String content = metadata.getMetadataContent();
    if (content != null) {
      //TODO configurable tags...
      Template tmpl = Mustache.compiler().compile(content);
      Map<String, String> data = new HashMap<>();
      data.put("title", getName());
      data.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
      content = tmpl.execute(data);
      //we need to clean the '\r' character in windows,
      // otherwise the strings are different even if no modification has been made
      content = content.replace("\r", "");
    }
    return content;
  }

  /**
   * @return The type of the metadata.
   */
  public String getTemplateType() {
    return metadata.getTemplateType();
  }

  /**
   * Updates the metadata content of the SIP.
   *
   * @param meta The new metadata content.
   * @see SipMetadata#update(String)
   */
  public void updateMetadata(String meta) {
    if(!meta.equals(metadata.getMetadataContent())) {
      metadata.update(meta);
      setChanged();
      notifyObservers();
    }
  }

  /**
   * Removes from the SIP's content the set of paths received as parameter.
   *
   * @param paths The set of paths to be removed
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
   * @return The version of the SIP's metadata
   */
  public String getMetadataVersion() {
    return metadata.getVersion();
  }

  /**
   * @return True if the metadata has been modified, false otherwise.
   * @see SipMetadata#isModified()
   */
  public boolean isMetadataModified() {
    return metadata.isModified();
  }

  /**
   * @return True if the content has been modified (nodes removed, flattened or
   * skipped), false otherwise.
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
   * @return The id of the SIP.
   */
  public String getId() {
    return id;
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
   * @param o   The observable object that is modified.
   * @param arg The arguments sent by the observable object.
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
