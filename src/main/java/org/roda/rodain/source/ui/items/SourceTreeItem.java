package org.roda.rodain.source.ui.items;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.control.TreeItem;

import org.roda.rodain.rules.Rule;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */

public class SourceTreeItem extends TreeItem<String>implements Observer {
  private String path;
  protected SourceTreeItemState state;
  protected SourceTreeDirectory parent;

  protected SourceTreeItem(String path, SourceTreeDirectory parent) {
    this.path = path;
    this.parent = parent;
    state = SourceTreeItemState.IGNORED;
  }

  public String getPath() {
    return path;
  }

  public SourceTreeItemState getState() {
    return state;
  }

  public void setState(SourceTreeItemState st) {
    if (st != state) {
      state = st;
    }
    forceUpdate();
  }

  public SourceTreeDirectory getParentDir() {
    return parent;
  }

  public void addIgnore() {

  }

  public void removeIgnore() {

  }

  public void addMapping(Rule r) {

  }

  public void removeMapping(Rule r) {

  }

  public void forceUpdate() {
    String value = getValue();
    if (value != null && !"".equals(value)) {
      setValue("");
      setValue(value);
    }
  }

  @Override
  public void update(Observable o, Object arg) {
  }
}
