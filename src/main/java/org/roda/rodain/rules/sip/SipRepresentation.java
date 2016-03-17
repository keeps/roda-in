package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.TreeNode;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-03-2016.
 */
public class SipRepresentation {
  private String name;
  private Set<TreeNode> files;

  public SipRepresentation(String name) {
    this.name = name;
    files = new HashSet<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<TreeNode> getFiles() {
    return files;
  }

  public void addFile(TreeNode file) {
    files.add(file);
  }

  public void addFile(Path path) {
    files.add(new TreeNode(path));
  }

  public void setFiles(Set<TreeNode> files) {
    this.files = files;
  }

  /**
   * Removes the TreeNode with the path received as parameter.
   *
   * @param path
   *          The path of the TreeNode to be removed
   * @return The removed TreeNode
   */
  public TreeNode remove(Path path) {
    TreeNode toRemove = null;
    for (TreeNode tn : files) {
      if (tn.getPath().equals(path)) {
        toRemove = tn;
        break;
      }
    }
    if (toRemove != null) {
      files.remove(toRemove);
    }
    return toRemove;
  }

}
