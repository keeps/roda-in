package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.utils.TreeVisitor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipSingle extends Observable implements TreeVisitor, SipPreviewCreator {
  private String startPath;
  // This map is returned, in full, to the SipPreviewNode when there's an update
  private Map<String, SipPreview> sipsMap;
  // This ArrayList is used to keep the SIPs ordered.
  // We need them ordered because we have to keep track of which SIPs have
  // already been loaded
  private List<SipPreview> sips;
  private int added = 0, returned = 0;
  private Deque<TreeNode> nodes;
  private Set<TreeNode> files;

  private String id;
  private Set<ContentFilter> filters;
  private MetadataTypes metaType;
  private Path metadataPath;
  private TemplateType templateType;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created with all the visited paths.
   *
   * @param id           The id of the SipPreviewCreator.
   * @param filters      The set of content filters
   * @param metaType     The type of metadata to be applied to each SIP
   * @param metadataPath The path of the metadata
   * @param templateType The type of the metadata template
   */
  public SipSingle(String id, Set<ContentFilter> filters, MetadataTypes metaType, Path metadataPath,
                   TemplateType templateType) {
    this.filters = filters;
    sipsMap = new HashMap<>();
    sips = new ArrayList<>();
    nodes = new ArrayDeque<>();
    this.id = id;
    this.metaType = metaType;
    this.metadataPath = metadataPath;
    this.templateType = templateType;
    files = new HashSet<>();
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
   * added SIPs.
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
   * @param st The starting path of the TreeVisitor.
   */
  @Override
  public void setStartPath(String st) {
    startPath = st;
  }

  /**
   * Creates a new TreeNode and adds it to the nodes Deque if the path isn't
   * mapped or ignored.
   *
   * @param path  The path of the directory.
   * @param attrs The attributes of the directory.
   */
  @Override
  public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
    if (filter(path))
      return;
    TreeNode newNode = new TreeNode(path);
    nodes.add(newNode);
  }

  /**
   * Adds the current directory to its parent's node. If the parent doesn't
   * exist, adds a new node to the Deque.
   *
   * @param path The path of the directory.
   */
  @Override
  public void postVisitDirectory(Path path) {
    if (filter(path))
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
   * @param path  The path of the visited file
   * @param attrs The attributes of the visited file
   */
  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
    if (filter(path)) {
      return;
    }
    if (nodes.isEmpty())
      files.add(new TreeNode(path));
    else
      nodes.peekLast().add(path);
  }

  /**
   * This method is empty in this class, but it's defined because of the
   * TreeVisitor interface.
   *
   * @param path The path of the file.
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
    Path metaPath = getMetadata();
    // create a new Sip
    Path path = Paths.get(startPath);
    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), files, metaType, metaPath, templateType);

    for (TreeNode tn : files) {
      tn.addObserver(sipPreview);
    }

    sips.add(sipPreview);
    sipsMap.put(sipPreview.getId(), sipPreview);
    added++;

    setChanged();
    notifyObservers();
  }

  private Path getMetadata() {
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
}
