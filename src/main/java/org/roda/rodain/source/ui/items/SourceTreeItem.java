package org.roda.rodain.source.ui.items;

import javafx.scene.control.TreeItem;
import org.roda.rodain.rules.Rule;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */

public class SourceTreeItem extends TreeItem<String> implements Observer {
  private String path;
  protected SourceTreeItemState state;
  protected SourceTreeDirectory parent;

  protected SourceTreeItem(String path, SourceTreeDirectory parent) {
    this.path = path;
    this.parent = parent;
    state = SourceTreeItemState.IGNORED;
    setValue(path);
  }

  /**
   * @return The path the item represents.
   */
  public String getPath() {
    return path;
  }

  /**
   * @return The current state of the item.
   * @see SourceTreeItemState
   */
  public SourceTreeItemState getState() {
    return state;
  }

  /**
   * Sets the state of the item and forces an update.
   * 
   * @param st
   *          The new state.
   */
  public void setState(SourceTreeItemState st) {
    if (st != state) {
      state = st;
    }
    forceUpdate();
  }

  /**
   * @return The parent of the item. This is *not* the same as getParent(), the
   *         value returned here is the SourceTreeDirectory received in the
   *         constructor.
   */
  public SourceTreeDirectory getParentDir() {
    return parent;
  }

  /**
   * Sets the item as ignored.
   */
  public void addIgnore() {

  }

  /**
   * Removes the ignored state of the item.
   */
  public void removeIgnore() {

  }

  /**
   * Sets the item as mapped.
   * 
   * @param r
   *          The rule that created a SIP which maps the item.
   */
  public void addMapping(Rule r) {

  }

  /**
   * Removes the mapped state of the item
   * 
   * @param r
   *          The rule that created a SIP which maps the item.
   */
  public void removeMapping(Rule r) {

  }

  /**
   * Forces a visual update of the item by setting the value as "" and back to
   * the original value.
   */
  public void forceUpdate() {
    String value = getValue();
    if (value != null && !"".equals(value)) {
      setValue("");
      setValue(value);
    }
  }

  /**
   * Called when an Observable object is changed.
   * 
   * @param o
   *          The Observable object.
   * @param arg
   *          The arguments sent by the Observable object.
   */
  @Override
  public void update(Observable o, Object arg) {
  }
}
