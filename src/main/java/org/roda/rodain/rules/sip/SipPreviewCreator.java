package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.utils.TreeVisitor;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public class SipPreviewCreator extends Observable implements TreeVisitor {
  private String startPath;
  // This map is returned, in full, to the SipPreviewNode when there's an update
  protected Map<String, SipPreview> sipsMap;
  // This ArrayList is used to keep the SIPs ordered.
  // We need them ordered because we have to keep track of which SIPs have
  // already been loaded
  protected List<SipPreview> sips;
  protected int added = 0, returned = 0;
  protected Deque<TreeNode> nodes;
  protected Set<TreeNode> files;

  private String id;
  private Set<ContentFilter> filters;
  protected MetadataTypes metaType;
  protected Path metadataPath;
  protected String templateType, templateVersion;

  protected boolean cancelled = false;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created with all
   * the visited paths.
   *
   * @param id
   *          The id of the SipPreviewCreator.
   * @param filters
   *          The set of content filters
   * @param metaType
   *          The type of metadata to be applied to each SIP
   * @param metadataPath
   *          The path of the metadata
   * @param templateType
   *          The type of the metadata template
   */
  public SipPreviewCreator(String id, Set<ContentFilter> filters, MetadataTypes metaType, Path metadataPath,
    String templateType, String templateVersion) {
    this.filters = filters;
    sipsMap = new HashMap<>();
    sips = new ArrayList<>();
    nodes = new ArrayDeque<>();
    this.id = id;
    this.metaType = metaType;
    this.metadataPath = metadataPath;
    this.templateType = templateType;
    this.templateVersion = templateVersion;
    files = new HashSet<>();
  }

  /**
   * @return The Map with the SIPs created by the SipPreviewCreator.
   */
  public Map<String, SipPreview> getSips() {
    return sipsMap;
  }

  /**
   * @return The count of the SIPs already created.
   */
  public int getCount() {
    return added;
  }

  /**
   * The object keeps a list with the created SIPs and this method returns them
   * one at a time.
   *
   * @return The next SIP in the list.
   */
  public SipPreview getNext() {
    return sips.get(returned++);
  }

  /**
   * @return True if the number of SIPs returned is smaller than the count of
   *         added SIPs.
   */
  public boolean hasNext() {
    return returned < added;
  }

  protected boolean filter(Path path) {
    String pathString = path.toString();
    for (ContentFilter cf : filters) {
      if (cf.filter(pathString))
        return true;
    }
    return false;
  }

  /**
   * Sets the starting path of this TreeVisitor.
   *
   * @param st
   *          The starting path of the TreeVisitor.
   */
  public void setStartPath(String st) {
    startPath = st;
  }

  public String getStartPath() {
    return startPath;
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
   * Ends the tree visit, creating the SIP with all the files added during the
   * visit and notifying the observers.
   */
  @Override
  public void end() {
    setChanged();
    notifyObservers();
  }

  protected Path getMetadata() {
    Path result = null;
    if (metaType == MetadataTypes.SINGLE_FILE)
      result = metadataPath;
    return result;
  }

  /**
   * @return The id of the visitor.
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Cancels the execution of the SipPreviewCreator
   */
  public void cancel() {
    cancelled = true;
  }
}
