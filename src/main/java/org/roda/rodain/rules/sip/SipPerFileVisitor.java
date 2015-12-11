package org.roda.rodain.rules.sip;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.TreeVisitor;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPerFileVisitor extends Observable implements TreeVisitor, SipPreviewCreator {
  private static final int UPDATEFREQUENCY = 500; // in milliseconds
  private static final String METADATA_EXT = ".xml";
  // This map is returned, in full, to the SipPreviewNode when there's an update
  private Map<String, SipPreview> sipsMap;
  // This ArrayList is used to keep the SIPs ordered.
  // We need them ordered because we have to keep track of which SIPs have
  // already been loaded
  private List<SipPreview> sips;
  private int added = 0, returned = 0;
  private long lastUIUpdate = 0;

  private String id;
  private Set<ContentFilter> filters;
  private MetadataTypes metaType;
  private TemplateType templateType;
  private Path metadataPath;

  public SipPerFileVisitor(String id, Set<ContentFilter> filters, MetadataTypes metaType, Path metadataPath,
    TemplateType templateType) {
    sips = new ArrayList<>();
    sipsMap = new HashMap<>();
    this.id = id;
    this.filters = filters;
    this.metaType = metaType;
    this.metadataPath = metadataPath;
    this.templateType = templateType;
  }

  /**
   * @return The Map with the SIPs created by the SipPreviewCreator.
   */
  @Override
  public Map<String, SipPreview> getSips() {
    return sipsMap;
  }

  /**
   * @return The count of the SIPs already created.
   */
  @Override
  public int getCount() {
    return added;
  }

  /**
   * The object keeps a list with the created SIPs and this method returns them
   * one at a time.
   * 
   * @return The next SIP in the list.
   */
  @Override
  public SipPreview getNext() {
    return sips.get(returned++);
  }

  /**
   * @return True if the number of SIPs returned is smaller than the count of
   *         added SIPs.
   */
  @Override
  public boolean hasNext() {
    return returned < added;
  }

  /**
   * Sets the starting path of this TreeVisitor. This method is empty in this
   * class, but it's defined because of the SipPreviewCreator interface.
   * 
   * @param st
   *          The starting path of the TreeVisitor.
   */
  @Override
  public void setStartPath(String st) {
  }

  /**
   * This method is empty in this class, but it's defined because of the
   * TreeVisitor interface.
   * 
   * @param path
   *          The path of the directory.
   * @param attrs
   *          The attributes of the directory.
   */
  @Override
  public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
  }

  /**
   * This method is empty in this class, but it's defined because of the
   * TreeVisitor interface.
   * 
   * @param path
   *          The path of the directory.
   */
  @Override
  public void postVisitDirectory(Path path) {
    if (PathCollection.getState(path.toString()) == SourceTreeItemState.NORMAL) {
      PathCollection.addPath(path.toString(), SourceTreeItemState.MAPPED);
    }
  }

  private boolean filter(Path path) {
    String pathString = path.toString();
    for (ContentFilter cf : filters) {
      if (cf.filter(pathString))
        return true;
    }
    return false;
  }

  /**
   * Creates a new SIP with the file being visited.
   *
   * @param path
   *          The path of the file being visited.
   * @param attrs
   *          The attributes of the file being visited.
   */
  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
    if (filter(path))
      return;

    Path metaPath = getMetadataPath(path);
    TreeNode node = new TreeNode(path);
    Set<TreeNode> files = new HashSet<>();
    files.add(node);

    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), files, metaType, metaPath, templateType);
    node.addObserver(sipPreview);

    sips.add(sipPreview);
    sipsMap.put(sipPreview.getId(), sipPreview);
    added++;

    long now = System.currentTimeMillis();
    if (now - lastUIUpdate > UPDATEFREQUENCY) {
      setChanged();
      notifyObservers();
      lastUIUpdate = now;
    }
  }

  private Path getMetadataPath(Path path) {
    Path result;
    switch (metaType) {
      case SINGLE_FILE:
        result = metadataPath;
        break;
      case DIFF_DIRECTORY: // uses the same logic as the next case
      case SAME_DIRECTORY:
        result = getFileFromDir(path);
        break;
      default:
        return null;
    }
    return result;
  }

  private Path getFileFromDir(Path path) {
    String fileName = FilenameUtils.removeExtension(path.getFileName().toString());
    Path newPath = metadataPath.resolve(fileName + METADATA_EXT);
    if (Files.exists(newPath)) {
      return newPath;
    }
    newPath = metadataPath.resolve(path.getFileName() + METADATA_EXT);
    if (Files.exists(newPath)) {
      return newPath;
    }
    return null;
  }

  /**
   * This method is empty in this class, but it's defined because of the
   * TreeVisitor interface.
   *
   * @param path
   *          The path of the file.
   */
  @Override
  public void visitFileFailed(Path path) {
  }

  /**
   * Ends the tree visit, notifying the observers of modifications.
   */
  @Override
  public void end() {
    setChanged();
    notifyObservers();
  }

  /**
   * @return The id of the visitor.
   */
  @Override
  public String getId() {
    return id;
  }
}
