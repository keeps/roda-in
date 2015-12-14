package org.roda.rodain.source.representation;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceFile implements SourceItem {
  private Path path;

  /**
   * Creates a new SourceFile object.
   *
   * @param path The path to be associated to the object.
   */
  public SourceFile(Path path) {
    this.path = path;
  }

  /**
   * @return The associated path
   */
  public Path getPath() {
    return path;
  }

  /**
   * Sets the path of the object
   *
   * @param path The path
   */
  public void setPath(Path path) {
    this.path = path;
  }
}
