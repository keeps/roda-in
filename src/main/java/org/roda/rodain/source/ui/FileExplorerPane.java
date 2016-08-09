package org.roda.rodain.source.ui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.stream.Collectors;

import org.controlsfx.control.PopOver;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.Footer;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.source.representation.SourceDirectory;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.HelpToken;
import org.roda.rodain.utils.Utils;
import org.roda.rodain.utils.WalkFileTree;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
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

  private ComputeDirectorySize computeSize;

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
    Label title = new Label(I18n.t("FileExplorerPane.title").toUpperCase());
    title.getStyleClass().add("title");

    top = new HBox(10);
    top.getStyleClass().add("title-box");
    top.setAlignment(Pos.CENTER_LEFT);
    top.setPadding(new Insets(15, 15, 15, 15));
    top.getChildren().add(title);

    HelpToken titlePopOver = new HelpToken(I18n.help("fileExplorer"), PopOver.ArrowLocation.LEFT_TOP, 240);
    top.setOnMouseEntered(event -> {
      if(Boolean.parseBoolean(AppProperties.getAppConfig("app.helpEnabled")) && !titlePopOver.isShowing()) {
        titlePopOver.show(top);
      }
    });
    top.setOnMouseExited(event -> {
      if(titlePopOver.isShowing()) {
        titlePopOver.hide();
      }
    });
  }

  private void createBottom() {
    bottom = new HBox(10);
    bottom.setPadding(new Insets(10, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER);

    ignore = new Button(I18n.t("ignore"));
    ignore.setId("bt_ignore");
    ignore.setMinWidth(100);
    ignore.setOnAction(event -> {
      if (selectedIsIgnored) {
        List<TreeItem<String>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        if (selectedItems == null)
          return;
        for (TreeItem ti : selectedItems) {
          SourceTreeItem sti = (SourceTreeItem) ti;
          if (sti.getState() == SourceTreeItemState.IGNORED)
            sti.removeIgnore();
        }
        selectedIsIgnored = false;
        ignore.setText(I18n.t("ignore"));
      } else {
        ignore();
        if (showIgnored) {
          selectedIsIgnored = true;
          ignore.setText(I18n.t("SourceTreeCell.remove"));
        }
      }
    });

    removeTopFolder = new Button(I18n.t("FileExplorerPane.removeFolder"));
    removeTopFolder.setId("bt_removeTopFolder");
    removeTopFolder.setMinWidth(100);
    removeTopFolder.setOnAction( event -> {
      SourceTreeDirectory selectedItem = (SourceTreeDirectory) treeView.getSelectionModel().getSelectedItem();
      if (selectedItem == null)
        return;

      dummyRoot.getChildren().remove(selectedItem);
      realRoots.remove(selectedItem.getPath());
      if (realRoots.isEmpty()) {
        this.setTop(new HBox());
        this.setCenter(centerHelp);
        this.setBottom(new HBox());
        ignoreAndRemoveBox.getChildren().remove(removeTopFolder);
      }
    });

    ignoreAndRemoveBox = new HBox(10);
    ignoreAndRemoveBox.getChildren().addAll(ignore);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    HelpToken editPopOver = new HelpToken(I18n.help("associate"), PopOver.ArrowLocation.LEFT_BOTTOM, 125);

    Button associate = new Button(I18n.t("associate"));
    associate.disableProperty().bind(RodaIn.getSchemePane().hasClassificationScheme().not());
    associate.setMinWidth(100);
    associate.setOnAction(event -> RodaIn.getSchemePane().startAssociation());
    associate.setOnMouseEntered(event -> {
      if(Boolean.parseBoolean(AppProperties.getAppConfig("app.helpEnabled")) && !editPopOver.isShowing()) {
        editPopOver.show(associate);
      }
    });
    associate.setOnMouseExited(event -> {
      if(editPopOver.isShowing()) {
        editPopOver.hide();
      }
    });

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
    Label title = new Label("1. " + I18n.t("FileExplorerPane.help.title"));
    title.setWrapText(true);
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    HBox loadBox = new HBox();
    loadBox.setAlignment(Pos.CENTER);
    Button load = new Button(I18n.t("FileExplorerPane.chooseDir"));
    load.setOnAction(event -> chooseNewRoot());
    load.setMinHeight(65);
    load.setMinWidth(220);
    load.setMaxWidth(220);
    load.getStyleClass().add("helpButton");
    loadBox.getChildren().add(load);

    HelpToken loadPopOver = new HelpToken(I18n.help("firstStep"), PopOver.ArrowLocation.LEFT_CENTER, 105);
    load.setOnMouseEntered(event -> {
      if(Boolean.parseBoolean(AppProperties.getAppConfig("app.helpEnabled")) && !loadPopOver.isShowing()) {
        loadPopOver.show(load);
      }
    });
    load.setOnMouseExited(event -> {
      if(loadPopOver.isShowing()) {
        loadPopOver.hide();
      }
    });

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
    treeView.getStyleClass().add("main-tree");
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
    chooser.setTitle(I18n.t("directorychooser.title"));
    File selectedDirectory = chooser.showDialog(stage);
    if (selectedDirectory == null)
      return;
    Path path = selectedDirectory.toPath();
    setFileExplorerRoot(path);
  }

  /**
   * Sets a new root to file explorer
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
    String content = String.format(I18n.t("FileExplorerPane.alertAddFolder.content"), oldPath, newPath);
    Alert dlg = new Alert(Alert.AlertType.INFORMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t("FileExplorerPane.alertAddFolder.header"));
    dlg.setTitle(I18n.t("FileExplorerPane.alertAddFolder.title"));
    dlg.setContentText(content);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);

    dlg.getDialogPane().setMinWidth(600);
    dlg.show();
  }

  public void updateAttributes() {
    ObservableList<TreeItem<String>> items = treeView.getSelectionModel().getSelectedItems();
    Set<String> paths = new HashSet<>();
    if(items != null)
      items.forEach(sourceTreeItem -> paths.add(((SourceTreeItem) sourceTreeItem).getPath()));
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
          start = items.size() + " " + I18n.t("items");
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
          result.append(I18n.t("directory"));
        else
          result.append(I18n.t("directories"));
        result.append(", ");
      }

      result.append(fileCount).append(" ");
      if (fileCount == 1)
        result.append(I18n.t("file"));
      else
        result.append(I18n.t("files"));

      result.append(", ");
      result.append(Utils.formatSize(size));
      Footer.setFileExplorerStatus(result.toString());
    });
  }

  private void setDragEvent(final SourceTreeCell cell) {
    // The drag starts on a gesture source
    cell.setOnDragDetected(event -> {
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
      Set<SourceTreeItem> ancestors = result.stream().filter(p ->
              !currentItem.getPath().equals(p.getPath()) &&
              currentItem.getPath().startsWith(p.getPath())).collect(Collectors.toSet()
      );
      if(ancestors != null && !ancestors.isEmpty())
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

  public void rootSelected(boolean rootSelected) {
    if (rootSelected) {
      if(!ignoreAndRemoveBox.getChildren().contains(removeTopFolder))
        ignoreAndRemoveBox.getChildren().add(removeTopFolder);
    } else {
      if(ignoreAndRemoveBox.getChildren().contains(removeTopFolder))
        ignoreAndRemoveBox.getChildren().remove(removeTopFolder);
    }
  }

  public void selectedIsIgnored(boolean b) {
    selectedIsIgnored = b;
    if (b) {
      ignore.setText(I18n.t("SourceTreeCell.remove"));
    } else {
      ignore.setText(I18n.t("ignore"));
    }
  }
}
