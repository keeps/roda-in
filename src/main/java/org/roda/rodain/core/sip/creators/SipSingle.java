package org.roda.rodain.core.sip.creators;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.rules.filters.ContentFilter;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipSingle extends SipPreviewCreator {
  /**
   * Creates a new SipPreviewCreator where there's a new SIP created with all
   * the visited paths.
   *
   * @param id
   *          The id of the SipPreviewCreator.
   * @param filters
   *          The set of content filters
   * @param metadataOption
   *          The type of metadata to be applied to each SIP
   * @param metadataPath
   *          The path of the metadata
   * @param templateType
   *          The type of the metadata template
   */
  public SipSingle(String id, Set<ContentFilter> filters, MetadataOption metadataOption, String metadataType,
    Path metadataPath, String templateType, String metadataVersion) {
    super(id, filters, metadataOption, metadataType, metadataPath, templateType, metadataVersion);
  }

  /**
   * Creates a new TreeNode and adds it to the nodes Deque if the path isn't
   * mapped or ignored.
   *
   * @param path
   *          The path of the directory.
   * @param attrs
   *          The attributes of the directory.
   */
  @Override
  public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
    if (filter(path) || cancelled)
      return;
    TreeNode newNode = new TreeNode(path);
    nodes.add(newNode);
  }

  /**
   * Adds the current directory to its parent's node. If the parent doesn't
   * exist, adds a new node to the Deque.
   *
   * @param path
   *          The path of the directory.
   */
  @Override
  public void postVisitDirectory(Path path) {
    if (filter(path) || cancelled)
      return;
    // pop the node of this directory and add it to its parent (if it exists)
    TreeNode node = nodes.removeLast();
    if (!nodes.isEmpty())
      nodes.peekLast().add(node);
    else
      files.add(node);
  }

  /**
   * Adds the visited file to its parent. If the parent doesn't exist, adds a
   * new TreeNode to the Deque.
   *
   * @param path
   *          The path of the visited file
   * @param attrs
   *          The attributes of the visited file
   */
  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
    if (filter(path) || cancelled) {
      return;
    }
    if (nodes.isEmpty())
      files.add(new TreeNode(path));
    else
      nodes.peekLast().add(path);
  }

  /**
   * Ends the tree visit, creating the SIP with all the files added during the
   * visit and notifying the observers.
   */
  @Override
  public void end() {
    if (cancelled) {
      setChanged();
      notifyObservers(Constants.EVENT_FINISHED);
      return;
    }
    // create a new Sip
    Path path = Paths.get(getStartPath());
    Set<Path> metaPath = getMetadataPath(path);

    SipRepresentation rep = new SipRepresentation(Constants.SIP_REP_FIRST);
    rep.setFiles(files);
    Set<SipRepresentation> repSet = new HashSet<>();
    repSet.add(rep);
    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), repSet, null);

    if (metadataOption == MetadataOption.TEMPLATE)
      sipPreview.getMetadata().add(new DescriptiveMetadata(metadataOption, templateType, metadataType, metadataVersion));
    else {
      for (Path p : metaPath) {
        DescriptiveMetadata dom = new DescriptiveMetadata(metadataOption, p, metadataType, metadataVersion, templateType);
        dom = Controller.updateTemplate(dom);
        sipPreview.getMetadata().add(dom);
      }

    }

    for (TreeNode tn : files) {
      tn.addObserver(sipPreview);
    }

    sips.add(sipPreview);
    sipsMap.put(sipPreview.getId(), sipPreview);
    added++;

    setChanged();
    notifyObservers(Constants.EVENT_FINISHED);
  }
}
