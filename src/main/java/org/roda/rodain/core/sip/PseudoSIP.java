package org.roda.rodain.core.sip;

import java.nio.file.Path;
import java.util.Set;

import org.roda.rodain.core.rules.TreeNode;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-03-2016.
 */
public class PseudoSIP implements PseudoItem {
  private TreeNode node;
  private Set<Path> metadata;

  public PseudoSIP(TreeNode node, Set<Path> metadata) {
    this.node = node;
    this.metadata = metadata;
  }

  public TreeNode getNode() {
    return node;
  }

  public Set<Path> getMetadata() {
    return metadata;
  }
}
