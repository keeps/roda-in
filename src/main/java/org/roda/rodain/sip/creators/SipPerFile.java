package org.roda.rodain.sip.creators;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPerFile extends SipPreviewCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(SipPerFile.class.getName());
  private static final int UPDATEFREQUENCY = 500; // in milliseconds
  private long lastUIUpdate = 0;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created for each
   * visited file.
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
  public SipPerFile(String id, Set<ContentFilter> filters, MetadataOptions metadataOption, String metadataType,
    Path metadataPath, String templateType, String metadataVersion) {
    super(id, filters, metadataOption, metadataType, metadataPath, templateType, metadataVersion);
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
      LOGGER.info("Access denied to file", e);
    } catch (IOException e) {
      LOGGER.error("Error walking the file tree", e);
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
    if (filter(path) || cancelled) {
      return;
    }

    TreeNode node = new TreeNode(path);
    createSip(path, node);

    long now = System.currentTimeMillis();
    if (now - lastUIUpdate > UPDATEFREQUENCY) {
      setChanged();
      notifyObservers();
      lastUIUpdate = now;
    }
  }
}
