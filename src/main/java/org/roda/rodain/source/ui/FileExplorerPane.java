package org.roda.rodain.source.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.roda.rodain.core.Footer;
import org.roda.rodain.source.representation.SourceDirectory;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.Utils;
import org.roda.rodain.utils.WalkFileTree;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public class FileExplorerPane extends BorderPane implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileExplorerPane.class.getName());
    private HBox top;
    private StackPane fileExplorer;
    private TreeView<String> treeView;
    private Stage stage;

    private HBox filterButtons;
    private ComputeDirectorySize computeSize;

    private static Set<String> oldIgnored;
    private static Set<String> oldMapped;

    //Filter control
    private static boolean showFiles = true;
    private static boolean showIgnored = false;
    private static boolean showMapped = false;

    //This thread is used to walk a directory's file tree and update the UI periodically with the SIZE and file count
    private WalkFileTree computeThread;

    public FileExplorerPane(Stage stage){
        super();

        this.stage = stage;

        createTop();
        createFileExplorer();
        createFilterButtons();

        //setFileExplorerRoot(Paths.get("/home/adrap/Documents/Git/roda-in/"));

        this.setTop(top);
        this.setCenter(fileExplorer);
        this.setBottom(filterButtons);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.33));
    }

    public static boolean isShowFiles() {
        return showFiles;
    }

    public static boolean isShowIgnored() {
        return showIgnored;
    }

    public static boolean isShowMapped() {
        return showMapped;
    }

    private void createTop(){
        Button btn = new Button("Open Folder");
        Label title = new Label("Source File Explorer");
        title.setId("title");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        top = new HBox();
        top.setPadding(new Insets(10, 10, 10, 10));
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(title, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null)
                    return;
                Path path = selectedDirectory.toPath();
                setFileExplorerRoot(path);
            }
        });
    }

    private void createFileExplorer(){
        //create tree pane
        final VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));

        treeView = new TreeView<>();
        treeView.setStyle("-fx-background-color:white;");
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // add everything to the tree pane
        treeBox.getChildren().addAll(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                SourceTreeCell cell = new SourceTreeCell();
                setDragEvent(cell);
                return cell;
            }
        });

        fileExplorer = new StackPane();
        fileExplorer.getChildren().add(treeBox);

        treeView.setOnMouseClicked(new SourceClickedEventHandler(this));
        treeView.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if("DELETE".equalsIgnoreCase(event.getCode().getName())){
                    ignore();
                }
            }
        });
    }

    public void setFileExplorerRoot(Path rootPath){
        if(treeView.getRoot() != null) {
            oldIgnored = ((SourceTreeDirectory) treeView.getRoot()).getIgnored();
            oldMapped = ((SourceTreeDirectory) treeView.getRoot()).getMapped();
        }

        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath, isShowFiles()));
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        updateMetadata(rootPath);
    }

    private void createFilterButtons(){
        filterButtons = new HBox();
        filterButtons.setPadding(new Insets(10, 10, 10, 10));

        final Button toggleFiles = new Button("Hide Files");
        toggleFiles.minWidthProperty().bind(filterButtons.widthProperty().multiply(0.32));
        toggleFiles.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                SourceTreeDirectory root = getCastedRoot();
                if(root == null)
                    return;

                showFiles = !isShowFiles();
                if(isShowFiles()) {
                    toggleFiles.setText("Hide files");
                    root.showFiles();
                } else{
                    toggleFiles.setText("Show files");
                    root.hideFiles();
                }

                //force update
                treeView.getRoot().setExpanded(false);
                treeView.getRoot().setExpanded(true);
            }
        });

        final Button toggleIgnored = new Button ("Show ignored");
        toggleIgnored.minWidthProperty().bind(filterButtons.widthProperty().multiply(0.32));
        toggleIgnored.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                SourceTreeDirectory root = getCastedRoot();
                if(root == null)
                    return;

                showIgnored = !isShowIgnored();
                if(isShowIgnored()) {
                    toggleIgnored.setText("Hide ignored");
                    root.showIgnored();
                }
                else{
                    toggleIgnored.setText("Show ignored");
                    root.hideIgnored();
                }

                //force update
                treeView.getRoot().setExpanded(false);
                treeView.getRoot().setExpanded(true);
            }
        });

        final Button toggleMapped = new Button ("Show mapped");
        toggleMapped.minWidthProperty().bind(filterButtons.widthProperty().multiply(0.32));
        toggleMapped.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                SourceTreeDirectory root = getCastedRoot();
                if(root == null)
                    return;

                showMapped = !isShowMapped();
                if(isShowMapped()) {
                    toggleMapped.setText("Hide mapped");
                    root.showMapped();
                }else{
                    toggleMapped.setText("Show mapped");
                    root.hideMapped();
                }

                //force update
                treeView.getRoot().setExpanded(false);
                treeView.getRoot().setExpanded(true);
            }
        });

        HBox spaceLeft = new HBox();
        HBox.setHgrow(spaceLeft, Priority.ALWAYS);
        HBox spaceRight = new HBox();
        HBox.setHgrow(spaceRight, Priority.ALWAYS);

        filterButtons.getChildren().addAll(toggleFiles, spaceLeft, toggleIgnored, spaceRight, toggleMapped);
    }

    private SourceTreeDirectory getCastedRoot(){
        TreeItem<String> root = treeView.getRoot();
        if (root == null)
            return null;
        if (!(root instanceof SourceTreeDirectory))
            return null;
        return (SourceTreeDirectory) root;
    }

    public void updateMetadata(Path path){
        //we need to stop the directory size compute thread to avoid more than one thread updating the ui at the same time
        stopComputeThread();
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            if(attr.isDirectory()){
                computeSize = new ComputeDirectorySize();
                computeSize.addObserver(this);
                Set<String> singlePath = new HashSet<>();
                singlePath.add(path.toString());
                computeThread = new WalkFileTree(singlePath, computeSize);
                computeThread.start();
            }
            else updateSize(1, 0, attr.size()); //it's a file
        } catch (IOException e) {
            log.error("Error reading file attributes", e);
        }
    }

    public void updateMetadata(String pathString){
        Path path = Paths.get(pathString);
        updateMetadata(path);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o == computeSize){
            updateSize(computeSize.getFilesCount(), computeSize.getDirectoryCount(), computeSize.getSize());
        }
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }

    private void stopComputeThread(){
        if(computeThread != null)
            computeThread.interrupt();
    }

    public void updateSize(final long fileCount, final long dirCount, final long size){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                StringBuilder result = new StringBuilder(dirCount + " ");
                if(dirCount == 1)
                    result.append("directory");
                else result.append("directories");

                result.append(", ");

                result.append(fileCount).append(" ");
                if(fileCount == 1)
                    result.append("file");
                else result.append("files");

                result.append(", ");
                result.append(Utils.formatSize(size));
                Footer.setStatus(result.toString());
            }
        });
    }

    private void setDragEvent(final SourceTreeCell cell){
        // The drag starts on a gesture source
        cell.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                SourceTreeItem item = (SourceTreeItem) cell.getTreeItem();
                if (item != null && item.getState() == SourceTreeItemState.NORMAL) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    String s = item.getPath().toString();
                    content.putString(s);
                    db.setContent(content);
                    event.consume();
                }
            }
        });
    }

    public Set<SourceTreeItem> getSelectedItems(){
        if(treeView == null)
            return Collections.emptySet();
        Set<SourceTreeItem> result = new HashSet<>();
        for(TreeItem item: treeView.getSelectionModel().getSelectedItems()){
            result.add((SourceTreeItem)item);
        }
        return result;
    }

    public static boolean isIgnored(String item){
        if(oldIgnored == null)
            return false;
        return oldIgnored.contains(item);
    }

    public static boolean isMapped(String item){
        if(oldMapped == null)
            return false;
        return oldMapped.contains(item);
    }

    public void map(String ruleId){
        Set<SourceTreeItem> items = getSelectedItems();
        for(SourceTreeItem item: items){
            TreeItem treeItem = (TreeItem) item;
            item.map(ruleId);

            SourceTreeDirectory dirParent = (SourceTreeDirectory)treeItem.getParent();
            if(!isShowMapped())
                dirParent.hideMapped();
            else {//force update
                Object value = treeItem.getValue();
                treeItem.setValue(null);
                treeItem.setValue(value);
            }
        }
    }

    /*
    * Ignores or unignores the selected items, depending on its current state.
    * If an item is ignored, this method unignores it.
    * If an item is normal, this method ignores it.
    * In the same call it can ignore and unignore items.
    * */
    public void ignore(Set<SourceTreeItem> items){
        for(SourceTreeItem item: items){
            TreeItem treeItem = (TreeItem) item;
            if(item.getState() == SourceTreeItemState.NORMAL)
                item.ignore();
            else if(item.getState() == SourceTreeItemState.IGNORED)
                item.unignore();

            SourceTreeDirectory parent = (SourceTreeDirectory) treeItem.getParent();
            if(!isShowIgnored())
                parent.hideIgnored();
            else {//force update
                Object value = treeItem.getValue();
                treeItem.setValue(null);
                treeItem.setValue(value);
            }
        }
    }

    public void ignore(){
        Set<SourceTreeItem> items = getSelectedItems();
        ignore(items);
    }
}
