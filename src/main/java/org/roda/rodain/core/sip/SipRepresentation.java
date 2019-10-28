package org.roda.rodain.core.sip;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.RepresentationContentType;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-03-2016.
 */
public class SipRepresentation {
  private String name;
  private RepresentationContentType type;
  private Set<TreeNode> files;

  public SipRepresentation(String name) {
    this.name = name;
    files = new HashSet<>();
    this.type = RepresentationContentType.defaultRepresentationContentType();
  }

  /**
   * @return The name of the SipRepresentation
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the SipRepresentation
   * 
   * @param name
   *          The name to be set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return The set of direct TreeNode that the SipRepresentation contains.
   * @see TreeNode
   */
  public Set<TreeNode> getFiles() {
    return files;
  }

  /**
   * Adds a new file to the SipRepresentation's set of files.
   * 
   * @param file
   *          The file to be added.
   */
  public void addFile(TreeNode file) {
    files.add(file);
  }

  /**
   * Adds a new file to the SipRepresentation's set of files.
   * 
   * @param path
   *          The path of the file to be added.
   */
  public void addFile(Path path) {
    files.add(new TreeNode(path));
  }

  /**
   * Sets the set of files of the SipRepresentation
   * 
   * @param files
   *          The set of files to be set.
   */
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

  public RepresentationContentType getType() {
    return type;
  }

  public void setType(RepresentationContentType type) {
    this.type = type;
  }

}
