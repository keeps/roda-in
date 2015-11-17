package org.roda.rodain.source.ui.items;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeLoading extends SourceTreeItem{
    public SourceTreeLoading(){
        super("Loading...", null);
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public SourceTreeItemState getState(){
        return SourceTreeItemState.NORMAL;
    }
}
