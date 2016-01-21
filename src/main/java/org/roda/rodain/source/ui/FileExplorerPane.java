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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.Footer;
import org.roda.rodain.core.PathCollection;
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
  private Stage stage;
  private HBox top;
  private StackPane fileExplorer;
  private TreeView<String> treeView;
  private HBox bottom;
  private VBox centerHelp;

  private ComputeDirectorySize computeSize;

  // Filter control
  private static boolean showFiles = true;
  private static boolean showIgnored = false;
  private static boolean showMapped = false;

  // This thread is used to walk a directory's file tree and update the UI
  // periodically with the SIZE and file count
  private WalkFileTree computeThread;

  public FileExplorerPane(Stage stage) {
    super();
    this.stage = stage;

    createCenterHelp();
    createTop();
    createFileExplorer();
    createBottom();

    this.setCenter(centerHelp);
    this.prefWidthProperty().bind(stage.widthProperty().multiply(0.32));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
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

  private void createTop() {
    Label title = new Label(AppProperties.getLocalizedString("FileExplorerPane.title"));
    title.getStyleClass().add("title");

    top = new HBox();
    top.setAlignment(Pos.CENTER_LEFT);
    top.setPadding(new Insets(10, 10, 10, 10));
    top.getChildren().add(title);
  }

  private void createBottom() {
    bottom = new HBox(10);
    bottom.setPadding(new Insets(10, 10, 10, 10));

    Button ignore = new Button(AppProperties.getLocalizedString("ignore"));
    ignore.setId("bt_ignore");
    ignore.setMinWidth(100);
    ignore.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        ignore();
      }
    });

    bottom.getChildren().add(ignore);
  }

  private void createCenterHelp() {
    centerHelp = new VBox();
    centerHelp.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(centerHelp, Priority.ALWAYS);
    centerHelp.setAlignment(Pos.CENTER);

    VBox box = new VBox(40);
    box.setPadding(new Insets(10, 10, 10, 10));
    box.setMaxWidth(355);
    box.setMaxHeight(200);
    box.setMinHeight(200);

    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    Label title = new Label("1. " + AppProperties.getLocalizedString("FileExplorerPane.help.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    HBox loadBox = new HBox();
    loadBox.setAlignment(Pos.CENTER);
    Button load = new Button(AppProperties.getLocalizedString("FileExplorerPane.chooseDir"));
    load.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        chooseNewRoot();
      }
    });
    load.setMinHeight(65);
    load.setMinWidth(220);
    load.setMaxWidth(220);
    load.getStyleClass().add("helpButton");
    loadBox.getChildren().add(load);

    box.getChildren().addAll(titleBox, loadBox);
    centerHelp.getChildren().add(box);
  }

  private void createFileExplorer() {
    // create tree pane
    final VBox treeBox = new VBox();
    Separator separatorTop = new Separator();
    Separator separatorBottom = new Separator();

    treeView = new TreeView<>();
    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    // add everything to the tree pane
    treeBox.getChildren().addAll(separatorTop, treeView, separatorBottom);
    VBox.setVgrow(treeView, Priority.ALWAYS);
    treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
      @Override
      public TreeCell<String> call(TreeView<String> p) {
        SourceTreeCell cell = new SourceTreeCell();
        setDragEvent(cell);
        return cell;
      }
    });
    treeView.getSelectionModel().clearSelection();

    fileExplorer = new StackPane();
    fileExplorer.getChildren().add(treeBox);

    treeView.setOnMouseClicked(new SourceClickedEventHandler(this));
  }

  public void chooseNewRoot() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(AppProperties.getLocalizedString("directorychooser.title"));
    File selectedDirectory = chooser.showDialog(stage);
    if (selectedDirectory == null)
      return;
    Path path = selectedDirectory.toPath();
    setFileExplorerRoot(path);
  }

  public void setFileExplorerRoot(Path rootPath) {
    this.setTop(top);
    this.setCenter(fileExplorer);
    this.setBottom(bottom);

    SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath, isShowFiles()),
        null);
    PathCollection.addItem(rootNode);
    rootNode.setExpanded(true);
    treeView.setRoot(rootNode);
    updateMetadata(rootPath);
  }

  private SourceTreeDirectory getCastedRoot() {
    TreeItem<String> root = treeView.getRoot();
    if (root == null)
      return null;
    if (!(root instanceof SourceTreeDirectory))
      return null;
    return (SourceTreeDirectory) root;
  }

  public void updateMetadata(Path path) {
    // we need to stop the directory size compute thread to avoid more than one
    // thread updating the ui at the same time
    stopComputeThread();
    try {
      BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
      if (attr.isDirectory()) {
        computeSize = new ComputeDirectorySize();
        computeSize.addObserver(this);
        Set<String> singlePath = new HashSet<>();
        singlePath.add(path.toString());
        computeThread = new WalkFileTree(singlePath, computeSize);
        computeThread.start();
      } else
        updateSize(1, 0, attr.size()); // it's a file
    } catch (IOException e) {
      log.error("Error reading file attributes", e);
    }
  }

  public void updateMetadata(String pathString) {
    Path path = Paths.get(pathString);
    updateMetadata(path);
  }

  @Override
  public void update(Observable o, Object arg) {
    if (o == computeSize) {
      updateSize(computeSize.getFilesCount(), computeSize.getDirectoryCount(), computeSize.getSize());
    }
  }

  public TreeView<String> getTreeView() {
    return treeView;
  }

  private void stopComputeThread() {
    if (computeThread != null)
      computeThread.interrupt();
  }

  public void updateSize(final long fileCount, final long dirCount, final long size) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        StringBuilder result = new StringBuilder();
        if (dirCount != 0) {
          result.append(dirCount + " ");
          if (dirCount == 1)
            result.append(AppProperties.getLocalizedString("directory"));
          else
            result.append(AppProperties.getLocalizedString("directories"));
          result.append(", ");
        }

        result.append(fileCount).append(" ");
        if (fileCount == 1)
          result.append(AppProperties.getLocalizedString("file"));
        else
          result.append(AppProperties.getLocalizedString("files"));

        result.append(", ");
        result.append(Utils.formatSize(size));
        Footer.setStatus(result.toString());
      }
    });
  }

  private void setDragEvent(final SourceTreeCell cell) {
    // The drag starts on a gesture source
    cell.setOnDragDetected(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        SourceTreeItem item = (SourceTreeItem) cell.getTreeItem();
        if (item != null && item.getState() == SourceTreeItemState.NORMAL) {
          Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
          ClipboardContent content = new ClipboardContent();
          String s = "source node - " + item.getPath();
          if (s != null) {
            content.putString(s);
            db.setContent(content);
          }
          event.consume();
        }
      }
    });
  }

  public Set<SourceTreeItem> getSelectedItems() {
    if (treeView == null)
      return Collections.emptySet();
    Set<SourceTreeItem> result = new HashSet<>();
    for (TreeItem item : treeView.getSelectionModel().getSelectedItems()) {
      result.add((SourceTreeItem) item);
    }
    return result;
  }

  /**
   * Ignores the items received in parameter.
   * If an item is normal, this method ignores it.
   * Depending on the state of the showIgnored flag, it shows or hides the
   * ignored items.
   * @param items The set of items to be ignored
   */
  public void ignore(Set<SourceTreeItem> items) {
    for (SourceTreeItem item : items) {
      if (item.getState() == SourceTreeItemState.NORMAL)
        item.addIgnore();

      SourceTreeDirectory parent = item.getParentDir();
      if (!isShowIgnored()) {
        if (parent != null) {
          parent.hideIgnored();
        }
        treeView.getSelectionModel().clearSelection();
      } else {// force update
        String value = item.getValue();
        item.setValue(null);
        item.setValue(value);
      }
    }
  }

  /**
   * Ignores the selected items.
   */
  public void ignore() {
    Set<SourceTreeItem> items = getSelectedItems();
    ignore(items);
  }

  public void toggleFilesShowing() {
    SourceTreeDirectory root = getCastedRoot();
    if (root == null)
      return;

    showFiles = !isShowFiles();
    if (isShowFiles()) {
      root.showFiles();
    } else {
      root.hideFiles();
    }

    // force update
    treeView.getRoot().setExpanded(false);
    treeView.getRoot().setExpanded(true);
  }

  public void toggleIgnoredShowing() {
    SourceTreeDirectory root = getCastedRoot();
    if (root == null)
      return;

    showIgnored = !isShowIgnored();
    if (isShowIgnored()) {
      root.showIgnored();
    } else {
      root.hideIgnored();
    }

    // force update
    treeView.getRoot().setExpanded(false);
    treeView.getRoot().setExpanded(true);
  }

  public void toggleMappedShowing() {
    SourceTreeDirectory root = getCastedRoot();
    if (root == null)
      return;

    showMapped = !isShowMapped();
    if (isShowMapped()) {
      root.showMapped();
    } else {
      root.hideMapped();
    }

    // force update
    treeView.getRoot().setExpanded(false);
    treeView.getRoot().setExpanded(true);
  }
}
