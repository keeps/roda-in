package org.roda.rodain.ui.source;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.source.ComputeDirectorySize;
import org.roda.rodain.core.source.representation.SourceDirectory;
import org.roda.rodain.core.utils.WalkFileTree;
import org.roda.rodain.ui.Footer;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.source.items.SourceTreeDirectory;
import org.roda.rodain.ui.source.items.SourceTreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public class FileExplorerPane extends BorderPane implements Observer {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileExplorerPane.class.getName());
  // 20170308 hsilva: disabled watchservice
  // public static final WatchService watcher = createWatcher();
  private Stage stage;
  private HBox top;
  private StackPane fileExplorer;
  private TreeView<String> treeView;
  private HBox bottom;
  private VBox centerHelp;
  private Button ignore, removeTopFolder;
  private HBox ignoreAndRemoveBox;

  private SourceTreeDirectory dummyRoot;
  private Map<String, SourceTreeDirectory> realRoots;
  private boolean selectedIsIgnored;

  // Threads
  private ComputeDirectorySize computeSize;
  private static DirectoryWatcher directoryWatcher;

  // Filter control
  private static boolean showFiles = true;
  private static boolean showIgnored = false;
  private static boolean showMapped = false;

  // This thread is used to walk a directory's file tree and update the UI
  // periodically with the SIZE and file count
  private WalkFileTree computeThread;

  /**
   * Creates a new FileExplorerPane object.
   *
   * @param stage
   *          The stage of the application.
   */
  public FileExplorerPane(Stage stage) {
    super();
    this.stage = stage;
    setPadding(new Insets(10, 10, 0, 10));

    createCenterHelp();
    createTop();
    createFileExplorer();
    createBottom();

    this.setCenter(centerHelp);
    this.setTop(top);
    this.prefWidthProperty().bind(stage.widthProperty().multiply(0.32));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
  }

  /**
   * @return True if the file explorer is showing files, false otherwise.
   */
  public static boolean isShowFiles() {
    return showFiles;
  }

  /**
   * @return True if the file explorer is showing ignored items, false
   *         otherwise.
   */
  public static boolean isShowIgnored() {
    return showIgnored;
  }

  /**
   * @return True if the file explorer is showing mapped items, false otherwise.
   */
  public static boolean isShowMapped() {
    return showMapped;
  }

  private void createTop() {
    Label title = new Label(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_TITLE).toUpperCase());
    title.getStyleClass().add(Constants.CSS_TITLE);

    top = new HBox(10);
    top.getStyleClass().add(Constants.CSS_TITLE_BOX);
    top.setAlignment(Pos.CENTER_LEFT);
    top.setPadding(new Insets(15, 15, 15, 15));
    top.getChildren().add(title);

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(top, new Tooltip(I18n.help("tooltip.fileExplorer")));
    }
  }

  private void createBottom() {
    bottom = new HBox(10);
    bottom.setPadding(new Insets(10, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER);

    ignore = new Button(I18n.t(Constants.I18N_IGNORE));
    ignore.setId("bt_ignore");
    ignore.setMinWidth(100);
    ignore.setOnAction(event -> {
      if (selectedIsIgnored) {
        List<TreeItem<String>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        if (selectedItems == null)
          return;
        for (TreeItem ti : selectedItems) {
          SourceTreeItem sti = (SourceTreeItem) ti;
          if (sti.getState() == PathState.IGNORED)
            sti.removeIgnore();
        }
        selectedIsIgnored = false;
        ignore.setText(I18n.t(Constants.I18N_IGNORE));
      } else {
        ignore();
        if (showIgnored) {
          selectedIsIgnored = true;
          ignore.setText(I18n.t(Constants.I18N_SOURCE_TREE_CELL_REMOVE));
        }
      }
    });

    removeTopFolder = new Button(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_REMOVE_FOLDER));
    removeTopFolder.setId("bt_removeTopFolder");
    removeTopFolder.setMinWidth(100);
    removeTopFolder.setOnAction(event -> {
      SourceTreeDirectory selectedItem = (SourceTreeDirectory) treeView.getSelectionModel().getSelectedItem();
      if (selectedItem == null)
        return;

      dummyRoot.getChildren().remove(selectedItem);
      realRoots.remove(selectedItem.getPath());
      if (realRoots.isEmpty()) {
        this.setTop(top);
        this.setCenter(centerHelp);
        this.setBottom(new HBox());
        ignoreAndRemoveBox.getChildren().remove(removeTopFolder);
      }
    });

    ignoreAndRemoveBox = new HBox(10);
    ignoreAndRemoveBox.getChildren().addAll(ignore);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Button associate = new Button(I18n.t(Constants.I18N_ASSOCIATE));
    associate.disableProperty().bind(RodaInApplication.getSchemePane().hasClassificationScheme().not());
    associate.setMinWidth(100);
    associate.setOnAction(event -> RodaInApplication.getSchemePane().startAssociation());

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(associate, new Tooltip(I18n.help("tooltip.associate")));
    }

    bottom.getChildren().addAll(ignoreAndRemoveBox, space, associate);
  }

  private void createCenterHelp() {
    centerHelp = new VBox();
    centerHelp.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(centerHelp, Priority.ALWAYS);
    centerHelp.setAlignment(Pos.CENTER);

    VBox box = new VBox(40);
    box.setPadding(new Insets(10, 10, 10, 10));
    box.setMaxWidth(400);
    box.setMaxHeight(200);
    box.setMinHeight(200);

    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    Label title = new Label(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_HELP_TITLE));
    title.setWrapText(true);
    title.getStyleClass().add(Constants.CSS_HELPTITLE);
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    HBox loadBox = new HBox();
    loadBox.setAlignment(Pos.CENTER);
    Button load = new Button(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_CHOOSE_DIR));
    load.setOnAction(event -> chooseNewRoot());
    load.setMinHeight(65);
    load.setMinWidth(220);
    load.setMaxWidth(220);
    load.getStyleClass().add(Constants.CSS_HELPBUTTON);
    loadBox.getChildren().add(load);

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(load, new Tooltip(I18n.help("tooltip.firstStep")));
    }

    box.getChildren().addAll(titleBox, loadBox);
    centerHelp.getChildren().add(box);
  }

  private void createFileExplorer() {
    // create tree pane
    final VBox treeBox = new VBox();
    treeBox.setPadding(new Insets(10, 0, 0, 0));
    Separator separatorBottom = new Separator();

    dummyRoot = new SourceTreeDirectory();
    realRoots = new HashMap<>();

    treeView = new TreeView<>();
    treeView.getStyleClass().add(Constants.CSS_MAIN_TREE);
    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    treeView.setShowRoot(false);
    treeView.setRoot(dummyRoot);
    // add everything to the tree pane
    treeBox.getChildren().addAll(treeView, separatorBottom);
    VBox.setVgrow(treeView, Priority.ALWAYS);
    treeView.setCellFactory(param -> {
      SourceTreeCell cell = new SourceTreeCell();
      setDragEvent(cell);
      return cell;
    });
    treeView.getSelectionModel().clearSelection();

    fileExplorer = new StackPane();
    fileExplorer.getChildren().add(treeBox);

    treeView.setOnMouseClicked(new SourceClickedEventHandler(this));

  }

  /**
   * Opens a DirectoryChooser so that the user can choose a new root for the
   * file explorer.
   */
  public void chooseNewRoot() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(I18n.t(Constants.I18N_DIRECTORY_CHOOSER_TITLE));
    File selectedDirectory = chooser.showDialog(stage);
    if (selectedDirectory == null)
      return;
    Path path = selectedDirectory.toPath();
    setFileExplorerRoot(path);
  }

  /**
   * Sets a new root to file explorer.
   *
   * @param rootPath
   *          The new root path
   */
  public void setFileExplorerRoot(Path rootPath) {
    // check if the path, a parent path or a child path haven't been added yet
    if (realRoots.containsKey(rootPath.toString())) {
      alertAddFolder(rootPath.toString(), rootPath.toString());
      return;
    }
    for (String pathString : realRoots.keySet()) {
      Path path = Paths.get(pathString);
      if (rootPath.startsWith(path) || path.startsWith(rootPath)) {
        alertAddFolder(rootPath.toString(), pathString);
        return;
      }
    }

    this.setTop(top);
    this.setCenter(fileExplorer);
    this.setBottom(bottom);

    SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath, isShowFiles()),
      null);
    realRoots.put(rootPath.toString(), rootNode);
    PathCollection.addItem(rootNode);
    rootNode.setExpanded(true);
    dummyRoot.getChildren().add(rootNode);
    Set<String> singlePath = new HashSet<>();
    singlePath.add(rootPath.toString());
    updateAttributes(singlePath);
  }

  private void alertAddFolder(String oldPath, String newPath) {
    String content = String.format(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_ALERT_ADD_FOLDER_CONTENT), oldPath,
      newPath);
    Alert dlg = new Alert(Alert.AlertType.INFORMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_ALERT_ADD_FOLDER_HEADER));
    dlg.setTitle(I18n.t(Constants.I18N_FILE_EXPLORER_PANE_ALERT_ADD_FOLDER_TITLE));
    dlg.setContentText(content);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);

    dlg.getDialogPane().setMinWidth(600);
    dlg.show();
  }

  public void updateAttributes() {
    ObservableList<TreeItem<String>> items = treeView.getSelectionModel().getSelectedItems();
    List<TreeItem<String>> copy = new ArrayList<>(items);
    Set<String> paths = new HashSet<>();
    if (copy != null && copy.size() > 0) {
      for (int i = 0; i < copy.size(); i++) {

        TreeItem<String> item = copy.get(i);
        if (item == null || ((SourceTreeItem) item).getPath() == null) {
          // LOGGER.error("AAA");
        } else {
          paths.add(((SourceTreeItem) item).getPath());
        }
      }
    }
    updateAttributes(paths);
  }

  /**
   * Updates the interface with the attributes of the selected items
   */
  private void updateAttributes(Set<String> paths) {
    // we need to stop the directory size compute thread to avoid more than one
    // thread updating the ui at the same time
    stopComputeThread();
    computeSize = new ComputeDirectorySize();
    computeSize.addObserver(this);
    computeThread = new WalkFileTree(paths, computeSize);
    computeThread.start();
  }

  @Override
  public void update(Observable o, Object arg) {
    if (o == computeSize) {
      ObservableList<TreeItem<String>> items = treeView.getSelectionModel().getSelectedItems();
      String start = null;
      if (!items.isEmpty()) {
        if (items.size() == 1 && items.get(0) != null) {
          start = items.get(0).getValue();
        } else {
          start = items.size() + " " + I18n.t(Constants.I18N_ITEMS);
        }
      }
      updateSize(start, computeSize.getFilesCount(), computeSize.getDirectoryCount(), computeSize.getSize());
    }
  }

  /**
   * Returns the tree view of the file explorer pane.
   *
   * @return
   */
  public TreeView<String> getTreeView() {
    return treeView;
  }

  private void stopComputeThread() {
    if (computeThread != null)
      computeThread.interrupt();
  }

  public void updateSize(final String start, final long fileCount, final long dirCount, final long size) {
    Platform.runLater(() -> {
      StringBuilder result = new StringBuilder();
      if (start != null)
        result.append(start).append(": ");

      if (dirCount != 0) {
        result.append(dirCount + " ");
        if (dirCount == 1)
          result.append(I18n.t(Constants.I18N_DIRECTORY));
        else
          result.append(I18n.t(Constants.I18N_DIRECTORIES));
        result.append(", ");
      }

      result.append(fileCount).append(" ");
      if (fileCount == 1)
        result.append(I18n.t(Constants.I18N_FILE));
      else
        result.append(I18n.t(Constants.I18N_FILES));

      result.append(", ");
      result.append(Controller.formatSize(size));
      Footer.setFileExplorerStatus(result.toString());
    });
  }

  private void setDragEvent(final SourceTreeCell cell) {
    // The drag starts on a gesture source
    cell.setOnDragDetected(event -> {
      SourceTreeItem item = (SourceTreeItem) cell.getTreeItem();
      if (item != null && item.getState() == PathState.NORMAL) {
        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        String s = "source node - " + item.getPath();
        if (s != null) {
          content.putString(s);
          db.setContent(content);
        }
        event.consume();
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

    Set<SourceTreeItem> toRemove = new HashSet<>();
    result.forEach(currentItem -> {
      Path currentItemPath = Paths.get(currentItem.getPath());
      Set<SourceTreeItem> ancestors = result.stream()
        .filter(p -> !currentItem.getPath().equals(p.getPath()) && currentItemPath.startsWith(Paths.get(p.getPath())))
        .collect(Collectors.toSet());
      if (ancestors != null && !ancestors.isEmpty())
        toRemove.add(currentItem);
    });
    result.removeAll(toRemove);

    return result;
  }

  /**
   * Ignores the items received in parameter. If an item is normal, this method
   * ignores it. Depending on the state of the showIgnored flag, it shows or
   * hides the ignored items.
   *
   * @param items
   *          The set of items to be ignored
   */
  public void ignore(Set<SourceTreeItem> items) {
    for (SourceTreeItem item : items) {
      if (item.getState() == PathState.NORMAL)
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
    if (realRoots == null || realRoots.isEmpty())
      return;

    for (SourceTreeDirectory root : realRoots.values()) {
      showFiles = !isShowFiles();
      if (isShowFiles()) {
        root.showFiles();
      } else {
        root.hideFiles();
      }
    }

    // force update
    treeView.getRoot().setExpanded(false);
    treeView.getRoot().setExpanded(true);
  }

  public void toggleIgnoredShowing() {
    if (realRoots == null || realRoots.isEmpty())
      return;

    showIgnored = !isShowIgnored();
    for (SourceTreeDirectory root : realRoots.values()) {
      if (isShowIgnored()) {
        root.showIgnored();
      } else {
        root.hideIgnored();
      }
    }

    // force update
    treeView.getRoot().setExpanded(false);
    treeView.getRoot().setExpanded(true);
  }

  public void toggleMappedShowing() {
    if (realRoots == null || realRoots.isEmpty())
      return;

    showMapped = !isShowMapped();
    for (SourceTreeDirectory root : realRoots.values()) {
      if (isShowMapped()) {
        root.showMapped();
      } else {
        root.hideMapped();
      }
    }

    // force update
    treeView.getRoot().setExpanded(false);
    treeView.getRoot().setExpanded(true);
  }

  /**
   * Adds or removes the "Remove folder" button when a top folder is selected or
   * not.
   * 
   * @param rootSelected
   */
  public void rootSelected(boolean rootSelected) {
    if (rootSelected) {
      if (!ignoreAndRemoveBox.getChildren().contains(removeTopFolder))
        ignoreAndRemoveBox.getChildren().add(removeTopFolder);
    } else {
      if (ignoreAndRemoveBox.getChildren().contains(removeTopFolder))
        ignoreAndRemoveBox.getChildren().remove(removeTopFolder);
    }
  }

  /**
   * Changes the text of the "Ignore" button to "Ignore" or "Remove ignore"
   * depending on whether the selected item is selected or not.
   * 
   * @param b
   */
  public void selectedIsIgnored(boolean b) {
    selectedIsIgnored = b;
    if (b) {
      ignore.setText(I18n.t(Constants.I18N_SOURCE_TREE_CELL_REMOVE));
    } else {
      ignore.setText(I18n.t(Constants.I18N_IGNORE));
    }
  }

  // 20170308 hsilva: disabled watchservice
  // private static WatchService createWatcher() {
  // WatchService watchService = null;
  // try {
  // watchService = FileSystems.getDefault().newWatchService();
  // directoryWatcher = new DirectoryWatcher();
  // directoryWatcher.start();
  // } catch (IOException e) {
  // LOGGER.warn(
  // "Can't create a WatchService. The application will be unable to update the
  // files of the file explorer", e);
  // }
  // return watchService;
  // }
  //
  // /**
  // * Calls the close() method to close the WatchService
  // */
  // public void closeWatcher() {
  // try {
  // watcher.close();
  // } catch (IOException e) {
  // LOGGER.debug("Error closing file explorer watcher", e);
  // }
  // }
}
