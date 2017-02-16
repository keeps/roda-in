package org.roda.rodain.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.roda.rodain.sip.SipPreview;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of paths and it's associated state and SourceTreeItem.
 * <p/>
 * <p>
 * All of this class's methods are static so that it can be used in the entire
 * application easily. This is useful because there's several places where the
 * state of a path can be changed and, with this class, this information is
 * always coherent, since all of them report the changes to it.
 * </p>
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 12-11-2015.
 */
public class PathCollection {
  private static Map<Path, SourceTreeItemState> states = new ConcurrentHashMap<>();
  private static Map<Path, SourceTreeItem> items = new HashMap<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(PathCollection.class.getName());

  private PathCollection() {
  }

  /**
   * If the path isn't in the collection, adds it and sets its state as NORMAL.
   *
   * @param path
   *          The path to be added to the collection
   */
  public static void simpleAddPath(Path path) {
    if (!"".equals(path.toString()) && !states.containsKey(path)) {
      states.put(path, SourceTreeItemState.NORMAL);
    }
  }

  /**
   * Adds a path and its state to collection.
   * <p/>
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
  public static void addPath(Path path, SourceTreeItemState st) {
    if("".equals(path)){
      return;
    }
    // ignoring or removing the ignore of an item
    if (st == SourceTreeItemState.IGNORED) {
      applySameStateAllChildren(path, SourceTreeItemState.NORMAL, st);
    }
    if (st == SourceTreeItemState.MAPPED) {
      applySameStateAllChildren(path, SourceTreeItemState.NORMAL, st);
    }
    if (st == SourceTreeItemState.NORMAL && states.get(path) == SourceTreeItemState.IGNORED) {
      applySameStateAllChildren(path, SourceTreeItemState.IGNORED, st);
      verifyStateAncestors(path);
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
      verifyStateAncestors(path);
      if(item instanceof SourceTreeDirectory){
        ((SourceTreeDirectory) item).moveChildrenWrongState();
      }
    }

    Path parent = path.getParent();
    // move the modified children in the parent
    if (parent!=null && items.containsKey(parent) && items.get(parent) instanceof SourceTreeDirectory) {
      ((SourceTreeDirectory) items.get(parent)).moveChildrenWrongState();
    }
  }

  private static void applySameStateAllChildren(Path path, SourceTreeItemState previousState,
    SourceTreeItemState state) {
    if("".equals(path)){
      return;
    }
    states.put(path, state);

    if(Files.isDirectory(path)) {
      Map<Path, SourceTreeItemState> children = getAllChildren(path);
      for (Path child : children.keySet()) {
        if (states.get(child) == previousState) {
          states.put(child, state);
          // update the item
          if (items.containsKey(child)) {
            items.get(child).setState(state);
          }
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
      addPath(Paths.get(path), st);
  }

  /**
   * Adds a SourceTreeItem reference to the collection.
   * <p/>
   * <p>
   * If the item's path isn't in the collection, it's also added.
   * </p>
   *
   * @param item
   *          The item to be added to the collection.
   */
  public static void addItem(SourceTreeItem item) {
    String path = item.getPath();
    if("".equals(path)){
      return;
    }
    if (!states.containsKey(path)) {
      states.put(Paths.get(path), item.getState());
    }
    items.put(Paths.get(path), item);
  }

  /**
   * Used to get the state associated with a path.
   *
   * @param path
   *          The path used to get the state.
   * @return The path's associated state if the path is in the collection,
   *         otherwise NORMAL.
   */
  public static SourceTreeItemState getState(Path path) {
    SourceTreeItemState result = SourceTreeItemState.NORMAL;
    if (states.containsKey(path))
      result = states.get(path);
    else {
      Path parent = path.getParent();
      if(parent!=null){
        if(Files.isDirectory(parent)) {
          result = getStateWithoutAddingParents(parent);
          addPath(path, result);
        }
      }
    }
    return result;
  }

  public static SourceTreeItemState getStateWithoutFallback(Path path) {
    SourceTreeItemState result = null;
    if (states.containsKey(path))
      result = states.get(path);
    return result;
  }
  
  private static SourceTreeItemState getStateWithoutAddingParents(Path path){
    SourceTreeItemState result = SourceTreeItemState.NORMAL;
    if (states.containsKey(path))
      result = states.get(path);
    else {
      Path parent = path.getParent();
      if(parent!=null){
        if(Files.isDirectory(parent)) {
          result = getState(parent);
        }
      }
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
  public static SourceTreeItem getItem(Path path) {
    return items.get(path);
  }

  public static void removePathAndItem(Path path){
    states.remove(path);
    items.remove(path);

    Map<Path, SourceTreeItemState> children = getAllChildren(path);
    children.keySet().forEach(PathCollection::removePathAndItem);
  }

  private static void verifyStateAncestors(Path path) {
    while(path.getParent()!=null){
      path = path.getParent();
      boolean updated = true;
      if(states.containsKey(path)) {
         updated = verifyState(path);

        if (items.containsKey(path)) {
          SourceTreeDirectory dir = (SourceTreeDirectory) items.get(path);
          dir.moveChildrenWrongState();
        }
      }
      if(!updated){
        break;
      }
    }
  }

  /*
   * Returns true if the state was updated, false if the state was the same.
   */
  private static boolean verifyState(Path path) {
    if("".equals(path)){
      return false;
    }
    
    SourceTreeItemState currentState = null;
    SourceTreeItemState newState = null;
    if(states.containsKey(path)){
      currentState = states.get(path);
    }
    int normalItems = 0, ignoredItems = 0, mappedItems = 0;
    Map<Path, SourceTreeItemState> children = getDirectChildren(path);
    for (Path child : children.keySet()) {
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
        newState = SourceTreeItemState.MAPPED;
        states.put(path, SourceTreeItemState.MAPPED);
      }
      // only IGNORED items, the directory is IGNORED
      if (mappedItems == 0 && ignoredItems != 0) {
        newState = SourceTreeItemState.IGNORED;
        states.put(path, SourceTreeItemState.IGNORED);
      }
      // IGNORED and MAPPED items, the directory is MAPPED
      if (mappedItems != 0 && ignoredItems != 0) {
        newState = SourceTreeItemState.MAPPED;
        states.put(path, SourceTreeItemState.MAPPED);
      }
    } else { // there's at least one NORMAL item, so the directory must be
      // NORMAL
      newState = SourceTreeItemState.NORMAL;
      states.put(path, SourceTreeItemState.NORMAL);
    }

    if (items.containsKey(path)) {
      SourceTreeItem item = items.get(path);
      item.setState(states.get(path));
    }
    
    if(currentState!=null && newState!=null){
      return currentState!=newState;
    }
    return true;
  }

  private static Map<Path, SourceTreeItemState> getDirectChildren(Path path) {
    int countSeparators = StringUtils.countMatches(path.toString(), File.separator) + 1;
    return states.entrySet().stream().parallel()
      .filter(
        p -> p.getKey().startsWith(path) && StringUtils.countMatches(p.getKey().toString(), File.separator) == countSeparators)
      .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }

  private static Map<Path, SourceTreeItemState> getAllChildren(Path path) {
    return states.entrySet().stream().parallel().filter(p -> p.getKey().startsWith(path))
      .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }

  public static void reset() {
    states = new HashMap<>();
    items = new HashMap<>();
  }
}