package org.roda.rodain.utils;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Observable;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public interface TreeVisitor {
  void preVisitDirectory(Path path, BasicFileAttributes attrs);

  void postVisitDirectory(Path path);

  void visitFile(Path path, BasicFileAttributes attrs);

  void visitFileFailed(Path path);

  void end();

  String getId();

  void setStartPath(String path);
}
