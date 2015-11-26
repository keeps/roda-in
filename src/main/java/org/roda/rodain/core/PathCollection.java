package org.roda.rodain.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

/**
 * A collection of paths and it's associated state and SourceTreeItem.
 *
 * <p>
 * All of this class's methods are static so that it can be used in the entire
 * application easily. This is useful because there's several places where the
 * state of a path can be changed and, with this class, this information is
 * always coherent, since all of them report the changes to it.
 * </p>
 *
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 12-11-2015.
 */
public class PathCollection {
  private static Map<String, SourceTreeItemState> states = new HashMap<>();
  private static Map<String, SourceTreeItem> items = new HashMap<>();

  private PathCollection() {
  }

  /**
   * Adds a path and its state to collection.
   *
   * <p>
   * If there's an item associated to the path, this method also updates its
   * state to avoid conflicts.
   * </p>
   * <p>
   * After the state is set, it also verifies the state of every directory path
   * until the root. For example when /a/b/c is MAPPED and /a/b/c/d.txt is set
   * back to NORMAL, the method checks the state of the directory "c", then "b"
   * and finally "a".
   * </p>
   * 
   * @param path
   *          The path to be added to the collection.
   * @param st
   *          The state of the item.
   */
  public static void addPath(String path, SourceTreeItemState st) {
    states.put(path, st);

    // if there's an item with this path
    if (items.containsKey(path)) {
      SourceTreeItem item = items.get(path);
      item.setState(states.get(path));
    }
    checkStateParents(path);
  }

  /**
   * Adds a set of paths to the collection, mapping them to a state.
   *
   * @param paths
   *          The set of paths to be added to the collection.
   * @param st
   *          The state of the items.
   * @see #addPath(String, SourceTreeItemState)
   */
  public static void addPaths(Set<String> paths, SourceTreeItemState st) {
    for (String path : paths)
      addPath(path, st);
  }

  /**
   * Adds a SourceTreeItem reference to the collection.
   *
   * <p>
   * If the item's path isn't in the collection, it's also added.
   * </p>
   * 
   * @param item
   *          The item to be added to the collection.
   */
  public static void addItem(SourceTreeItem item) {
    String path = item.getPath();
    if (!states.containsKey(path)) {
      states.put(path, item.getState());
    }
    items.put(path, item);
  }

  /**
   * Used to get the state associated with a path.
   *
   * @param path
   *          The path used to get the state.
   * @return The path's associated state if the path is in the collection,
   *         otherwise NORMAL.
   */
  public static SourceTreeItemState getState(String path) {
    SourceTreeItemState result = SourceTreeItemState.NORMAL;
    if (states.containsKey(path))
      result = states.get(path);
    return result;
  }

  /**
   * Used to get the SourceTreeItem associated to a path.
   *
   * @param path
   *          The path used to get the item.
   * @return The associated item if the path is in the collection, null
   *         otherwise.
   */
  public static SourceTreeItem getItem(String path) {
    return items.get(path);
  }

  private static void checkStateParents(String path) {
    int index = 0, end = path.length();
    String separator = File.separator;

    while (index >= 0) { // while we still have string to read and haven't found
                         // a matching path
      index = path.lastIndexOf(separator, end); // get the path until the slash
                                                // we're checking
      if (index == -1) {
        break;
      } else {
        String sub = path.substring(0, index);
        end = index - 1; // move the starting index for the next iteration so
                         // it's before the slash
        if (items.containsKey(sub) && items.get(sub) instanceof SourceTreeDirectory) {
          SourceTreeDirectory dir = (SourceTreeDirectory) items.get(sub);
          dir.verifyState();
          if (states.get(path) == SourceTreeItemState.NORMAL && dir.getState() == SourceTreeItemState.MAPPED) {
            addPath(sub, SourceTreeItemState.NORMAL);
          }
        } else {
          states.put(sub, SourceTreeItemState.NORMAL);
        }
      }
    }
  }
}