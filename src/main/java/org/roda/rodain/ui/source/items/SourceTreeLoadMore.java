package org.roda.rodain.ui.source.items;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.I18n;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoadMore extends SourceTreeItem {
  public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream(Constants.RSC_ICON_LIST_ADD));

  /**
   * Creates a new SourceTreeLoadMore object.
   */
  public SourceTreeLoadMore() {
    super(I18n.t(Constants.I18N_SOURCE_TREE_LOAD_MORE_TITLE), null);
    this.setGraphic(new ImageView(fileImage));
  }

  /**
   * @return Null because this item has no path
   */
  @Override
  public String getPath() {
    return null;
  }

  /**
   * @return The NORMAL state
   */
  @Override
  public PathState getState() {
    return PathState.NORMAL;
  }
}
