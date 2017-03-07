package org.roda.rodain.ui.source.items;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Observable;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.ui.rules.Rule;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeFile extends SourceTreeItem {
  public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream(Constants.RSC_ICON_FILE));
  // this stores the full path to the file
  private String fullPath;

  private Rule rule;

  /**
   * Creates a new SourceTreeFile object.
   *
   * @param file
   *          The path that the new object will represent.
   * @param st
   *          The state the new object will have.
   * @param parent
   *          The parent item of the new object.
   */
  public SourceTreeFile(Path file, PathState st, SourceTreeDirectory parent) {
    this(file, parent);
    state = st;
    this.parent = parent;
  }

  /**
   * Creates a new SourceTreeFile object.
   *
   * @param file
   *          The path that the new object will represent.
   * @param parent
   *          The parent item of the new object.
   */
  public SourceTreeFile(Path file, SourceTreeDirectory parent) {
    super(file.toString(), parent);
    this.fullPath = file.toString();
    this.setGraphic(new ImageView(fileImage));

    // set the value
    if (!fullPath.endsWith(File.separator)) {
      // set the value (which is what is displayed in the tree)
      String value = file.toString();
      int indexOf = value.lastIndexOf(File.separator);
      if (indexOf > 0) {
        this.setValue(value.substring(indexOf + 1));
      } else {
        this.setValue(value);
      }
    }

    state = PathCollection.getState(Paths.get(fullPath));
  }

  /**
   * Sets the state of the item.
   *
   * @param st
   *          The new state.
   */
  @Override
  public void setState(PathState st) {
    if (state != st) {
      state = st;
    }
  }

  /**
   * Sets the item as ignored if the current state is NORMAL.
   */
  @Override
  public void addIgnore() {
    if (state == PathState.NORMAL) {
      state = PathState.IGNORED;
      PathCollection.addPath(Paths.get(fullPath), state);
    }
  }

  /**
   * Sets the item as mapped if the current state is NORMAL.
   *
   * @param r
   *          The rule that created a SIP which maps the item.
   */
  @Override
  public void addMapping(Rule r) {
    rule = r;
    if (state == PathState.NORMAL) {
      state = PathState.MAPPED;
    }
  }

  /**
   * Removes the ignored state of the item.
   */
  @Override
  public void removeIgnore() {
    if (state == PathState.IGNORED) {
      state = PathState.NORMAL;
      PathCollection.addPath(Paths.get(fullPath), state);
    }
  }

  /**
   * Removes the mapped state of the item
   *
   * @param r
   *          The rule that created a SIP which maps the item.
   */
  @Override
  public void removeMapping(Rule r) {
    if (rule == null || r == rule) {
      if (PathCollection.getState(Paths.get(fullPath)) == PathState.MAPPED) {
        state = PathState.NORMAL;
        PathCollection.addPath(Paths.get(fullPath), state);
      }
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
    if (o instanceof Rule && arg instanceof String && "Removed rule".equals(arg)) {
      Rule rul = (Rule) o;
      removeMapping(rul);
    }
  }
}
