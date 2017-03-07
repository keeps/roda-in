package org.roda.rodain.core.rules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 *
 *        <p>
 *        Used in the Handlers to make a representation of the documents tree in
 *        a SIP
 *        </p>
 */
public class TreeNode extends Observable {
  private Path path;
  private Map<String, TreeNode> files;

  /**
   * Creates a new TreeNode object.
   *
   * @param path
   *          The path to be associated to the TreeNode.
   */
  public TreeNode(Path path) {
    this.path = path;
    files = new HashMap<>();
  }

  /**
   * Flattens the TreeNode, i.e., moves all it's child nodes to one level.
   */
  public void flatten() {
    Map<String, TreeNode> newFiles = new HashMap<>();
    for (String file : files.keySet()) {
      if (Files.isDirectory(Paths.get(file))) {
        files.get(file).flatten(); // flatten the children
        newFiles.putAll(files.get(file).getOnlyFiles()); // add its files to the
        // new Map
      } else
        newFiles.put(file, files.get(file));
    }
    files = newFiles;

    changed();
  }

  /**
   * @return A set with all the paths from the tree that starts in the TreeNode
   *         where this method is called.
   */
  public Set<String> getFullTreePaths() {
    Set<String> result = new HashSet<>();
    result.add(path.toString());
    for (TreeNode tn : files.values())
      result.addAll(tn.getFullTreePaths());
    return result;
  }

  /**
   * @return A set with all the paths from the tree that starts in the TreeNode
   *         where this method is called.
   */
  public Set<Path> getFullTreePathsAsPaths() {
    Set<Path> result = new HashSet<>();
    result.add(path);
    for (TreeNode tn : files.values())
      result.addAll(tn.getFullTreePathsAsPaths());
    return result;
  }

  /**
   * @return The direct children of the TreeNode.
   */
  public Map<String, TreeNode> getChildren() {
    return files;
  }

  /**
   * @return The direct children of the TreeNode that are files (not
   *         directories)
   */
  public Map<String, TreeNode> getOnlyFiles() {
    Map<String, TreeNode> result = new HashMap<>();
    for (String file : files.keySet()) {
      if (!Files.isDirectory(Paths.get(file))) // add to result if it's a file
        result.put(file, files.get(file));
    }
    return result;
  }

  /**
   * If an item's path is in the selected paths to be ignored, this method
   * returns a set of the item's full tree. Else, calls this method in all its
   * children, returning all the paths removed in the children.
   *
   * @param paths
   *          is a Set of the selected paths to be ignored
   * @return a Set of all the paths removed
   */
  public Set<String> ignoreContent(Set<Path> paths) {
    Set<String> result = new HashSet<>();
    if (paths.contains(path)) {
      // this item and all its children
      result.addAll(getFullTreePaths());
    } else {
      Set<Path> toRemove = new HashSet<>();
      for (TreeNode tn : files.values()) {
        result.addAll(tn.ignoreContent(paths));
        if (paths.contains(tn.path))
          toRemove.add(tn.path);
      }
      if (!toRemove.isEmpty()) {
        for (Path p : toRemove)
          remove(p);
      }
    }
    return result;
  }

  /**
   * Adds new TreeNodes to the node's children.
   *
   * @param map
   *          The map with the new TreeNodes
   */
  public void addAll(Map<String, TreeNode> map) {
    files.putAll(map);
    changed();
  }

  /**
   * Adds a new TreeNode to the node's children
   *
   * @param node
   *          The new TreeNode to be added
   */
  public void add(TreeNode node) {
    files.put(node.getPath().toString(), node);
    changed();
  }

  /**
   * Adds a new TreeNode to the node's children
   *
   * @param node
   *          The path of the file to be added to the children. Before being
   *          added, the method creates a new TreeNode with this path.
   */
  public void add(Path node) {
    files.put(node.toString(), new TreeNode(node));
    changed();
  }

  /**
   * Removes the TreeNode with the path received as parameter.
   *
   * @param path
   *          The path of the TreeNode to be removed
   * @return The removed TreeNode
   */
  public TreeNode remove(Path path) {
    TreeNode result = files.get(path.toString());
    files.remove(path.toString());
    changed();
    return result;
  }

  /**
   * @return The node's path
   */
  public Path getPath() {
    return path;
  }

  /**
   * @return A set of the node's children's paths.
   */
  public Set<String> getKeys() {
    return files.keySet();
  }

  /**
   * @param key
   *          The path of the TreeNode we want to get.
   * @return The TreeNode with the path received as parameter
   */
  public TreeNode get(String key) {
    return files.get(key);
  }

  private void changed() {
    setChanged();
    notifyObservers();
  }

  /**
   * Adds a new observer to the TreeNode and all its children
   *
   * @param o
   *          The Observer to be added
   */
  @Override
  public void addObserver(Observer o) {
    super.addObserver(o);
    for (TreeNode tn : files.values()) {
      tn.addObserver(o);
    }
  }
}
