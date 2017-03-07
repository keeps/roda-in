package org.roda.rodain.core.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.roda.rodain.core.rules.filters.IgnoredFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidFilesCounter implements FileVisitor<Path> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidFilesCounter.class.getName());

  int validFiles;

  public ValidFilesCounter() {
    validFiles = 0;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    LOGGER.debug("preVisitDirectory '{}'", dir);
    /*
     * if (IgnoredFilter.isIgnored(dir)) { return FileVisitResult.SKIP_SUBTREE;
     * }else{ return FileVisitResult.CONTINUE; }
     */
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    LOGGER.debug("postVisitDirectory '{}'", dir);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    LOGGER.debug("visitFile '{}'", file);
    if (!IgnoredFilter.isIgnored(file)) {
      validFiles++;
      return FileVisitResult.TERMINATE;
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    LOGGER.debug("visitFileFailed '{}'", file);
    return FileVisitResult.CONTINUE;
  }

  public int getValidFiles() {
    return validFiles;
  }

}