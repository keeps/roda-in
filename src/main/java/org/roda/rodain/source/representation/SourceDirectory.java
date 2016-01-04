package org.roda.rodain.source.representation;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceDirectory implements SourceItem {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SourceDirectory.class.getName());
  private Path path;
  private int itemsToLoad = 0;
  private TreeMap<String, SourceItem> children;
  private DirectoryStream<Path> directoryStream;
  private Iterator<Path> iterator;
  private boolean showFiles;

  /**
   * Creates a new SourceDirectory object.
   *
   * @param path      The path associated to the object
   * @param showFiles Whether to show the files or not
   */
  public SourceDirectory(Path path, boolean showFiles) {
    this.path = path;
    this.showFiles = showFiles;
    children = new TreeMap<>();
  }

  /**
   * Gets the associated path.
   *
   * @return The path
   */
  public Path getPath() {
    return path;
  }

  /**
   * Gets the children of the directory
   *
   * @return The children of the directory
   */
  public SortedMap<String, SourceItem> getChildren() {
    return children;
  }

  /**
   * Gets a single child
   *
   * @param p The path of the child
   * @return The child
   */
  public SourceItem getChild(Path p) {
    return children.get(p.toString());
  }

  /**
   * Only returns an object if the path is a directory, casting the result.
   *
   * @param p The path of the child
   * @return The casted object of the child or null if the path isn't a directory.
   */
  public SourceDirectory getChildDirectory(Path p) {
    if (Files.isDirectory(p))
      return (SourceDirectory) children.get(p.toString());
    return null;
  }

  /**
   * @return True if the iterator isn't closed, false otherwise.
   */
  public boolean isStreamOpen() {
    return iterator.hasNext();
  }

  /**
   * Adds a new child to the map.
   *
   * @param p    The path of the child
   * @param item The object of the child
   */
  public void addChild(Path p, SourceItem item) {
    children.put(p.toString(), item);
  }

  /**
   * Closes the directory stream if it hasn't been closed yet.
   */
  public void closeDirectoryStream() {
    try {
      if (directoryStream != null)
        directoryStream.close();
    } catch (IOException e) {
      log.error("Error closing directory stream", e);
    }
  }

  private void startDirectoryStream() {
    if (directoryStream != null)
      return;

    try {
      directoryStream = Files.newDirectoryStream(path);
      iterator = directoryStream.iterator();
    } catch (AccessDeniedException e) {
      log.info("No access to file", e);
    } catch (IOException e) {
      log.error("Error accessing file", e);
    }
  }

  /**
   * @return True if the diretory has been loaded at least once, false otherwise.
   */
  public boolean isFirstLoaded() {
    return iterator != null;
  }

  /**
   * Loads more items to the children map.
   *
   * @return The map with the newly added items
   */
  public SortedMap<String, SourceItem> loadMore() {
    startDirectoryStream();
    int loaded = 0, childrenSize = children.size();
    TreeMap<String, SourceItem> result = new TreeMap<>();
    if (iterator != null) {
      itemsToLoad += 50;
      while (iterator.hasNext() && (childrenSize + loaded < itemsToLoad)) {
        Path file = iterator.next();
        if (!showFiles && !Files.isDirectory(file))
          continue;
        SourceItem added = loadChild(file);
        result.put(file.toString(), added);
        loaded++;
      }
      // we can close the directory stream if there's no more files to load in
      // the iterator
      if (!iterator.hasNext())
        closeDirectoryStream();
    }
    return result;
  }

  private SourceItem loadChild(Path file) {
    SourceItem item;
    if (Files.isDirectory(file)) {
      item = new SourceDirectory(file, showFiles);
      addChild(file, item);

    } else {
      item = new SourceFile(file);
      addChild(file, item);
    }
    return item;
  }
}
