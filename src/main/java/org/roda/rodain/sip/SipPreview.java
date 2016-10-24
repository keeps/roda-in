package org.roda.rodain.sip;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda_project.commons_ip.model.IPContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview extends DescriptionObject implements Observer {
  private static final Logger LOGGER = LoggerFactory.getLogger(SipPreview.class.getName());

  private Set<SipRepresentation> representations;
  private Set<TreeNode> documentation;
  private IPContentType contentType;
  private boolean contentModified = false;
  private boolean removed = false;

  /**
   * Creates a new SipPreview object.
   *
   * @param name
   *          The name of the SIP
   * @param representations
   *          The set of representations to be added to the SIP
   * @param metadata
   *          The metadata of the SIP
   */
  public SipPreview(String name, Set<SipRepresentation> representations, DescObjMetadata metadata) {
    super(new DescObjMetadata(MetadataOptions.TEMPLATE, "ead2002", "ead", "2002"));
    this.representations = representations;
    documentation = new HashSet<>();
    setTitle(name);
    List<DescObjMetadata> tempList = new ArrayList<>();
    if (metadata != null)
      tempList.add(metadata);
    setMetadata(tempList);
    if(metadata!=null){
      try {
        String metadataItemLevel = AppProperties.getConfig("metadata." + metadata.getTemplateType() + ".itemLevel");
        setDescriptionlevel(metadataItemLevel);
      } catch (Throwable t) {
        LOGGER.error(t.getMessage(), t);
      }
    }
    setId("ID" + UUID.randomUUID().toString());
    contentType = new IPContentType(IPContentType.IPContentTypeEnum.MIXED);

    // set paths as mapped
    for (SipRepresentation sr : representations) {
      for (TreeNode tn : sr.getFiles()) {
        PathCollection.addPath(tn.getPath().toString(), SourceTreeItemState.MAPPED);
      }
    }
  }

  public void setContentType(IPContentType contentType) {
    this.contentType = contentType;
  }

  public IPContentType getContentType() {
    return contentType;
  }

  /**
   * @return All the representations of the SIP.
   */
  public Set<SipRepresentation> getRepresentations() {
    return representations;
  }

  public Set<TreeNode> getDocumentation() {
    return documentation;
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
    for (SipRepresentation sr : representations) {
      for (TreeNode tn : sr.getFiles()) {
        ignored.addAll(tn.ignoreContent(paths));
        if (paths.contains(tn.getPath()))
          toRemove.add(tn);
      }
      sr.getFiles().removeAll(toRemove);
    }
    PathCollection.addPaths(ignored, SourceTreeItemState.NORMAL);
  }

  public void addRepresentation(SipRepresentation sipRep) {
    representations.add(sipRep);
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
    for (SipRepresentation sr : representations) {
      for (TreeNode tn : sr.getFiles()) {
        paths += tn.getFullTreePaths().size();
      }
    }
    // remove
    int update = 0;
    for (SipRepresentation sr : representations) {
      for (TreeNode tn : sr.getFiles()) {
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
    }
    removed = true;
    setChanged();
    notifyObservers();
  }

  public void removeFromRule() {
    setChanged();
    notifyObservers("Remove from rule");
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

  public void removeRepresentation(SipRepresentation representation) {
    Set<Path> paths = new HashSet<>();
    for (TreeNode tn : representation.getFiles()) {
      paths.addAll(tn.getFullTreePathsAsPaths());
    }
    ignoreContent(paths);
    representations.remove(representation);
  }

  public void addDocumentation(Set<TreeNode> docs) {
    documentation.addAll(docs);
  }

  public void removeDocumentation(Set<Path> paths) {
    Set<String> ignored = new HashSet<>();
    Set<TreeNode> toRemove = new HashSet<>();
    for (TreeNode tn : documentation) {
      ignored.addAll(tn.ignoreContent(paths));
      if (paths.contains(tn.getPath()))
        toRemove.add(tn);
    }
    documentation.removeAll(toRemove);
  }
}
