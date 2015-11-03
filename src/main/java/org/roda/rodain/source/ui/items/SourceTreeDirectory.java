package org.roda.rodain.source.ui.items;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import org.roda.rodain.source.representation.SourceDirectory;
import org.roda.rodain.source.representation.SourceItem;
import org.roda.rodain.source.ui.ExpandedEventHandler;
import org.roda.rodain.source.ui.FileExplorerPane;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeDirectory extends TreeItem<String> implements SourceTreeItem{
    public static final Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder.png"));
    public static final Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder-open.png"));
    public static final Comparator<? super TreeItem> comparator = createComparator();
    private SourceDirectory directory;
    public boolean expanded = false;
    private String fullPath;
    private SourceTreeItemState state;
    private String mappingRuleId;

    private HashSet<SourceTreeItem> ignored;
    private HashSet<SourceTreeItem> mapped;
    private HashSet<SourceTreeFile> files;

    public SourceTreeDirectory(Path file, SourceDirectory directory, SourceTreeItemState st){
        this(file, directory);
        state = st;
    }

    public SourceTreeDirectory(Path file, SourceDirectory directory) {
        super(file.toString());
        this.directory = directory;
        this.fullPath = file.toString();
        state = SourceTreeItemState.NORMAL;
        ignored = new HashSet<>();
        mapped = new HashSet<>();
        files = new HashSet<>();

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

    /**
     * Creates a task to hide all this item's mapped items.
     * The task is needed to prevent the UI thread from hanging due to the computations.
     *
     * First, it removes all the children with the MAPPED state and adds them to the mapped set, so that they can be
     * shown at a later date. If a child is a directory, this method is called in that item.
     * Finally, clears the children and adds the new list of items.
     *
     * @see #showMapped()
     */
    public void hideMapped(){
        final ArrayList<TreeItem<String>> newChildren = new ArrayList<>(getChildren());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Set<TreeItem> toRemove = new HashSet<>();
                for (TreeItem sti : newChildren) {
                    SourceTreeItem item = (SourceTreeItem) sti;
                    if (item.getState() == SourceTreeItemState.MAPPED) {
                        mapped.add(item);
                        toRemove.add(sti);
                    }
                    if (item instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) item).hideMapped();
                }
                newChildren.removeAll(toRemove);
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                getChildren().setAll(newChildren);
            }
        });

        new Thread(task).start();
    }

    /**
     * Creates a task to show all this item's mapped items.
     * The task is needed to prevent the UI thread from hanging due to the computations.
     *
     * First, it adds all the items in the mapped set, which are the previously hidden items, and clears the set. We need
     * to be careful in this step because if the hiddenFiles flag is true, then we must hide the mapped items that are files.
     * Then makes a call to this method for all its children and hidden ignored items.
     * Finally, clears the children, adds the new list of items, and sorts them.
     *
     * @see #sortChildren()
     * @see #hideMapped()
     */
    public void showMapped(){
        final ArrayList<TreeItem<String>> newChildren = new ArrayList<>(getChildren());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for(SourceTreeItem sti: mapped) {
                    if(sti instanceof SourceTreeFile && ! FileExplorerPane.isShowFiles()) {
                        files.add((SourceTreeFile) sti);
                    }else newChildren.add((TreeItem) sti);
                }
                mapped.clear();
                for(TreeItem sti: newChildren){
                    if(sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showMapped();
                }
                for(SourceTreeItem sti: ignored){
                    if(sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showMapped();
                }
                return null;
            }
        };

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                getChildren().setAll(newChildren);
                sortChildren();
            }
        });

        new Thread(task).start();
    }

    /**
     * Creates a task to hide all this item's ignored items.
     * The task is needed to prevent the UI thread from hanging due to the computations.
     *
     * First, it removes all the children with the IGNORED state and adds them to the ignored set, so that they can be
     * shown at a later date. If a child is a directory, this method is called in that item.
     * Then, calls this method for this item's children directories, that are in the hidden mapped items set.
     * Finally, clears the children and adds the new list of items.
     *
     * @see #showIgnored()
     */
    public void hideIgnored(){
        final ArrayList<TreeItem<String>> children = new ArrayList<>(getChildren());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Set<TreeItem> toRemove = new HashSet<>();
                for(TreeItem sti: children){
                    SourceTreeItem item = (SourceTreeItem) sti;
                    if (item.getState() == SourceTreeItemState.IGNORED) {
                        ignored.add(item);
                        toRemove.add(sti);
                    }
                    if(item instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) item).hideIgnored();
                }
                children.removeAll(toRemove);
                for(SourceTreeItem item: mapped){
                    if(item instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) item).hideIgnored();
                }
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                getChildren().setAll(children);
            }
        });

        new Thread(task).start();
    }

    /**
     * Creates a task to show all this item's ignored items.
     * The task is needed to prevent the UI thread from hanging due to the computations.
     *
     * First, it adds all the items in the ignored set, which are the previously hidden items, and clears the set. We need
     * to be careful in this step because if the hiddenFiles flag is true, then we must hide the ignored items that are files.
     * Then makes a call to this method for all its children and hidden mapped items.
     * Finally, clears the children, adds the new list of items, and sorts them.
     *
     * @see #sortChildren()
     * @see #hideIgnored()
     */
    public void showIgnored(){
        final ArrayList<TreeItem<String>> newChildren = new ArrayList<>(getChildren());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for(SourceTreeItem sti: ignored) {
                    if(sti instanceof SourceTreeFile && ! FileExplorerPane.isShowFiles()) {
                        files.add((SourceTreeFile) sti);
                    }else newChildren.add((TreeItem) sti);
                }
                ignored.clear();
                for(TreeItem sti: newChildren){
                    if(sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showIgnored();
                }
                for(SourceTreeItem sti: mapped){
                    if(sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showIgnored();
                }
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                getChildren().setAll(newChildren);
                sortChildren();
            }
        });
        new Thread(task).start();
    }

    /**
     * Creates a task to hide all this item's file items.
     * The task is needed to prevent the UI thread from hanging due to the computations.
     *
     * First, it removes all the children that are a file and adds them to the files set, so that they can be
     * shown at a later date. If a child is a directory, this method is called in that item.
     * Finally, clears the children and adds the new list of items.
     *
     * @see #showFiles() ()
     */
    public void hideFiles(){
        final ArrayList<TreeItem<String>> children = new ArrayList<>(getChildren());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Set<TreeItem> toRemove = new HashSet<>();
                for (TreeItem sti : children) {
                    if (sti instanceof SourceTreeFile) {
                        files.add((SourceTreeFile) sti);
                        toRemove.add(sti);
                    } else {
                        SourceTreeItem item = (SourceTreeItem) sti;
                        if (item instanceof SourceTreeDirectory)
                            ((SourceTreeDirectory) item).hideFiles();
                    }
                }
                children.removeAll(toRemove);
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                getChildren().setAll(children);
            }
        });
        new Thread(task).start();
    }

    /**
     * Creates a task to show all this item's file items.
     * The task is needed to prevent the UI thread from hanging due to the computations.
     *
     * First, it adds all the items in the files set, which are the previously hidden items, and clears the set.
     * Then makes a call to this method for all its children and hidden ignored/mapped items.
     * Finally, clears the children, adds the new list of items, and sorts them.
     *
     * @see #sortChildren()
     * @see #hideFiles() ()
     */
    public void showFiles(){
        final ArrayList<TreeItem<String>> newChildren = new ArrayList<>(getChildren());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (SourceTreeItem sti : files) {
                    newChildren.add((TreeItem) sti);
                }
                files.clear();
                for (TreeItem sti : newChildren) {
                    if (sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showFiles();
                }
                for (SourceTreeItem sti : ignored) {
                    if (sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showFiles();
                }
                for (SourceTreeItem sti : mapped) {
                    if (sti instanceof SourceTreeDirectory)
                        ((SourceTreeDirectory) sti).showFiles();
                }
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                getChildren().setAll(newChildren);
                sortChildren();
            }
        });
        new Thread(task).start();
    }

    public Set<String> getIgnored(){
        Set<String> result = new HashSet<>();
        //we need to include the items that are being shown and the hidden
        for(SourceTreeItem sti: ignored) {
           result.add(sti.getPath());
        }
        for(TreeItem sti: getChildren()) {
            SourceTreeItem item = (SourceTreeItem) sti;
            if(item instanceof SourceTreeDirectory)
                result.addAll(((SourceTreeDirectory)item).getIgnored());

            if (item.getState() == SourceTreeItemState.IGNORED)
                result.add(item.getPath());
        }
        return result;
    }

    public Set<String> getMapped(){
        Set<String> result = new HashSet<>();
        //we need to include the items that are being shown and the hidden
        for(SourceTreeItem sti: mapped) {
            result.add(sti.getPath());
        }
        for(TreeItem sti: getChildren()) {
            SourceTreeItem item = (SourceTreeItem) sti;
            if(item instanceof SourceTreeDirectory)
                result.addAll(((SourceTreeDirectory)item).getMapped());

            if (item.getState() == SourceTreeItemState.MAPPED)
                result.add(item.getPath());
        }
        return result;
    }

    private static Comparator createComparator(){
        return new Comparator<TreeItem>() {
            @Override
            public int compare(TreeItem o1, TreeItem o2) {
                if(o1.getClass() == o2.getClass()) { //sort items of the same class by value
                    String s1 = (String) o1.getValue();
                    String s2 = (String) o2.getValue();
                    return s1.compareToIgnoreCase(s2);
                }
                //directories must appear first
                if(o1 instanceof SourceTreeDirectory)
                    return -1;
                //"Load More..." item should be at the bottom
                if(o2 instanceof SourceTreeLoadMore)
                    return -1;
                return 1;
            }
        };
    }

    public void sortChildren(){
        ArrayList<TreeItem<String>> aux = new ArrayList<>(getChildren());
        Collections.sort(aux, comparator);
        getChildren().setAll(aux);
    }

    @Override
    public String getPath() {
        return this.fullPath;
    }

    @Override
    public SourceTreeItemState getState(){
        return state;
    }

    @Override
    public void addIgnore(){
        if(state == SourceTreeItemState.NORMAL)
            state = SourceTreeItemState.IGNORED;
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.addIgnore();
        }
    }

    @Override
    public void addMapping(String ruleId){
        if(state == SourceTreeItemState.NORMAL)
            state = SourceTreeItemState.MAPPED;
        mappingRuleId = ruleId;
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.addMapping(ruleId);
        }
    }

    @Override
    public void removeIgnore(){
        if (state == SourceTreeItemState.IGNORED)
            state = SourceTreeItemState.NORMAL;
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.removeIgnore();
        }
    }

    @Override
    public void removeMapping(String ruleId){
        if(state == SourceTreeItemState.MAPPED && mappingRuleId.equals(ruleId))
            state = SourceTreeItemState.NORMAL;
        mappingRuleId = "";
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.removeMapping(ruleId);
        }
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
                        addChild(children, sourceItem);
                    }
                    // check if there's more files to load
                    if (directory.isStreamOpen())
                        children.add(new SourceTreeLoadMore());
                }
                Collections.sort(children, comparator);
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

    private void addChild(List children, String sourceItem){
        //check if this path has been loaded and ignored/mapped. If it hasn't we apply the parent's state
        SourceTreeItemState newState = state;
        if(FileExplorerPane.isIgnored(sourceItem))
            newState = SourceTreeItemState.IGNORED;
        else if(FileExplorerPane.isMapped(sourceItem))
            newState = SourceTreeItemState.MAPPED;

        SourceTreeItem item;
        Path sourcePath = Paths.get(sourceItem);
        if (Files.isDirectory(sourcePath)) {
            item = new SourceTreeDirectory(sourcePath, directory.getChildDirectory(sourcePath), newState);
        } else item = new SourceTreeFile(sourcePath, newState);

        switch (newState){
            case IGNORED:
                addChildIgnored(children, item);
                break;
            case MAPPED:
                addChildMapped(children, item);
                break;
            case NORMAL:
                if(item instanceof SourceTreeFile)
                    if (FileExplorerPane.isShowFiles())
                        children.add(item);
                    else files.add((SourceTreeFile)item);
                else children.add(item);
                break;
            default:
        }
    }

    private void addChildIgnored(List children, SourceTreeItem item){
        if(FileExplorerPane.isShowIgnored()) {
            if(item instanceof SourceTreeFile && !FileExplorerPane.isShowFiles()) {
                files.add((SourceTreeFile) item);
            } else children.add(item);
        } else ignored.add(item);
    }

    private void addChildMapped(List children, SourceTreeItem item){
        if(FileExplorerPane.isShowMapped()) {
            if(item instanceof SourceTreeFile && !FileExplorerPane.isShowFiles()) {
                files.add((SourceTreeFile) item);
            } else children.add(item);
        } else mapped.add(item);
    }
}