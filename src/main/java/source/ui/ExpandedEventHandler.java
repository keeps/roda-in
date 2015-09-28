package source.ui;

import javafx.scene.control.TreeItem;
import source.ui.items.SourceTreeDirectory;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;

/**
 * Created by adrapereira on 16-09-2015.
 */
public class ExpandedEventHandler implements EventHandler<TreeItem.TreeModificationEvent<Object>> {
    public SourceTreeDirectory source;
    public void handle(TreeItem.TreeModificationEvent<Object> e) {
        //TreeItem<String> item = e.getSource();
        System.out.println(e.getSource().getClass());

        source = (SourceTreeDirectory)e.getSource();

        // The event is triggered in the item and all its parents until the root,
        // so we set an additional control variable to only execute the desired code once
        if(source.expanded) return;
        source.expanded = true;

        if (source.isExpanded()) {
            ImageView iv = (ImageView) source.getGraphic();
            iv.setImage(SourceTreeDirectory.folderExpandImage);
        }

        // We only load new items if this hasn't been done before
        if(!source.directory.hasFirstLoaded())
            source.loadMore();
    }
}
