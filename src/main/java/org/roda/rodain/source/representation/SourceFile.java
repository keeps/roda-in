package org.roda.rodain.source.representation;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceFile implements SourceItem {
  private Path path;

  public SourceFile(Path path) {
    this.path = path;
  }

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }
}
