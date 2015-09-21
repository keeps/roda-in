package SourceUI.Items;

import SourceRepresentation.SourceDirectory;
import SourceRepresentation.SourceItem;
import SourceUI.ExpandedEventHandler;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceTreeDirectory extends TreeItem<String> {
    public static Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("folder.png"));
    public static Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("folder-open.png"));
    public SourceDirectory directory;
    public boolean expanded = false;
    //this stores the full path to the file or directory
    private String fullPath;

    public String getFullPath() {
        return (this.fullPath);
    }

    public SourceTreeDirectory(Path file, SourceDirectory directory) {
        super(file.toString());
        this.directory = directory;
        this.fullPath = file.toString();
        this.setGraphic(new ImageView(folderCollapseImage));

        this.getChildren().add(new SourceTreeLoading());

        //set the value
        if (!fullPath.endsWith(File.separator)) {
            //set the value (which is what is displayed in the tree)
            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(value);
            }
        }

        this.addEventHandler(TreeItem.branchExpandedEvent(), new ExpandedEventHandler());

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
            public void handle(TreeItem.TreeModificationEvent<Object> e) {
                SourceTreeDirectory source = (SourceTreeDirectory) e.getSource();
                if (!source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderCollapseImage);
                    source.expanded = false;
                }
            }
        });
    }

    /*
    * We need to create a task to load the items to a temporary collection, otherwise the UI will hang while we access the disk.
    * */
    public void loadMore(){
        final ArrayList<TreeItem<String>> children = new ArrayList<TreeItem<String>>(getChildren());

        // First we access the disk and save the loaded items to a temporary collection
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                TreeMap<String, SourceItem> loaded;
                loaded = directory.loadMore();

                if (loaded.size() != 0) {
                    //Add new items
                    for (String sourceItem : loaded.keySet()) {
                        Path sourcePath = Paths.get(sourceItem);
                        if (Files.isDirectory(sourcePath)) {
                            children.add(new SourceTreeDirectory(sourcePath, directory.getChildDirectory(sourcePath)));
                        } else children.add(new SourceTreeFile(sourcePath));
                    }
                    // check if there's more files to load
                    if (directory.isStreamOpen())
                        children.add(new SourceTreeLoadMore());
                }
                return loaded.size();
            }
        };

        // After everything is loaded, we add all the items to the TreeView at once.
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent workerStateEvent) {
                // Remove "loading" items
                ArrayList<Object> toRemove = new ArrayList<Object>();
                for(Object o: children)
                    if(o instanceof SourceTreeLoading)
                        toRemove.add(o);
                children.removeAll(toRemove);
                // Set the children
                getChildren().setAll(children);
            }
        });

        new Thread(task).start();
    }

}
