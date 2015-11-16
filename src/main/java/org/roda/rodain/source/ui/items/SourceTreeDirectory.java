package org.roda.rodain.source.ui.items;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.source.representation.SourceDirectory;
import org.roda.rodain.source.representation.SourceItem;
import org.roda.rodain.source.ui.ExpandedEventHandler;
import org.roda.rodain.source.ui.FileExplorerPane;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeDirectory extends SourceTreeItem{
    public static final Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder.png"));
    public static final Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder-open.png"));
    public static final Comparator<? super TreeItem> comparator = createComparator();

    public boolean expanded = false;
    private SourceDirectory directory;
    private String fullPath;
    private SourceTreeDirectory parent;

    private HashSet<SourceTreeItem> ignored;
    private HashSet<SourceTreeItem> mapped;
    private HashSet<SourceTreeFile> files;

    private Set<Rule> rules = new HashSet<>();

    public SourceTreeDirectory(Path file, SourceDirectory directory, SourceTreeItemState st, SourceTreeDirectory parent){
        this(file, directory, parent);
        state = st;
    }

    public SourceTreeDirectory(Path file, SourceDirectory directory, SourceTreeDirectory parent) {
        super(file.toString());
        this.directory = directory;
        this.fullPath = file.toString();
        this.parent = parent;
        state = SourceTreeItemState.NORMAL;
        ignored = new HashSet<>();
        mapped = new HashSet<>();
        files = new HashSet<>();

        this.getChildren().add(new SourceTreeLoading());

        String value = file.toString();
        if (!fullPath.endsWith(File.separator)) {
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
                    }else newChildren.add(sti);
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
        if(state == SourceTreeItemState.NORMAL) {
            state = SourceTreeItemState.IGNORED;
        }
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.addIgnore();
        }
        verifyState();
    }

    @Override
    public void addMapping(Rule r){
        rules.add(r);
        if(state == SourceTreeItemState.NORMAL) {
            state = SourceTreeItemState.MAPPED;
        }
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.addMapping(r);
        }
        verifyState();
    }

    @Override
    public void removeIgnore(){
        if (state == SourceTreeItemState.IGNORED) {
            state = SourceTreeItemState.NORMAL;
        }
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.removeIgnore();
        }
        verifyState();
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

                Set<Rule> allRules = getAllRules();
                if (loaded.size() != 0) {
                    //Add new items
                    for (String sourceItem : loaded.keySet()) {
                        addChild(children, sourceItem, allRules);
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

                verifyState();
            }
        });

        new Thread(task).start();
    }

    private void addChild(List children, String sourceItem, Set<Rule> allRules){
        //check if this path has been loaded and ignored/mapped. If it hasn't we apply the parent's state
        SourceTreeItemState newState = state;
        if(FileExplorerPane.isIgnored(sourceItem))
            newState = SourceTreeItemState.IGNORED;
        else if(FileExplorerPane.isMapped(sourceItem))
            newState = SourceTreeItemState.MAPPED;

        for(Rule rule: allRules){
            if(rule.isMapped(sourceItem))
                newState = SourceTreeItemState.MAPPED;
        }

        SourceTreeItem item;
        Path sourcePath = Paths.get(sourceItem);
        if (Files.isDirectory(sourcePath)) {
            item = new SourceTreeDirectory(sourcePath, directory.getChildDirectory(sourcePath), newState, this);
        } else item = new SourceTreeFile(sourcePath, newState, this);

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

    @Override
    public void removeMapping(Rule r){
        Set<String> removed = r.getRemoved();
        if(removed.contains(fullPath) && state == SourceTreeItemState.MAPPED) {
            state = SourceTreeItemState.NORMAL;
        }
        // check if this node is parent to any removed mappings. If it is, then set the state normal to keep integrity
        for(String s: removed){
            if(s.contains(fullPath)){
                state = SourceTreeItemState.NORMAL;
            }
        }
        /*for(Rule rule: rules) {
            if (!rule.isMapped(fullPath)) {
                state = SourceTreeItemState.NORMAL;
            }
        }*/


        // remove mappings in the children, ignored list, mapped list and files list
        for(TreeItem it: getChildren()){
            SourceTreeItem item = (SourceTreeItem)it;
            item.removeMapping(r);
        }
        for(SourceTreeItem it: ignored){
            it.removeMapping(r);
        }
        for(SourceTreeItem it: mapped){
            it.removeMapping(r);
        }
        for(SourceTreeItem it: files){
            it.removeMapping(r);
        }
        verifyState();
        parentVerify();
        moveChildrenWrongState();
    }

    /**
     * Verifies if the state of this item is right.
     * Example: A directory with state MAPPED must have all children mapped
     */
    public void verifyState(){
        int normalItems = 0, ignoredItems = 0, mappedItems = 0;
        boolean stateChanged = false;

        if(directory.isFirstLoaded()) {
            for (TreeItem it : getChildren()) {
                SourceTreeItem item = (SourceTreeItem) it;
                switch (item.getState()){
                    case MAPPED:
                        mappedItems++;
                        break;
                    case IGNORED:
                        ignoredItems++;
                        break;
                    default:
                        normalItems++;
                        break;
                }
            }
            for (SourceTreeItem sti : mapped) {
                if (sti.getState() == SourceTreeItemState.NORMAL) {
                    normalItems++;
                    mappedItems--;
                } else
                    mappedItems++;
            }

            if(normalItems == 0){
                if(mappedItems != 0) {
                    state = SourceTreeItemState.MAPPED;
                    stateChanged = true;
                }else{
                    state = SourceTreeItemState.IGNORED;
                    stateChanged = true;
                }
            }else {
                if (state == SourceTreeItemState.MAPPED) {
                    state = SourceTreeItemState.NORMAL;
                    stateChanged = true;
                } else if (state == SourceTreeItemState.IGNORED) {
                    state = SourceTreeItemState.NORMAL;
                    stateChanged = true;
                }
            }
        }

        forceUpdate();

        if(stateChanged){
            parentVerify();
            parentMoveChildrenWrongState();
        }

    }

    /**
     * @return a Set of this node's rules and the parent's rules, until the root
     */
    public Set<Rule> getAllRules(){
        Set<Rule> allRules = new HashSet<>();
        if(parent != null)
            allRules.addAll(parent.getAllRules());
        allRules.addAll(rules);

        return allRules;
    }

    private void parentVerify(){
        if(parent != null){
            parent.verifyState();
        }
    }

    public void moveChildrenWrongState(){
        Set<SourceTreeItem> toRemove = new HashSet<>();
        boolean modified = false;
        for (SourceTreeItem sti : mapped) {
            if (sti.getState() == SourceTreeItemState.NORMAL) {
                toRemove.add(sti);
                getChildren().add(sti);
            }
        }
        mapped.removeAll(toRemove);
        if(toRemove.size() != 0)
            modified = true;

        if(!FileExplorerPane.isShowMapped()) {
            toRemove = new HashSet<>();
            for (TreeItem ti : getChildren()) {
                SourceTreeItem sti = (SourceTreeItem) ti;
                if (sti.getState() == SourceTreeItemState.MAPPED) {
                    toRemove.add(sti);
                    mapped.add(sti);
                }
            }
            getChildren().removeAll(toRemove);
        }
        if(!FileExplorerPane.isShowIgnored()) {
            toRemove = new HashSet<>();
            for (TreeItem ti : getChildren()) {
                SourceTreeItem sti = (SourceTreeItem) ti;
                if (sti.getState() == SourceTreeItemState.IGNORED) {
                    toRemove.add(sti);
                    ignored.add(sti);
                }
            }
            getChildren().removeAll(toRemove);
        }
        if(toRemove.size() != 0 || modified)
            sortChildren();
    }

    private void parentMoveChildrenWrongState(){
        if(parent != null){
            parent.moveChildrenWrongState();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof Rule && arg != null && arg.toString().contains("Removed")){
            Rule rule = (Rule) o;
            removeMapping(rule);
            parent.removeMapping(rule);
            parentMoveChildrenWrongState();
        }
    }
}
