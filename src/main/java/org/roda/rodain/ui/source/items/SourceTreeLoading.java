package org.roda.rodain.ui.source.items;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.I18n;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoading extends SourceTreeItem {
  /**
   * Instantiates a new SourceTreeLoading object.
   */
  public SourceTreeLoading() {
    super(I18n.t(Constants.I18N_SOURCE_TREE_LOADING_TITLE), null);
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
