package org.roda.rodain.rules.sip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.utils.TreeVisitor;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPerFolderVisitor extends Observable implements TreeVisitor, SipPreviewCreator {
  private static final int UPDATEFREQUENCY = 500; // in milliseconds
  private long lastUIUpdate = 0;
  private String startPath;
  // This map is returned, in full, to the SipPreviewNode when there's an update
  private Map<String, SipPreview> sipsMap;
  // This ArrayList is used to keep the SIPs ordered.
  // We need them ordered because we have to keep track of which SIPs have
  // already been loaded
  private List<SipPreview> sips;
  private int added = 0, returned = 0;
  private Deque<TreeNode> nodes;

  private String id;
  private int maxLevel;
  private Set<ContentFilter> filters;
  private MetadataTypes metaType;
  private Path metadataPath;
  private TemplateType templateType;

  public SipPerFolderVisitor(String id, int maxLevel, Set<ContentFilter> filters, MetadataTypes metaType,
    Path metadataPath, TemplateType templateType) {
    this.maxLevel = maxLevel;
    this.filters = filters;
    this.metaType = metaType;
    this.metadataPath = metadataPath;
    this.templateType = templateType;
    sips = new ArrayList<>();
    sipsMap = new HashMap<>();
    nodes = new ArrayDeque<>();
    this.id = id;
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

  private boolean filter(Path path) {
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
  @Override
  public void setStartPath(String st) {
    startPath = st;
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
    if (filter(path))
      return;
    TreeNode newNode = new TreeNode(path);
    nodes.add(newNode);
  }

  /**
   * Adds the current directory to its parent's node. Creates a SIP if the level
   * is smaller than the max level configured.
   * 
   * @param path
   *          The path of the directory.
   */
  @Override
  public void postVisitDirectory(Path path) {
    if (filter(path))
      return;
    // pop the node of this directory and add it to its parent (if it exists)
    TreeNode node = nodes.removeLast();
    if (!nodes.isEmpty())
      nodes.peekLast().add(node);

    // Check if we create a new SIP using this node
    // every directory is a sub-directory of the start path, so if we remove it,
    // we get the relative path to it
    String relative = path.toString().replace(startPath, "");
    Path relativePath = Paths.get(relative);
    int relativeLevel = relativePath.getNameCount();

    if (relativeLevel <= maxLevel) {
      Path metaPath = getMetadataPath();
      // create a new Sip
      Set<TreeNode> files = new HashSet<>();
      files.add(node);

      SipPreview sipPreview = new SipPreview(path.getFileName().toString(), files, metaType, metaPath,
 templateType);
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
  }

  /**
   * Adds the visited file to its parent
   *
   * @param path
   *          The path of the visited file
   * @param attrs
   *          The attributes of the visited file
   */
  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
    if (filter(path))
      return;
    nodes.peekLast().add(path);
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

  private Path getMetadataPath() {
    Path result = null;
    if (metaType == MetadataTypes.SINGLE_FILE)
      result = metadataPath;
    return result;
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
