package org.roda.rodain.core.utils;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public interface TreeVisitor {
  /**
   * Method called before visiting a directory.
   * @param path The path of the directory
   * @param attrs The attributes of the directory
   */
  void preVisitDirectory(Path path, BasicFileAttributes attrs);

  /**
   * Method called after visiting a directory
   * @param path The path of the directory
   */
  void postVisitDirectory(Path path);

  /**
   * Method called whenever a file is found.
   * @param path The path of the file.
   * @param attrs The attributes of the vile
   */
  void visitFile(Path path, BasicFileAttributes attrs);

  /**
   * Method called when the visit of a file fails.
   * @param path the path of the file
   */
  void visitFileFailed(Path path);

  /**
   * Method that executes the finishing tasks of the tree visit process.
   */
  void end();

  /**
   * @return The ID of the TreeVisitor.
   */
  String getId();

  /**
   * Sets the path where the tree visit starts.
   * @param path The path where the tree visit starts.
   */
  void setStartPath(String path);
}
