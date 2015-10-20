package source.ui.items;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.SortedMap;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import source.representation.SourceDirectory;
import source.representation.SourceItem;
import source.ui.ExpandedEventHandler;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeDirectory extends TreeItem<String> implements SourceTreeItem{
    public static final Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder.png"));
    public static final Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder-open.png"));
    private SourceDirectory directory;
    public boolean expanded = false;
    //this stores the full path to the file or directory
    private String fullPath;
    private SourceTreeItemState state;

    public SourceTreeDirectory(Path file, SourceDirectory directory, SourceTreeItemState st){
        this(file, directory);
        state = st;
    }

    public SourceTreeDirectory(Path file, SourceDirectory directory) {
        super(file.toString());
        this.directory = directory;
        this.fullPath = file.toString();
        state = SourceTreeItemState.NORMAL;

        this.getChildren().add(new SourceTreeLoading());

        String value = file.toString();
        //set the value
        if (!fullPath.endsWith(File.separator)) {
            //set the value (which is what is displayed in the tree)

            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(value);
            }
        }


        this.addEventHandler(SourceTreeDirectory.branchExpandedEvent(), new ExpandedEventHandler());

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
            @Override
            public void handle(TreeItem.TreeModificationEvent<Object> e) {
                SourceTreeDirectory source = SourceTreeDirectory.class.cast(e.getSource());
                if (!source.isExpanded()) {
                    source.expanded = false;
                }
            }
        });
    }

    @Override
    public String getPath() {
        return (this.fullPath);
    }

    @Override
    public SourceTreeItemState getState(){
        return state;
    }

    @Override
    public void ignore(){
        if(state == SourceTreeItemState.NORMAL)
            state = SourceTreeItemState.IGNORED;
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.ignore();
        }
    }

    @Override
    public void map(){
        if(state == SourceTreeItemState.NORMAL)
            state = SourceTreeItemState.MAPPED;
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.map();
        }
    }

    @Override
    public void toNormal(){
        state = SourceTreeItemState.NORMAL;
    }

    public SourceDirectory getDirectory() {
        return directory;
    }

    /*
        * We need to create a task to load the items to a temporary collection, otherwise the UI will hang while we access the disk.
        * */
    public void loadMore(){
        final ArrayList<TreeItem<String>> children = new ArrayList<>(getChildren());

        // First we access the disk and save the loaded items to a temporary collection
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                SortedMap<String, SourceItem> loaded;
                loaded = getDirectory().loadMore();
                if (loaded.size() != 0) {
                    //Add new items
                    for (String sourceItem : loaded.keySet()) {
                        Path sourcePath = Paths.get(sourceItem);
                        if (Files.isDirectory(sourcePath)) {
                            children.add(new SourceTreeDirectory(sourcePath, directory.getChildDirectory(sourcePath), state));
                        } else children.add(new SourceTreeFile(sourcePath, state));
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
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                // Remove "loading" items
                ArrayList<Object> toRemove = new ArrayList<>();
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
