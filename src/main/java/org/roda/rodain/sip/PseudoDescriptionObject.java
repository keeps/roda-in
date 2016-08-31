package org.roda.rodain.sip;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-03-2016.
 */
public class PseudoDescriptionObject implements PseudoItem {
  private Path path;
  private List<PseudoItem> children;

  /**
   * Instantiates a new PseudDescriptionObject
   * @param path The path of the object
   */
  public PseudoDescriptionObject(Path path) {
    this.path = path;
    children = new ArrayList<>();
  }

  /**
   * Adds a child item to the children list.
   * @param child The child item.
   */
  public void addChild(PseudoItem child) {
    children.add(child);
  }

  /**
   * @return The path of the object
   */
  public Path getPath() {
    return path;
  }

  /**
   * @return The children list of the object.
   */
  public List<PseudoItem> getChildren() {
    return children;
  }
}
