package source.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javafx.util.Callback;
import org.slf4j.LoggerFactory;

import source.representation.SourceDirectory;
import source.ui.items.SourceTreeCell;
import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeItem;
import utils.Utils;
import utils.WalkFileTree;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class FileExplorerPane extends BorderPane implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileExplorerPane.class.getName());
    private HBox top;
    private StackPane fileExplorer;
    private TreeView<String> treeView;
    private Stage stage;

    private GridPane metadata;
    private Label l_title, l_type, l_content, l_path, l_metadata;
    private CheckBox toggleFiles;
    private ComputeDirectorySize computeSize;

    //This thread is used to walk a directory's file tree and update the UI periodically with the size and file count
    private WalkFileTree computeThread;

    public FileExplorerPane(Stage stage){
        super();

        this.stage = stage;

        createTop();
        createFileExplorer();
        createMetadata();

        SplitPane split  = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(fileExplorer, metadata);

        this.setTop(top);
        this.setCenter(split);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.25));
    }

    private void createTop(){
        Button btn = new Button("Open Folder");
        toggleFiles = new CheckBox("Show Files");
        Label title = new Label("Source File Explorer");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        top = new HBox();
        top.setPadding(new Insets(10, 10, 10, 10));
        top.setSpacing(10);
        top.setAlignment(Pos.TOP_RIGHT);
        top.getChildren().addAll(title, space, toggleFiles, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null) return;
                Path path = selectedDirectory.toPath();
                boolean showFiles = toggleFiles.isSelected();
                setFileExplorerRoot(path, showFiles);
            }
        });

        toggleFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                TreeItem<String> root = treeView.getRoot();
                if (root == null) return;
                if (!(root instanceof SourceTreeDirectory)) return;
                SourceTreeDirectory rootCasted = (SourceTreeDirectory) root;
                String pathString = rootCasted.getPath();
                Path path = Paths.get(pathString);
                setFileExplorerRoot(path, new_val);
            }
        });
    }

    private void createFileExplorer(){
        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        treeView = new TreeView<String>();
        treeView.setStyle("-fx-background-color:white;");
        // add everything to the tree pane
        treeBox.getChildren().addAll(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        treeView.setCellFactory((new Callback<TreeView<String>, TreeCell<String>>() {
            public TreeCell<String> call(TreeView<String> p) {
                return new SourceTreeCell();
            }
        }));

        fileExplorer = new StackPane();
        fileExplorer.getChildren().add(treeBox);

        treeView.setOnMouseClicked(new SourceClickedEventHandler(this));
    }

    public void setFileExplorerRoot(Path rootPath, boolean showFiles){
        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath, showFiles));
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        updateMetadata(rootPath);
    }

    private void createMetadata(){
        metadata = new GridPane();
        metadata.setAlignment(Pos.TOP_LEFT);
        metadata.setHgap(10);
        metadata.setVgap(10);
        metadata.setPadding(new Insets(25, 25, 25, 25));

        Label title = new Label("Title");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(title, 0, 1);
        l_title = new Label();
        l_title.setWrapText(true);
        metadata.add(l_title, 1, 1);

        Label type = new Label("Type");
        type.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(type, 0, 2);
        l_type = new Label();
        l_type.setWrapText(true);
        metadata.add(l_type, 1, 2);

        Label content = new Label("Content");
        content.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(content, 0, 3);
        l_content = new Label();
        l_content.setWrapText(true);
        metadata.add(l_content, 1, 3);

        Label path = new Label("Path");
        path.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(path, 0, 4);
        l_path = new Label();
        l_path.setWrapText(true);
        metadata.add(l_path, 1, 4);

        Label metadataLabel = new Label("Metadata");
        metadataLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadataLabel.setMinWidth(100); //don't allow the label to minimize when the pane is shrunk
        metadata.add(metadataLabel, 0, 5);
        l_metadata = new Label();
        l_metadata.setWrapText(true);
        metadata.add(l_metadata, 1, 5);
    }

    public void updateMetadata(Path path){
        //we need to stop the directory size compute thread to avoid more than one thread updating the ui at the same time
        stopComputeThread();
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            String title;
            if(path.getFileName() != null) title = path.getFileName().toString();
            else title = path.toString();
            l_title.setText(title);
            l_path.setText(path.toString());
            l_metadata.setText("dummy.xml");

            if(attr.isDirectory()){
                l_type.setText("Directory");
                l_content.setText("");
                computeSize = new ComputeDirectorySize();
                computeSize.addObserver(this);
                computeThread = new WalkFileTree(path.toString(), computeSize);
                computeThread.start();
            }
            else{
                l_type.setText("File");
                l_content.setText(Utils.formatSize(attr.size()));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void updateMetadata(String pathString){
        Path path = Paths.get(pathString);
        updateMetadata(path);
    }
    
    public void update(Observable o, Object arg) {
        if(o == computeSize){
            updateSize(computeSize.getFilesCount(), computeSize.getDirectoryCount(), computeSize.getSize());
        }
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }

    private void stopComputeThread(){
        if(computeThread != null) computeThread.interrupt();
    }

    public void updateSize(final long fileCount, final long dirCount, final long size){
        Platform.runLater(new Runnable() {
            public void run() {
                StringBuilder result = new StringBuilder(dirCount + " ");
                if(dirCount == 1) result.append("directory");
                else result.append("directories");

                result.append(", ");

                result.append(fileCount).append(" ");
                if(fileCount == 1) result.append("file");
                else result.append("files");

                result.append(", ");
                result.append(Utils.formatSize(size));
                l_content.setText(result.toString());
                }
        });
    }

    public SourceTreeItem getSelectedItem(){
        if(treeView == null) return null;
        int selIndex = treeView.getSelectionModel().getSelectedIndex();
        if(selIndex == -1) return null;
        return (SourceTreeItem)treeView.getTreeItem(selIndex);
    }
}
