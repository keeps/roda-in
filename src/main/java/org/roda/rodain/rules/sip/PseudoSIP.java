package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.TreeNode;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-03-2016.
 */
public class PseudoSIP implements PseudoItem {
  private TreeNode node;
  private Path metadata;

  public PseudoSIP(TreeNode node, Path metadata) {
    this.node = node;
    this.metadata = metadata;
  }

  public TreeNode getNode() {
    return node;
  }

  public Path getMetadata() {
    return metadata;
  }
}
