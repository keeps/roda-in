package rodain.source.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.slf4j.LoggerFactory;

import rodain.core.Footer;
import rodain.source.FilteredItemsBag;
import rodain.source.representation.SourceDirectory;
import rodain.source.ui.items.SourceTreeDirectory;
import rodain.source.ui.items.SourceTreeItem;
import rodain.source.ui.items.SourceTreeItemState;
import rodain.utils.Utils;
import rodain.utils.WalkFileTree;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class FileExplorerPane extends BorderPane implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileExplorerPane.class.getName());
    private HBox top;
    private StackPane fileExplorer;
    private TreeView<String> treeView;
    private Stage stage;

    private HBox filterButtons;
    private ComputeDirectorySize computeSize;

    private FilteredItemsBag filteredItems = new FilteredItemsBag();

    //Filter control
    public static boolean showFiles = false;
    public static boolean showIgnored = false;
    public static boolean showMapped = false;

    //This thread is used to walk a directory's file tree and update the UI periodically with the SIZE and file count
    private WalkFileTree computeThread;

    public FileExplorerPane(Stage stage){
        super();

        this.stage = stage;

        createTop();
        createFileExplorer();
        createFilterButtons();

        this.setTop(top);
        this.setCenter(fileExplorer);
        this.setBottom(filterButtons);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.33));
    }

    private void createTop(){
        Button btn = new Button("Open Folder");
        Label title = new Label("Source File Explorer");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        top = new HBox();
        top.setPadding(new Insets(10, 10, 10, 10));
        top.setSpacing(10);
        top.setAlignment(Pos.TOP_RIGHT);
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
        treeBox.setSpacing(10);

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
                setDragEvent(stage, cell);
                return cell;
            }
        });

        fileExplorer = new StackPane();
        fileExplorer.getChildren().add(treeBox);

        treeView.setOnMouseClicked(new SourceClickedEventHandler(this));
        treeView.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().getName().equalsIgnoreCase("DELETE")){
                    ignore();
                }
            }
        });
    }

    public void setFileExplorerRoot(Path rootPath){
        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath, showFiles));
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        updateMetadata(rootPath);
    }

    private void createFilterButtons(){
        filterButtons = new HBox();
        filterButtons.setPadding(new Insets(10, 10, 10, 10));

        final Button toggleFiles = new Button("Show Files");
        toggleFiles.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                TreeItem<String> root = treeView.getRoot();
                if (root == null)
                    return;
                if (!(root instanceof SourceTreeDirectory))
                    return;
                showFiles = !showFiles;
                if(showFiles)
                    toggleFiles.setText("Hide files");
                else toggleFiles.setText("Show files");

                SourceTreeDirectory rootCasted = (SourceTreeDirectory) root;
                String pathString = rootCasted.getPath();
                Path path = Paths.get(pathString);
                setFileExplorerRoot(path);
            }
        });

        final Button toggleIgnored = new Button ("Show ignored");
        toggleIgnored.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                showIgnored = !showIgnored;
                if(showIgnored) {
                    toggleIgnored.setText("Hide ignored");
                    /*Collection<String> ignored = filteredItems.getIgnored();
                    for(String key: ignored){
                        SourceTreeItem parent = filteredItems.getIgnoredParent(key);
                        TreeItem item = (TreeItem)filteredItems.getIgnored(key);
                        ObservableList<TreeItem> list = ((TreeItem)parent).getChildren();
                        //if(!filteredItems.isMapped(parent.getPath()))
                        list.add(item);
                    }*/
                    TreeItem root = treeView.getRoot();
                    SourceTreeDirectory dir = (SourceTreeDirectory)root;
                    dir.showIgnored();
                }
                else{
                    toggleIgnored.setText("Show ignored");
                    /*Collection<String> ignored = filteredItems.getIgnored();
                    for(String key: ignored){
                        TreeItem parent = (TreeItem)filteredItems.getIgnoredParent(key);
                        TreeItem item = (TreeItem)filteredItems.getIgnored(key);
                        ObservableList<TreeItem> list = parent.getChildren();
                        list.remove(item);
                    }*/
                    TreeItem root = treeView.getRoot();
                    SourceTreeDirectory dir = (SourceTreeDirectory)root;
                    dir.hideIgnored();
                }

                treeView.getRoot().setExpanded(false);
                treeView.getRoot().setExpanded(true);
            }
        });

        final Button toggleMapped = new Button ("Show mapped");
        toggleMapped.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                showMapped = !showMapped;
                if(showMapped) {
                    toggleMapped.setText("Hide mapped");
                    /*Collection<String> mapped = filteredItems.getMapped();

                    for(String key: mapped){
                        TreeItem parent = (TreeItem)filteredItems.getMappedParent(key);
                        TreeItem item = (TreeItem)filteredItems.getMapped(key);
                        ObservableList<TreeItem> list = parent.getChildren();
                        if(!list.contains(item))
                            list.add(item);
                    }*/
                    TreeItem root = treeView.getRoot();
                    SourceTreeDirectory dir = (SourceTreeDirectory)root;
                    dir.showMapped();
                }else{
                    toggleMapped.setText("Show mapped");
                    /*Collection<String> mapped = filteredItems.getMapped();
                    for(String key: mapped){
                        TreeItem parent = (TreeItem)filteredItems.getMappedParent(key);
                        TreeItem item = (TreeItem)filteredItems.getMapped(key);
                        ObservableList<TreeItem> list = parent.getChildren();
                        list.remove(item);
                    }*/
                    TreeItem root = treeView.getRoot();
                    SourceTreeDirectory dir = (SourceTreeDirectory)root;
                    dir.hideMapped();
                }

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
        } catch (IOException e) {
            log.error("" + e);
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

    private void setDragEvent(Stage stage, final SourceTreeCell cell){
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
            return null;
        Set<SourceTreeItem> result = new HashSet<>();
        for(TreeItem item: treeView.getSelectionModel().getSelectedItems()){
            result.add((SourceTreeItem)item);
        }
        return result;
    }

    public void map(){
        Set<SourceTreeItem> items = getSelectedItems();
        for(SourceTreeItem item: items){
            TreeItem treeItem = (TreeItem) item;
            item.map();
            SourceTreeItem parent = (SourceTreeItem) treeItem.getParent();
            filteredItems.map(item.getPath(), item, parent);

            SourceTreeDirectory dirParent = (SourceTreeDirectory)parent;
            if(!showMapped)
                //treeItem.getParent().getChildren().remove(treeItem);
                dirParent.hideMapped();
            else {//force update
                Object value = treeItem.getValue();
                treeItem.setValue(null);
                treeItem.setValue(value);
            }
        }
    }

    public void ignore(){
        Set<SourceTreeItem> items = getSelectedItems();
        for(SourceTreeItem item: items){
            TreeItem treeItem = (TreeItem) item;
            item.ignore();
            SourceTreeItem parent = (SourceTreeItem) treeItem.getParent();
            filteredItems.ignore(item.getPath(), item, parent);

            SourceTreeDirectory dirParent = (SourceTreeDirectory)parent;
            if(!showIgnored)
                //treeItem.getParent().getChildren().remove(treeItem);
                dirParent.hideIgnored();
            else {//force update
                Object value = treeItem.getValue();
                treeItem.setValue(null);
                treeItem.setValue(value);
            }
        }
    }
}
