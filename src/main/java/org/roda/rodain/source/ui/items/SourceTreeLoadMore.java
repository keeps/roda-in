package org.roda.rodain.source.ui.items;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.roda.rodain.core.I18n;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoadMore extends SourceTreeItem {
  public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/list-add.png"));

  public SourceTreeLoadMore() {
    super(I18n.t("SourceTreeLoadMore.title"), null);
    this.setGraphic(new ImageView(fileImage));
  }

  /**
   * @return Nullm because this item has no path
   */
  @Override
  public String getPath() {
    return null;
  }

  /**
   * @return The NORMAL state
   */
  @Override
  public SourceTreeItemState getState() {
    return SourceTreeItemState.NORMAL;
  }
}
