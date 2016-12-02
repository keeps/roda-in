package org.roda.rodain.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.roda.rodain.rules.filters.IgnoredFilter;

class ValidFilesCounter implements FileVisitor<Path> {
  int validFiles;

  public ValidFilesCounter() {
    validFiles = 0;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    /*if (IgnoredFilter.isIgnored(dir)) {
      return FileVisitResult.SKIP_SUBTREE;
    }else{
      return FileVisitResult.CONTINUE;
    }*/
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (!IgnoredFilter.isIgnored(file)) {
      validFiles++;
      return FileVisitResult.TERMINATE;
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  public int getValidFiles() {
    return validFiles;
  }

}