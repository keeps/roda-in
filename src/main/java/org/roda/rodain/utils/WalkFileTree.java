package org.roda.rodain.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class WalkFileTree extends Thread {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(WalkFileTree.class.getName());
  private Set<String> paths;
  private TreeVisitor handler;
  private boolean cancelled = false;

  private int processedFiles = 0, processedDirs = 0;

  public WalkFileTree(Set<String> startPath, TreeVisitor handler) {
    this.paths = startPath;
    this.handler = handler;
  }

  @Override
  public void run() {
    for (String startPath : paths) {
      handler.setStartPath(startPath);
      final Path path = Paths.get(startPath);
      // walkFileTree doesn't work if the start path is a file, so we call the
      // method directly
      if (!Files.isDirectory(path)) {
        handler.visitFile(path, null);
      } else {
        try {
          Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              processedFiles++;
              handler.visitFile(file, attrs);
              return isTerminated();
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
              handler.preVisitDirectory(dir, attrs);
              return isTerminated();
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
              processedDirs++;
              handler.postVisitDirectory(dir);
              return isTerminated();
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
              handler.visitFileFailed(file);
              return isTerminated();
            }
          });
        } catch (AccessDeniedException e) {
          log.info("Access denied to file", e);
        } catch (IOException e) {
          log.error("Error walking the file tree", e);
        }
      }
    }
    handler.end();
  }

  public void cancel() {
    cancelled = true;
  }

  public int getProcessedDirs() {
    return processedDirs;
  }

  public int getProcessedFiles() {
    return processedFiles;
  }

  private FileVisitResult isTerminated() {
    // terminate if the thread has been interrupted
    if (Thread.interrupted() || cancelled) {
      return FileVisitResult.TERMINATE;
    }
    return FileVisitResult.CONTINUE;
  }
}
