package org.roda.rodain.rules.sip;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.Utils;

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

  public SipPreview(String name, Set<TreeNode> files, MetadataTypes metaType, Path metadataPath,
    TemplateType templateType) {
    this.name = name;
    this.files = files;
    metadata = new SipMetadata(metaType, metadataPath, templateType);
    id = UUID.randomUUID().toString();

    setPathsAsMapped();
  }

  private void setPathsAsMapped() {
    for (TreeNode tn : files) {
      Set<String> paths = tn.getFullTreePaths();
      for (String path : paths) {
        PathCollection.addPath(path, SourceTreeItemState.MAPPED);
      }
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
   * @return The metadata content of the SIP.
   * @see SipMetadata#getMetadataContent()
   */
  public String getMetadataContent() {
	  String content = metadata.getMetadataContent();
	  if(content!=null){
		  //TODO configurable tags...
		  content = Utils.replaceTag(content,"#title#",getName());
	      content = Utils.replaceTag(content,"#date#",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));  
	  }
    return content;
  }

  /**
   * @return The type of the metadata.
   */
  public TemplateType getTemplateType() {
    return metadata.getTemplateType();
  }

  /**
   * Updates the metadata content of the SIP.
   * 
   * @param meta
   *          The new metadata content.
   * @see SipMetadata#update(String)
   */
  public void updateMetadata(String meta) {
    metadata.update(meta);
    setChanged();
    notifyObservers();
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
   * @return True if the metadata has been modified, false otherwise.
   * @see SipMetadata#isModified()
   */
  public boolean isMetadataModified() {
    return metadata.isModified();
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
   * Notifies the observers of the SIP that it has been modified.
   */
  public void changedAndNotify() {
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
