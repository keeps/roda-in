package org.roda.rodain.source.ui.items;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoading extends SourceTreeItem {
  public SourceTreeLoading() {
    super("Loading...", null);
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
