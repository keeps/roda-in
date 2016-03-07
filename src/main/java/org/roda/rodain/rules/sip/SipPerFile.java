package org.roda.rodain.rules.sip;

import org.apache.commons.io.FilenameUtils;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.TreeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPerFile extends Observable implements TreeVisitor, SipPreviewCreator {
  private static final Logger log = LoggerFactory.getLogger(SipPerFile.class.getName());
  private static final int UPDATEFREQUENCY = 500; // in milliseconds
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
  private String templateType;
  private String templateVersion;
  private Path metadataPath;

  private boolean cancelled = false;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created for each
   * visited file.
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
  public SipPerFile(String id, Set<ContentFilter> filters, MetadataTypes metaType, Path metadataPath,
    String templateType, String templateVersion) {
    sips = new ArrayList<>();
    sipsMap = new HashMap<>();
    this.id = id;
    this.filters = filters;
    this.metaType = metaType;
    this.metadataPath = metadataPath;
    this.templateType = templateType;
    this.templateVersion = templateVersion;

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
   * @param path
   *          The starting path of the TreeVisitor.
   */
  @Override
  public void setStartPath(String path) {
    try {
      Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          PathCollection.simpleAddPath(file.toString());
          return isTerminated();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          PathCollection.simpleAddPath(dir.toString());
          return isTerminated();
        }
      });
    } catch (AccessDeniedException e) {
      log.info("Access denied to file", e);
    } catch (IOException e) {
      log.error("Error walking the file tree", e);
    }
  }

  private FileVisitResult isTerminated() {
    // terminate if the thread has been interrupted
    if (Thread.interrupted() || cancelled) {
      return FileVisitResult.TERMINATE;
    }
    return FileVisitResult.CONTINUE;
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
    if (path.getFileName() == null) {
      return true;
    }
    if (templateType != null) {
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + templateType);
      return matcher.matches(path.getFileName());
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
    if (filter(path) || cancelled)
      return;

    Path metaPath = getMetadataPath(path);
    TreeNode node = new TreeNode(path);
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
      case DIFF_DIRECTORY:
        result = getFileFromDir(path);
        break;
      case SAME_DIRECTORY:
        result = searchMetadata(path);
        break;
      default:
        return null;
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

  private Path getFileFromDir(Path path) {
    String fileName = FilenameUtils.removeExtension(path.getFileName().toString());
    File dir = new File(metadataPath.toString());
    File[] foundFiles = dir.listFiles((dir1, name) -> {
      return name.startsWith(fileName + ".");
    });

    if (foundFiles.length > 0) {
      return foundFiles[0].toPath();
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

  /**
   * Cancels the execution of the SipPreviewCreator
   */
  @Override
  public void cancel() {
    cancelled = true;
  }
}
