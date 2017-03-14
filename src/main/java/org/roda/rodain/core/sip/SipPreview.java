package org.roda.rodain.core.sip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.Sip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview extends Sip implements Observer {
  private static final Logger LOGGER = LoggerFactory.getLogger(SipPreview.class.getName());
  private Set<SipRepresentation> representations;
  private Set<TreeNode> documentation;
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
  public SipPreview(String name, Set<SipRepresentation> representations, DescriptiveMetadata metadata) {
    super(DescriptiveMetadata.buildDefaultDescObjMetadata());
    this.representations = representations;
    documentation = new HashSet<>();
    setTitle(name);
    List<DescriptiveMetadata> tempList = new ArrayList<>();
    if (metadata != null)
      tempList.add(metadata);
    setMetadata(tempList);
    if (metadata != null) {
      try {
        String metadataItemLevel = ConfigurationManager
          .getMetadataConfig(metadata.getTemplateType() + Constants.CONF_K_SUFIX_ITEM_LEVEL);
        setDescriptionlevel(metadataItemLevel);
      } catch (Throwable t) {
        LOGGER.error(t.getMessage(), t);
      }
    }

    // set paths as mapped
    for (SipRepresentation sr : representations) {
      for (TreeNode tn : sr.getFiles()) {
        PathCollection.addPath(tn.getPath(), PathState.MAPPED);
      }
    }
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

    PathCollection.addPaths(ignored, PathState.NORMAL);
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
          PathCollection.addPath(Paths.get(path), PathState.NORMAL);
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
    notifyObservers(Constants.EVENT_REMOVE_FROM_RULE);
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
    if (docs != null) {
      Iterator<TreeNode> it = docs.iterator();
      while (it.hasNext()) {
        TreeNode t = it.next();
        if (!alreadyExistsDocumentation(t)) {
          documentation.add(t);
        }
      }
    }
    // documentation.addAll(docs);
  }

  private boolean alreadyExistsDocumentation(TreeNode t) {
    boolean exist = false;
    if (documentation != null) {
      Iterator<TreeNode> it = documentation.iterator();
      while (it.hasNext()) {
        if (it.next().getPath().toString().equals(t.getPath().toString())) {
          exist = true;
        }
      }
    }
    return exist;
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
