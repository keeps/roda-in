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

  public PseudoDescriptionObject(Path path) {
    this.path = path;
    children = new ArrayList<>();
  }

  public void addChild(PseudoItem child) {
    children.add(child);
  }

  public Path getPath() {
    return path;
  }

  public List<PseudoItem> getChildren() {

    return children;
  }
}
