package WithHandlers;

import Source.SourceDirectory;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by adrap on 16-09-2015.
 */
public class ExpandedEventHandler implements EventHandler<TreeItem.TreeModificationEvent<Object>> {
    public SourceTreeDirectory source;
    public void handle(TreeItem.TreeModificationEvent<Object> e) {
        source = (SourceTreeDirectory) e.getSource();
        if(source.expanded) return;
        source.expanded = true;
        if (source.isExpanded()) {
            ImageView iv = (ImageView) source.getGraphic();
            iv.setImage(FilePathTreeItem.folderExpandImage);
        }

        /*Task task = new Task<Void>() {
            @Override public Void call() {
                SourceDirectory dir = source.directory;
                int loaded = dir.loadMore();
                System.out.println("Loaded: " + loaded);

                source.getChildren().clear();
                for (String sourceItem : dir.getChildren().keySet()) {
                    Path sourcePath = Paths.get(sourceItem);
                    if (Files.isDirectory(sourcePath)) {
                        source.getChildren().add(new SourceTreeDirectory(sourcePath, dir.getChildDirectory(sourcePath)));
                    } else source.getChildren().add(new SourceTreeFile(sourcePath));
                }
                return null;
            }
        };
        new Thread(task).start();*/

        SourceDirectory dir = source.directory;
        int loaded = dir.firstLoad();
        System.out.println("Loaded: " + loaded);

        if(loaded != 0) {
            source.getChildren().clear();
            for (String sourceItem : dir.getChildren().keySet()) {
                Path sourcePath = Paths.get(sourceItem);
                if (Files.isDirectory(sourcePath)) {
                    source.getChildren().add(new SourceTreeDirectory(sourcePath, dir.getChildDirectory(sourcePath)));
                } else source.getChildren().add(new SourceTreeFile(sourcePath));
            }
            // check if there's more files to load
            if(dir.isStreamOpen())
                source.getChildren().add(new SourceTreeLoadMore());
        }
    }
}
