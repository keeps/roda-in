package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.schema.DescObjMetadata;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-11-2015.
 */
public class SipPerSelection extends SipPreviewCreator {
  private static final int UPDATEFREQUENCY = 500; // in milliseconds
  private long lastUIUpdate = 0;
  private Set<String> selectedPaths;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created for each
   * selected path.
   *
   * @param id
   *          The id of the SipPreviewCreator.
   * @param selectedPaths
   *          The set of selected paths for the SIPs creation
   * @param filters
   *          The set of content filters
   * @param metaType
   *          The type of metadata to be applied to each SIP
   * @param metadataPath
   *          The path of the metadata
   * @param templateType
   *          The type of the metadata template
   */
  public SipPerSelection(String id, Set<String> selectedPaths, Set<ContentFilter> filters, MetadataTypes metaType,
    Path metadataPath, String templateType, String templateVersion) {
    super(id, filters, metaType, metadataPath, templateType, templateVersion);
    this.selectedPaths = selectedPaths;
  }

  @Override
  public boolean filter(Path path) {
    boolean result = super.filter(path);

    if (path.getFileName() == null) {
      result = true;
    }
    if (templateType != null) {
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + templateType);
      result = matcher.matches(path.getFileName());
    }
    return result;
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
   * Adds the current directory to its parent's node. Creates a SIP if this path
   * is in the selected set of paths.
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

    // Check if we create a new SIP using this node
    if (selectedPaths.contains(path.toString())) {
      createSip(path, node);
    }

    long now = System.currentTimeMillis();
    if (now - lastUIUpdate > UPDATEFREQUENCY) {
      setChanged();
      notifyObservers();
      lastUIUpdate = now;
    }
  }

  private void createSip(Path path, TreeNode node) {
    Path metaPath = getMetadataPath(path);
    // create a new Sip
    Set<TreeNode> files = new HashSet<>();
    files.add(node);

    DescObjMetadata metadata;
    if (metaType == MetadataTypes.TEMPLATE)
      metadata = new DescObjMetadata(metaType, templateType, templateVersion);
    else
      metadata = new DescObjMetadata(metaType, metaPath);

    SipRepresentation rep = new SipRepresentation("rep1");
    rep.setFiles(files);
    Set<SipRepresentation> repSet = new HashSet<>();
    repSet.add(rep);
    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), repSet, metadata);
    node.addObserver(sipPreview);

    sips.add(sipPreview);
    sipsMap.put(sipPreview.getId(), sipPreview);
    added++;
  }

  /**
   * If the path is in the selected set of paths creates a new SIP using the
   * file, otherwise, adds the visited file to its parent.
   *
   * @param path
   *          The path of the visited file
   * @param attrs
   *          The attributes of the visited file
   */
  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
    if (filter(path) || cancelled)
      return;
    if (selectedPaths.contains(path.toString())) {
      createSip(path, new TreeNode(path));
    } else {
      if (nodes.isEmpty()) {
        nodes.add(new TreeNode(path.getParent()));
      }
      nodes.peekLast().add(path);
    }
  }

  private Path getMetadataPath(Path sipPath) {
    Path result = null;
    if (metaType == MetadataTypes.SINGLE_FILE) {
      result = metadataPath;
    } else if (metaType == MetadataTypes.SAME_DIRECTORY) {
      result = searchMetadata(sipPath);
    }
    return result;
  }

  private Path searchMetadata(Path sipPath) {
    File dir = sipPath.toFile();
    if (!dir.isDirectory())
      dir = sipPath.getParent().toFile();

    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + templateType);
    File[] foundFiles = dir.listFiles((dir1, name) -> {
      return matcher.matches(Paths.get(name));
    });

    if (foundFiles != null && foundFiles.length > 0) {
      return foundFiles[0].toPath();
    }
    return null;
  }
}
