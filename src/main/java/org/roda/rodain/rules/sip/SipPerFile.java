package org.roda.rodain.rules.sip;

import org.apache.commons.io.FilenameUtils;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPerFile extends SipPreviewCreator {
  private static final Logger log = LoggerFactory.getLogger(SipPerFile.class.getName());
  private static final int UPDATEFREQUENCY = 500; // in milliseconds
  private long lastUIUpdate = 0;

  private Map<String, Set<Path>> metadata;

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
    super(id, filters, metaType, metadataPath, templateType, templateVersion);
    metadata = new HashMap<>();

    if (metadataPath != null && metaType == MetadataTypes.DIFF_DIRECTORY) {
      try {
        Files.walkFileTree(metadataPath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String key = FilenameUtils.removeExtension(file.getFileName().toString());
            Set<Path> paths = metadata.get(key);
            if (paths == null)
              paths = new HashSet<>();
            paths.add(file);
            metadata.put(key, paths);
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (AccessDeniedException e) {
        log.info("Access denied to file", e);
      } catch (IOException e) {
        log.error("Error walking the file tree", e);
      }
    }
  }

  @Override
  public void setStartPath(String path) {
    try {
      Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (!filter(file))
            PathCollection.simpleAddPath(file.toString());
          return isTerminated();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (!filter(dir))
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
   */
  @Override
  public void postVisitDirectory(Path path) {
    if (PathCollection.getState(path.toString()) == SourceTreeItemState.NORMAL) {
      PathCollection.addPath(path.toString(), SourceTreeItemState.MAPPED);
    }
  }

  @Override
  public boolean filter(Path path) {
    boolean result = super.filter(path);
    if (result)
      return true;

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

    DescObjMetadata dom = null;
    if (metaType == MetadataTypes.TEMPLATE)
      dom = new DescObjMetadata(metaType, templateType, templateVersion);
    else if (metaPath != null)
      dom = new DescObjMetadata(metaType, metaPath);

    SipRepresentation rep = new SipRepresentation("rep1");
    rep.setFiles(files);
    Set<SipRepresentation> repSet = new HashSet<>();
    repSet.add(rep);
    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), repSet, dom);
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
    File[] foundFiles = dir.listFiles((dir1, name) -> matcher.matches(Paths.get(name)));

    if (foundFiles != null && foundFiles.length > 0) {
      return foundFiles[0].toPath();
    }
    return null;
  }

  private Path getFileFromDir(Path path) {
    String fileNameWithExtension = path.getFileName().toString();
    String fileName = FilenameUtils.removeExtension(fileNameWithExtension);

    Set<Path> paths = metadata.get(fileName);
    Path result = null;
    if (paths != null) {
      for (Path p : paths) {
        if (!p.getFileName().toString().equals(fileNameWithExtension))
          result = p;

      }
    }
    return result;
  }
}
