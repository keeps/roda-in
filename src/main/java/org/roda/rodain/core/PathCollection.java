package org.roda.rodain.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
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
  private static Map<String, SourceTreeItemState> states = new ConcurrentHashMap<>();
  private static Map<String, SourceTreeItem> items = new HashMap<>();

  private PathCollection() {
  }

  public static void simpleAddPath(String path) {
    if (!states.containsKey(path)) {
      states.put(path, SourceTreeItemState.NORMAL);
    }
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
    if (st == SourceTreeItemState.IGNORED) {
      ignore(path);
    }

    if (states.get(path) != SourceTreeItemState.NORMAL) {
      states.put(path, st);
      verifyStateAncestors(path);
    } else {
      verifyState(path);
    }
    states.put(path, st);

    // if there's an item with this path
    if (items.containsKey(path)) {
      SourceTreeItem item = items.get(path);
      item.setState(states.get(path));
    }

    // move the modified children in the parent
    String parent = path.substring(0, path.lastIndexOf(File.separator));
    if (items.containsKey(parent) && items.get(parent) instanceof SourceTreeDirectory) {
      ((SourceTreeDirectory) items.get(parent)).moveChildrenWrongState();
    }
  }

  private static void ignore(String path) {
    states.put(path, SourceTreeItemState.IGNORED);

    Map<String, SourceTreeItemState> children = getAllChildren(path);
    for (String child : children.keySet()) {
      if (states.get(child) == SourceTreeItemState.NORMAL) {
        states.put(child, SourceTreeItemState.IGNORED);
        // update the item
        if (items.containsKey(child)) {
          items.get(child).setState(SourceTreeItemState.IGNORED);
        }
      }
    }
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
    SourceTreeItemState result;
    if (states.containsKey(path))
      result = states.get(path);
    else {
      // get the state of the parent
      int index = path.lastIndexOf(File.separator);
      String parent = path.substring(0, index);
      result = getState(parent);
    }
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

  private static void verifyStateAncestors(String path) {
    int index = 0, end = path.length();
    String separator = File.separator;

    // while we still have string to read and haven't found a matching path
    while (index >= 0) {
      // get the path until the slash we're checking
      index = path.lastIndexOf(separator, end);
      if (index == -1) {
        break;
      } else {
        String sub = path.substring(0, index);
        // move the starting index for the next iteration so it's before the
        // slash
        end = index - 1;
        verifyState(sub);
      }
    }
  }

  private static void verifyState(String path) {
    int normalItems = 0, ignoredItems = 0, mappedItems = 0;
    Map<String, SourceTreeItemState> children = getDirectChildren(path);
    for (String child : children.keySet()) {
      switch (states.get(child)) {
        case MAPPED:
          mappedItems++;
          break;
        case IGNORED:
          ignoredItems++;
          break;
        default:
          normalItems++;
          break;
      }
    }

    if (normalItems == 0) {
      // only MAPPED items, the directory is MAPPED
      if (mappedItems != 0 && ignoredItems == 0) {
        states.put(path, SourceTreeItemState.MAPPED);
      }
      // only IGNORED items, the directory is IGNORED
      if (mappedItems == 0 && ignoredItems != 0) {
        states.put(path, SourceTreeItemState.IGNORED);
      }
      // IGNORED and MAPPED items, the directory is MAPPED
      if (mappedItems != 0 && ignoredItems != 0) {
        states.put(path, SourceTreeItemState.MAPPED);
      }
    } else { // there's at least one NORMAL item, so the directory must be
      // NORMAL
      states.put(path, SourceTreeItemState.NORMAL);
    }

    if (items.containsKey(path)) {
      SourceTreeItem item = items.get(path);
      item.setState(states.get(path));
    }

  }

  private static Map<String, SourceTreeItemState> getDirectChildren(String path) {
    int countSeparators = StringUtils.countMatches(path, File.separator) + 1;
    return states.entrySet().stream().parallel()
      .filter(
        p -> p.getKey().startsWith(path) && StringUtils.countMatches(p.getKey(), File.separator) == countSeparators)
      .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }

  private static Map<String, SourceTreeItemState> getAllChildren(String path) {
    return states.entrySet().stream().parallel().filter(p -> p.getKey().startsWith(path))
      .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }
}