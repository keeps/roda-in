package org.roda.rodain.source.ui;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;

import org.roda.rodain.source.ui.items.SourceTreeDirectory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-09-2015.
 */
public class ExpandedEventHandler implements EventHandler<TreeItem.TreeModificationEvent<Object>> {
    private SourceTreeDirectory source;

    @Override
    public void handle(TreeItem.TreeModificationEvent<Object> e) {
        source = SourceTreeDirectory.class.cast(e.getSource());

        // The event is triggered in the item and all its parents until the root,
        // so we set an additional control variable to only execute the desired code once
        if(source.expanded)
            return;
        source.expanded = true;

        // We only load new items if this hasn't been done before
        if(!source.getDirectory().hasFirstLoaded())
            source.loadMore();
    }
}
