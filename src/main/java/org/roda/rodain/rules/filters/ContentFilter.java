package org.roda.rodain.rules.filters;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public class ContentFilter {
  private HashSet<String> ignored;
  private HashSet<String> mapped;

  /**
   * Creates a new ContentFilter object
   */
  public ContentFilter() {
    ignored = new HashSet<>();
    mapped = new HashSet<>();
  }

  /**
   * Adds the path to the ignored paths list.
   *
   * @param st The path to be added to the ignored paths list.
   */
  public void addIgnored(String st) {
    ignored.add(st);
  }

  /**
   * Adds the all the paths in the collection to the ignored paths list.
   *
   * @param col The collection of paths to be added to the ignored paths list.
   */
  public void addAllIgnored(Collection col) {
    ignored.addAll(col);
  }

  /**
   * Adds the path to the mapped paths list.
   *
   * @param st The path to be added to the mapped paths list.
   */
  public void addMapped(String st) {
    mapped.add(st);
  }

  /**
   * Adds the all the paths in the collection to the mapped paths list.
   *
   * @param col The collection of paths to be added to the mapped paths list.
   */
  public void addAllMapped(Collection col) {
    mapped.addAll(col);
  }

  /**
   * Checks the ignored and mapped path lists and the PathCollection to
   * determine if the path should be filtered.
   * <p/>
   * <p>
   * Additionally, checks if any ancestor of the path is in one of the lists.
   * </p>
   *
   * @param st The path to be filtered
   * @return True if the path or any of its ancestors is in any of the lists, or
   * if the state of the path in the PathCollection isn't NORMAL, false
   * otherwise.
   */
  public boolean filter(String st) {
    boolean result = false;
    if (ignored.contains(st) || mapped.contains(st) || PathCollection.getState(st) != SourceTreeItemState.NORMAL) {
      result = true;
    } else {
      int index = 0, end = st.length(), fromIndex = 0;
      String separator = File.separator;

      while (index < end && !result) { // while we still have string to read and
        // haven't found a matching path
        index = st.indexOf(separator, fromIndex); // get the path until the
        // slash we're checking
        if (index == -1) {
          break;
        } else {
          String sub = st.substring(0, index);
          fromIndex = index + 1; // move the starting index for the next
          // iteration so it's after the slash
          if (ignored.contains(sub) || mapped.contains(sub)) {
            result = true;
          }
        }
      }
    }
    return result;
  }

}
