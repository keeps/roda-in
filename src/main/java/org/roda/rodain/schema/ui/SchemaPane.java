package org.roda.rodain.schema.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.ClassificationSchema;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class SchemaPane extends BorderPane {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SchemaPane.class.getName());
  private TreeView<String> treeView;
  private VBox treeBox;
  private SchemaNode rootNode;
  private HBox topBox, bottom;
  private VBox dropBox;
  private static Stage primaryStage;

  private ArrayList<SchemaNode> schemaNodes;

  // center help
  private VBox centerHelp;
  private boolean hasClassificationScheme = false;

  /**
   * Creates a new SchemaPane object.
   *
   * @param stage
   *          The primary stage of the application.
   */
  public SchemaPane(Stage stage) {
    super();
    primaryStage = stage;
    schemaNodes = new ArrayList<>();

    createTreeView();
    createTop();
    createBottom();

    createCenterHelp();
    this.setCenter(centerHelp);

    String lastClassScheme = AppProperties.getConfig("lastClassificationScheme");
    if (lastClassScheme != null && !"".equals(lastClassScheme)) {
      try {
        ClassificationSchema schema = loadClassificationSchemaFile(lastClassScheme);
        updateClassificationSchema(schema);
      } catch (IOException e) {
        log.error("Error reading classification scheme specification", e);
      }
    }

    this.prefWidthProperty().bind(stage.widthProperty().multiply(0.33));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
  }

  private void createTop() {
    Label title = new Label(AppProperties.getLocalizedString("SchemaPane.title"));
    title.getStyleClass().add("title");

    topBox = new HBox();
    topBox.setPadding(new Insets(10, 10, 10, 10));
    topBox.setAlignment(Pos.CENTER_LEFT);
    topBox.getChildren().add(title);
  }

  private void createCenterHelp() {
    centerHelp = new VBox();
    centerHelp.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(centerHelp, Priority.ALWAYS);
    centerHelp.setAlignment(Pos.CENTER);

    VBox box = new VBox(40);
    box.setPadding(new Insets(22, 10, 10, 10));
    box.setMaxWidth(355);
    box.setMaxHeight(200);
    box.setMinHeight(200);

    createDropBox();

    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    Label title = new Label("2 . " + AppProperties.getLocalizedString("SchemaPane.help.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    HBox loadBox = new HBox();
    loadBox.setAlignment(Pos.CENTER);
    Button load = new Button(AppProperties.getLocalizedString("load"));
    load.setMinHeight(65);
    load.setMinWidth(130);
    load.setMaxWidth(130);
    load.setOnAction(event -> loadClassificationSchema());
    load.getStyleClass().add("helpButton");
    loadBox.getChildren().add(load);

    Hyperlink link = new Hyperlink(AppProperties.getLocalizedString("SchemaPane.create"));
    link.setOnAction(event -> createClassificationScheme());

    TextFlow flow = new TextFlow(new Text(AppProperties.getLocalizedString("SchemaPane.or")), link);
    flow.setTextAlignment(TextAlignment.CENTER);

    box.getChildren().addAll(titleBox, loadBox);
    centerHelp.getChildren().addAll(box, flow);
  }

  private void createDropBox() {
    dropBox = new VBox();

    HBox innerBox = new HBox();
    VBox.setVgrow(innerBox, Priority.ALWAYS);
    innerBox.setAlignment(Pos.CENTER);
    innerBox.setMinHeight(200);

    Separator separatorTop = new Separator();
    Separator separatorBottom = new Separator();

    Label title = new Label(AppProperties.getLocalizedString("SchemaPane.dragHelp"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    innerBox.getChildren().add(title);
    dropBox.getChildren().addAll(separatorTop, innerBox, separatorBottom);

    dropBox.setOnDragOver(event -> {
      if (rootNode != null && event.getGestureSource() instanceof SourceTreeCell) {
        event.acceptTransferModes(TransferMode.COPY);
        title.setText(AppProperties.getLocalizedString("InspectionPane.onDrop"));
      }
      event.consume();
    });

    dropBox.setOnDragDropped(event -> {
      RodaIn.getSchemaPane().startAssociation(rootNode);
      event.consume();
    });

    dropBox.setOnDragExited(event -> {
      title.setText(AppProperties.getLocalizedString("SchemaPane.dragHelp"));
      event.consume();
    });
  }

  private void createTreeView() {
    // create tree pane
    treeBox = new VBox();
    VBox.setVgrow(treeBox, Priority.ALWAYS);

    DescriptionObject dobj = new DescriptionObject();
    rootNode = new SchemaNode(dobj);
    rootNode.setExpanded(true);
    rootNode.getChildren().addListener(new ListChangeListener<TreeItem<String>>() {
      @Override
      public void onChanged(Change<? extends TreeItem<String>> c) {
        if (rootNode.getChildren().isEmpty()) {
          setCenter(dropBox);
          RodaIn.getInspectionPane().showHelp();
        } else {
          setCenter(treeBox);
        }
      }
    });

    // create the tree view
    treeView = new TreeView<>(rootNode);
    VBox.setVgrow(treeView, Priority.ALWAYS);
    treeView.setShowRoot(false);
    treeView.setEditable(true);
    treeView.setCellFactory(param -> {
      SchemaTreeCell cell = new SchemaTreeCell();
      setDropEvent(cell);
      return cell;
    });

    Separator separatorTop = new Separator();
    Separator separatorBottom = new Separator();
    // add everything to the tree pane
    treeBox.getChildren().addAll(separatorTop, treeView, separatorBottom);
    treeView.setOnMouseClicked(new SchemaClickedEventHandler(this));
    treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem>() {
      @Override
      public void changed(ObservableValue observable, TreeItem oldValue, TreeItem newValue) {
        RodaIn.getInspectionPane().saveMetadata();
        if (oldValue instanceof SipPreviewNode) {
          oldValue.valueProperty().unbind();
          ((SipPreviewNode) oldValue).setBlackIconSelected(true);
          forceUpdate(oldValue);
        }
        if (oldValue instanceof SchemaNode) {
          oldValue.valueProperty().unbind();
          ((SchemaNode) oldValue).setBlackIconSelected(true);
          forceUpdate(oldValue);
        }
        if (newValue instanceof SipPreviewNode) {
          ((SipPreviewNode) newValue).setBlackIconSelected(false);
          forceUpdate(newValue);
          RodaIn.getInspectionPane().update((SipPreviewNode) newValue);
        }
        if (newValue instanceof SchemaNode) {
          ((SchemaNode) newValue).setBlackIconSelected(false);
          forceUpdate(newValue);
          RodaIn.getInspectionPane().update((SchemaNode) newValue);
        }
      }
    });
  }

  private void forceUpdate(TreeItem<String> item) {
    String value = item.getValue();
    item.setValue(null);
    item.setValue(value);
  }

  private TreeItem<String> getSelectedItem() {
    TreeItem<String> result = null;
    int selIndex = treeView.getSelectionModel().getSelectedIndex();
    if (selIndex != -1) {
      result = treeView.getTreeItem(selIndex);
    }
    return result;
  }

  /**
   * Creates a file chooser dialog so that the user can choose the
   * classification scheme file to be loaded. Then, loads the file and creates
   * the tree.
   */
  public void loadClassificationSchema() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(AppProperties.getLocalizedString("filechooser.title"));
    File selectedFile = chooser.showOpenDialog(primaryStage);
    if (selectedFile == null)
      return;
    String inputFile = selectedFile.toPath().toString();
    try {
      ClassificationSchema schema = loadClassificationSchemaFile(inputFile);
      updateClassificationSchema(schema);
    } catch (IOException e) {
      log.error("Error reading classification scheme specification", e);
    }
  }

  /**
   * Creates a ClassificationSchema object from the InputStream and builds a
   * tree using it.
   * 
   * @param stream
   *          The stream with the JSON file used to create the
   *          ClassificationSchema
   */
  public void loadClassificationSchemeFromStream(InputStream stream) {
    try {
      // create ObjectMapper instance
      ObjectMapper objectMapper = new ObjectMapper();
      // convert stream to object
      ClassificationSchema scheme = objectMapper.readValue(stream, ClassificationSchema.class);
      updateClassificationSchema(scheme);
    } catch (IOException e) {
      log.error("Error reading classification scheme from stream", e);
    }
  }

  private ClassificationSchema loadClassificationSchemaFile(String fileName) throws IOException {
    AppProperties.setConfig("lastClassificationScheme", fileName);
    AppProperties.saveConfig();
    InputStream input = new FileInputStream(fileName);

    // create ObjectMapper instance
    ObjectMapper objectMapper = new ObjectMapper();

    // convert json string to object
    return objectMapper.readValue(input, ClassificationSchema.class);
  }

  private void updateClassificationSchema(ClassificationSchema cs) {
    if (!confirmUpdate()) {
      return;
    }
    setTop(topBox);
    setCenter(treeBox);
    setBottom(bottom);
    rootNode.getChildren().clear();
    List<DescriptionObject> dos = cs.getDos();
    Map<String, SchemaNode> nodes = new HashMap<>();
    Set<SchemaNode> roots = new HashSet<>();

    for (DescriptionObject descObj : dos) {
      // Check if the node is a root node
      if (descObj.getParentId() == null) {
        // Create a new node if it hasn't been created
        if (!nodes.containsKey(descObj.getId())) {
          SchemaNode root = new SchemaNode(descObj);
          nodes.put(descObj.getId(), root);
        }
        roots.add(nodes.get(descObj.getId()));
      } else {
        // Get a list with the items where the id equals the node's parent's id
        List<DescriptionObject> parents = dos.stream().filter(p -> p.getId().equals(descObj.getParentId()))
          .collect(Collectors.toList());
        // If the input file is well formed, there should be one item in the
        // list, no more and no less
        if (parents.size() != 1) {
          String format = "The node \"%s\" has %d parents";
          String message = String.format(format, descObj.getTitle(), parents.size());
          log.warn("Error creating the scheme tree", new MalformedSchemaException(message));
          continue;
        }
        DescriptionObject parent = parents.get(0);
        SchemaNode parentNode;
        // If the parent node hasn't been processed yet, add it to the nodes map
        if (nodes.containsKey(parent.getId())) {
          parentNode = nodes.get(parent.getId());
        } else {
          parentNode = new SchemaNode(parent);
          nodes.put(parent.getId(), parentNode);
        }
        SchemaNode node;
        // If the node hasn't been added yet, create it and add it to the nodes
        // map
        if (nodes.containsKey(descObj.getId())) {
          node = nodes.get(descObj.getId());
        } else {
          node = new SchemaNode(descObj);
          nodes.put(descObj.getId(), node);
        }
        parentNode.getChildren().add(node);
        parentNode.addChildrenNode(node);
      }
    }

    // Add all the root nodes as children of the hidden rootNode
    for (SchemaNode sn : roots) {
      rootNode.getChildren().add(sn);
      schemaNodes.add(sn);
    }
    sortRootChildren();
    hasClassificationScheme = true;
  }

  private boolean confirmUpdate() {
    if (rootNode.getChildren().isEmpty()) {
      return true;
    }
    String content = AppProperties.getLocalizedString("SchemaPane.confirmNewScheme.content");
    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
    dlg.setHeaderText(AppProperties.getLocalizedString("SchemaPane.confirmNewScheme.header"));
    dlg.setTitle(AppProperties.getLocalizedString("SchemaPane.confirmNewScheme.title"));
    dlg.setContentText(content);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(primaryStage);
    dlg.showAndWait();

    if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      rootNode.remove();
      return true;
    } else
      return false;
  }

  private void sortRootChildren() {
    ArrayList<TreeItem<String>> aux = new ArrayList<>(rootNode.getChildren());
    Collections.sort(aux, new SchemaComparator());
    rootNode.getChildren().setAll(aux);
  }

  private void createBottom() {
    bottom = new HBox(10);
    bottom.setPadding(new Insets(10, 10, 10, 10));

    Button removeLevel = new Button(AppProperties.getLocalizedString("SchemaPane.remove"));
    removeLevel.setId("removeLevel");
    removeLevel.setMinWidth(100);
    removeLevel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        TreeItem<String> selected = getSelectedItem();
        if (selected != null) {
          if (selected instanceof SchemaNode) {
            SchemaNode node = (SchemaNode) selected;
            int fullSipCount = node.fullSipCount();
            if (fullSipCount != 0) {
              String content = String.format(AppProperties.getLocalizedString("SchemaPane.confirmRemove.content"),
                fullSipCount);
              Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
              dlg.setHeaderText(AppProperties.getLocalizedString("SchemaPane.confirmRemove.header"));
              dlg.setTitle(AppProperties.getLocalizedString("SchemaPane.confirmRemove.title"));
              dlg.setContentText(content);
              dlg.initModality(Modality.APPLICATION_MODAL);
              dlg.initOwner(primaryStage);
              dlg.show();
              dlg.resultProperty().addListener(o -> confirmRemove(selected, dlg.getResult()));
            } else
              removeNode(selected);
          } else if (selected instanceof SipPreviewNode) {
            SipPreview currentSIP = ((SipPreviewNode) selected).getSip();
            RuleModalController.removeSipPreview(currentSIP);
            Task<Void> removeTask = new Task<Void>() {
              @Override
              protected Void call() throws Exception {
                currentSIP.removeSIP();
                return null;
              }
            };
            new Thread(removeTask).start();
          }
        }
      }
    });

    Button addLevel = new Button(AppProperties.getLocalizedString("SchemaPane.add"));
    addLevel.setMinWidth(100);

    addLevel.setOnAction(event -> addNewLevel());

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Button export = new Button(AppProperties.getLocalizedString("export"));
    export.setMinWidth(100);
    export.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        RodaIn.exportSIPs();
      }
    });

    bottom.getChildren().addAll(removeLevel, addLevel, space, export);
  }

  private void confirmRemove(TreeItem<String> selected, ButtonType type) {
    if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      // remove all the rules under this SchemaNode
      ((SchemaNode) selected).remove();
      // remove the node from the tree
      removeNode(selected);
    }
  }

  private void removeNode(TreeItem<String> selected) {
    TreeItem parent = selected.getParent();
    parent.getChildren().remove(selected);
    schemaNodes.remove(selected);
  }

  public void createClassificationScheme() {
    if (!confirmUpdate()) {
      return;
    }
    setTop(topBox);
    // setCenter(treeBox);
    setCenter(dropBox);
    setBottom(bottom);
    rootNode.getChildren().clear();
    hasClassificationScheme = true;
  }

  private SchemaNode addNewLevel() {
    TreeItem<String> selectedItem = getSelectedItem();
    SchemaNode selected = null;
    if (selectedItem instanceof SchemaNode) {
      selected = (SchemaNode) selectedItem;
    }

    DescriptionObject dobj = new DescriptionObject();
    dobj.setId(UUID.randomUUID().toString());
    dobj.setTitle(AppProperties.getLocalizedString("SchemaPane.newNode"));
    dobj.setDescriptionlevel("series");
    SchemaNode newNode = new SchemaNode(dobj);
    if (selected != null) {
      dobj.setParentId(selected.getDob().getId());
      selected.getChildren().add(newNode);
      selected.addChildrenNode(newNode);
      selected.sortChildren();
      if (!selected.isExpanded())
        selected.setExpanded(true);
    } else {
      newNode.updateDescLevel("fonds");
      rootNode.getChildren().add(newNode);
      rootNode.addChildrenNode(newNode);
      sortRootChildren();
    }
    setCenter(treeBox);
    schemaNodes.add(newNode);
    // Edit the node's title as soon as it's created
    treeView.layout();
    treeView.edit(newNode);
    treeView.getSelectionModel().select(newNode);
    treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());

    return newNode;
  }

  public void startAssociation() {
    if (hasClassificationScheme) {
      TreeItem<String> selected = getSelectedItem();
      if (selected != null && selected instanceof SchemaNode)
        startAssociation((SchemaNode) selected);
      else
        startAssociation(rootNode);
    }
  }

  public void startAssociation(SchemaNode descObj) {
    Set<SourceTreeItem> sourceSet = RodaIn.getSourceSelectedItems();
    boolean valid = true;
    // both trees need to have 1 element selected
    if (sourceSet != null && !sourceSet.isEmpty() && descObj != null) {
      Set<SourceTreeItem> toRemove = new HashSet<>();
      for (SourceTreeItem source : sourceSet) {
        if (source.getState() != SourceTreeItemState.NORMAL) {
          toRemove.add(source);
          continue;
        }
        if (!(source instanceof SourceTreeDirectory || source instanceof SourceTreeFile)) {
          valid = false;
          break;
        }
      }
      sourceSet.removeAll(toRemove);
    } else
      valid = false;

    // we need to check the size again because we may have deleted some items in
    // the "for" loop
    if (sourceSet == null || sourceSet.isEmpty())
      valid = false;

    if (valid)
      RuleModalController.newAssociation(primaryStage, sourceSet, descObj);
  }

  private void setDropEvent(SchemaTreeCell cell) {
    setOnDragDetected(cell);
    setOnDragOver(cell);
    setOnDragEntered(cell);
    setOnDragExited(cell);
    setOnDragDropped(cell);
    setOnDragDone(cell);
  }

  private void setOnDragDetected(SchemaTreeCell cell) {
    cell.setOnDragDetected(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        TreeItem item = cell.getTreeItem();
        if (item instanceof SchemaNode) {
          if (item != null) {
            Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            String s = "scheme node - " + ((SchemaNode) item).getDob().getId();
            if (s != null) {
              content.putString(s);
              db.setContent(content);
            }
            event.consume();
          }
        }
      }
    });
  }

  private void setOnDragOver(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        TreeItem<String> treeItem = cell.getTreeItem();
        if (treeItem == null) {
          if (event.getGestureSource() instanceof SchemaNode)
            event.acceptTransferModes(TransferMode.MOVE);
          else
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        if (treeItem instanceof SchemaNode) {
          SchemaNode item = (SchemaNode) cell.getTreeItem();
          if (item != null && event.getGestureSource() != cell && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
          }
        }
        if (treeItem instanceof SipPreviewNode) {
          event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
      }
    });
  }

  private void setOnDragEntered(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragEntered(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        TreeItem<String> treeItem = cell.getTreeItem();
        if (treeItem instanceof SchemaNode) {
          SchemaNode item = (SchemaNode) cell.getTreeItem();
          if (item != null && event.getGestureSource() != cell && event.getDragboard().hasString()) {
            cell.getStyleClass().add("schemaNodeHovered");
          }
        }
        event.consume();
      }
    });
  }

  private void setOnDragExited(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragExited(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        cell.getStyleClass().remove("schemaNodeHovered");
        cell.updateItem(cell.getItem(), false);
        event.consume();
      }
    });
  }

  private void setOnDragDropped(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        TreeItem treeItem = cell.getTreeItem();
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
          // edit the classification scheme
          if (db.getString().startsWith("scheme")) {
            TreeItem selected = treeView.getSelectionModel().getSelectedItem();
            TreeItem parent = selected.getParent();
            parent.getChildren().remove(selected);
            SchemaNode node = (SchemaNode) treeItem;
            if (node == null) {
              rootNode.getChildren().add(selected);
              sortRootChildren();
            } else {
              node.getChildren().add(selected);
              node.sortChildren();
            }
          } else {
            if (treeItem != null) {
              // dropped on a SIP, associate to the parent of the SIP
              if (treeItem instanceof SipPreviewNode) {
                SipPreviewNode sipPreviewNode = (SipPreviewNode) treeItem;
                startAssociation((SchemaNode) sipPreviewNode.getParent());
              } else {
                // normal association
                startAssociation((SchemaNode) treeItem);
              }
            } else {
              // association to the empty tree view
              startAssociation(rootNode);
            }
          }
          success = true;
        }
        event.setDropCompleted(success);
        event.consume();
      }
    });
  }

  private void setOnDragDone(final SchemaTreeCell cell) {
    // on a Source
    cell.setOnDragDone(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
      }
    });
  }

  private Set<SchemaNode> recursiveGetSchemaNodes(TreeItem<String> root) {
    Set<SchemaNode> result = new HashSet<>();
    for (TreeItem<String> t : root.getChildren()) {
      if (t instanceof SchemaNode) {
        result.add((SchemaNode) t);
      }
      result.addAll(recursiveGetSchemaNodes(t));
    }
    return result;
  }

  public void showTree() {
    setCenter(treeBox);
  }

  /**
   * @return A set with all the SchemaNodes in the tree
   */
  public Set<SchemaNode> getSchemaNodes() {
    return recursiveGetSchemaNodes(rootNode);
  }

  /**
   * @return The TreeView of the SchemaPane
   */
  public TreeView<String> getTreeView() {
    return treeView;
  }

  /**
   * @return The Map with the SIPs of all the SchemaNodes in the TreeView
   */
  public Map<SipPreview, String> getSipPreviews() {
    schemaNodes.add(rootNode);
    Map<SipPreview, String> result = new HashMap<>();
    for (SchemaNode sn : schemaNodes) {
      result.putAll(sn.getSipPreviews());
    }
    return result;
  }
}
