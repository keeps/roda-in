package org.roda.rodain.inspection.trees;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 08-03-2016.
 */
public class ContentCreator extends Observable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContentCreator.class.getName());
  private Deque<TreeNode> nodes;
  private Set<TreeNode> files;
  private Set<Path> paths;

  private Set<ContentFilter> filters;

  private boolean cancelled = false;

  /**
   * Creates a new DocumentationCreator object
   * 
   * @param filters
   *          The set of content filters
   */
  public ContentCreator(Set<ContentFilter> filters, Set<Path> paths) {
    this.filters = filters;
    this.paths = paths;
    nodes = new ArrayDeque<>();
    files = new HashSet<>();
  }

  public Set<TreeNode> start() {
    for (Path path : paths) {
      // walkFileTree doesn't work if the start path is a file, so we call the
      // method directly
      if (!Files.isDirectory(path)) {
        visitFile(path, null);
      } else {
        try {
          ContentCreator refToThis = this;
          Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              refToThis.visitFile(file, attrs);
              return isTerminated();
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
              refToThis.preVisitDirectory(dir, attrs);
              return isTerminated();
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
              refToThis.postVisitDirectory(dir);
              return isTerminated();
            }
          });
        } catch (AccessDeniedException e) {
          LOGGER.info("Access denied to file", e);
        } catch (IOException e) {
          LOGGER.error("Error walking the file tree", e);
        }
      }
    }
    end();
    return files;
  }

  private FileVisitResult isTerminated() {
    // terminate if the thread has been interrupted
    if (Thread.interrupted() || cancelled) {
      return FileVisitResult.TERMINATE;
    }
    return FileVisitResult.CONTINUE;
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
   * Creates a new TreeNode and adds it to the nodes Deque if the path isn't
   * mapped or ignored.
   *
   * @param path
   *          The path of the directory.
   * @param attrs
   *          The attributes of the directory.
   */
  private void preVisitDirectory(Path path, BasicFileAttributes attrs) {
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
  private void postVisitDirectory(Path path) {
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
  private void visitFile(Path path, BasicFileAttributes attrs) {
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
  private void end() {
    setChanged();
    notifyObservers();
  }

  public Set<TreeNode> getFiles() {
    return files;
  }
}
