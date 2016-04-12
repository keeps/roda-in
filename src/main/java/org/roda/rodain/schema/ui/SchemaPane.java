package org.roda.rodain.schema.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
import javafx.stage.StageStyle;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.Footer;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.sip.SipRepresentation;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.ClassificationSchema;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class SchemaPane extends BorderPane {
  private static final Logger log = LoggerFactory.getLogger(SchemaPane.class.getName());
  private TreeView<String> treeView;
  private VBox treeBox;
  private SchemaNode rootNode;
  private HBox topBox, bottom;
  private VBox dropBox;
  private static Stage primaryStage;
  private Set<SchemaNode> schemaNodes;

  private boolean modifiedPlan = false;

  // center help
  private VBox centerHelp;
  private BooleanProperty hasClassificationScheme;

  /**
   * Creates a new SchemaPane object.
   *
   * @param stage
   *          The primary stage of the application.
   */
  public SchemaPane(Stage stage) {
    super();
    setPadding(new Insets(10, 10, 0, 10));
    primaryStage = stage;
    schemaNodes = new HashSet<>();

    createTreeView();
    createTop();
    createBottom();

    createCenterHelp();
    this.setCenter(centerHelp);

    hasClassificationScheme = new SimpleBooleanProperty(false);

    String lastClassScheme = AppProperties.getConfig("lastClassificationScheme");
    if (lastClassScheme != null && !"".equals(lastClassScheme)) {
      try {
        ClassificationSchema schema = loadClassificationSchemaFile(lastClassScheme);
        updateClassificationSchema(schema, true);
      } catch (IOException e) {
        log.error("Error reading classification scheme specification", e);
      }
    }

    this.prefWidthProperty().bind(stage.widthProperty().multiply(0.33));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
  }

  private void createTop() {
    Label title = new Label(I18n.t("SchemaPane.title").toUpperCase());
    title.getStyleClass().add("title");

    topBox = new HBox();
    topBox.getStyleClass().add("title-box");
    topBox.setPadding(new Insets(15, 15, 15, 15));
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
    Label title = new Label("2 . " + I18n.t("SchemaPane.help.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    HBox loadBox = new HBox();
    loadBox.setAlignment(Pos.CENTER);
    Button load = new Button(I18n.t("load"));
    load.setMinHeight(65);
    load.setMinWidth(130);
    load.setMaxWidth(130);
    load.setOnAction(event -> loadClassificationSchema());
    load.getStyleClass().add("helpButton");
    loadBox.getChildren().add(load);

    Hyperlink link = new Hyperlink(I18n.t("SchemaPane.create"));
    link.setOnAction(event -> createClassificationScheme());

    TextFlow flow = new TextFlow(new Text(I18n.t("SchemaPane.or")), link);
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

    Label title = new Label(I18n.t("SchemaPane.dragHelp"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    innerBox.getChildren().add(title);
    dropBox.getChildren().addAll(separatorTop, innerBox, separatorBottom);

    dropBox.setOnDragOver(event -> {
      if (rootNode != null && event.getGestureSource() instanceof SourceTreeCell) {
        event.acceptTransferModes(TransferMode.COPY);
        title.setText(I18n.t("InspectionPane.onDrop"));
      }
      event.consume();
    });

    dropBox.setOnDragDropped(event -> {
      RodaIn.getSchemePane().startAssociation(rootNode);
      event.consume();
    });

    dropBox.setOnDragExited(event -> {
      title.setText(I18n.t("SchemaPane.dragHelp"));
      event.consume();
    });
  }

  private void createTreeView() {
    // create tree pane
    treeBox = new VBox();
    treeBox.setPadding(new Insets(10, 0, 0, 0));
    VBox.setVgrow(treeBox, Priority.ALWAYS);

    createRootNode();

    // create the tree view
    treeView = new TreeView<>(rootNode);
    treeView.getStyleClass().add("main-tree");
    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    VBox.setVgrow(treeView, Priority.ALWAYS);
    treeView.setShowRoot(false);
    treeView.setEditable(true);
    treeView.setCellFactory(param -> {
      SchemaTreeCell cell = new SchemaTreeCell();
      setDropEvent(cell);
      return cell;
    });

    Separator separatorBottom = new Separator();
    // add everything to the tree pane
    treeBox.getChildren().addAll(treeView, separatorBottom);
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

  private void updateFooter(TreeItem item) {
    StringBuilder sb = new StringBuilder();
    sb.append(item.getValue()).append(": ");
    if (item instanceof SipPreviewNode) {

    }
    Footer.setClassPlanStatus(sb.toString());
  }

  private void createRootNode() {
    DescriptionObject dobj = new DescriptionObject();
    dobj.setParentId(null);
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
    chooser.setTitle(I18n.t("filechooser.title"));
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
      rootNode.getChildren().clear();
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

    modifiedPlan = false;
    // convert json string to object
    return objectMapper.readValue(input, ClassificationSchema.class);
  }

  private void updateClassificationSchema(ClassificationSchema cs) {
    updateClassificationSchema(cs, false);
  }

  private void updateClassificationSchema(ClassificationSchema cs, boolean skipConfirmation) {
    if (!skipConfirmation)
      if (!confirmUpdate())
        return;

    setTop(topBox);
    setCenter(treeBox);
    setBottom(bottom);
    rootNode.getChildren().clear();
    List<DescriptionObject> dos = cs.getDos();
    Map<String, SchemaNode> nodes = new HashMap<>();
    Set<SchemaNode> roots = new HashSet<>();

    try {
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
          // Get a list with the items where the id equals the node's parent's
          // id
          List<DescriptionObject> parents = dos.stream().filter(p -> p.getId().equals(descObj.getParentId()))
            .collect(Collectors.toList());
          // If the input file is well formed, there should be one item in the
          // list, no more and no less
          if (parents.size() != 1) {
            String format = "The node \"%s\" has %d parents";
            String message = String.format(format, descObj.getTitle(), parents.size());
            log.info("Error creating the scheme tree", new MalformedSchemaException(message));
            continue;
          }
          DescriptionObject parent = parents.get(0);
          SchemaNode parentNode;
          // If the parent node hasn't been processed yet, add it to the nodes
          // map
          if (nodes.containsKey(parent.getId())) {
            parentNode = nodes.get(parent.getId());
          } else {
            parentNode = new SchemaNode(parent);
            nodes.put(parent.getId(), parentNode);
          }
          SchemaNode node;
          // If the node hasn't been added yet, create it and add it to the
          // nodes
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
      // if there were no nodes in the file, show the help panel
      if (roots.isEmpty()) {
        setTop(new HBox());
        setCenter(centerHelp);
        setBottom(new HBox());
      } else {
        sortRootChildren();
        hasClassificationScheme.setValue(true);
      }
      modifiedPlan = false;
    } catch (Exception e) {
      log.error("Error updating the classification plan", e);
    }
  }

  private boolean confirmUpdate() {
    if (rootNode.getChildren().isEmpty()) {
      return true;
    }
    String content = I18n.t("SchemaPane.confirmNewScheme.content");
    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t("SchemaPane.confirmNewScheme.header"));
    dlg.setTitle(I18n.t("SchemaPane.confirmNewScheme.title"));
    dlg.setContentText(content);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(primaryStage);
    dlg.showAndWait();

    if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      rootNode.remove();
      createRootNode();
      treeView.setRoot(rootNode);
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

    Button removeLevel = new Button(I18n.t("SchemaPane.remove"));
    removeLevel.setId("removeLevel");
    removeLevel.setMinWidth(100);
    removeLevel.setOnAction(event -> {
      List<TreeItem<String>> selectedItems = new ArrayList<>(treeView.getSelectionModel().getSelectedItems());
      Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
      dlg.initStyle(StageStyle.UNDECORATED);
      dlg.setHeaderText(I18n.t("SchemaPane.confirmRemove.header"));
      dlg.setTitle(I18n.t("SchemaPane.confirmRemove.title"));
      dlg.setContentText(I18n.t("SchemaPane.confirmRemove.content"));
      dlg.initModality(Modality.APPLICATION_MODAL);
      dlg.initOwner(primaryStage);
      dlg.show();
      dlg.resultProperty().addListener(o -> confirmRemove(selectedItems, dlg.getResult()));
    });

    Button addLevel = new Button(I18n.t("SchemaPane.add"));
    addLevel.setMinWidth(100);

    addLevel.setOnAction(event -> addNewLevel());

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Button export = new Button(I18n.t("export"));
    export.setMinWidth(100);
    export.setOnAction(event -> RodaIn.exportSIPs());

    bottom.getChildren().addAll(removeLevel, addLevel, space, export);
  }

  private void confirmRemove(List<TreeItem<String>> selectedItems, ButtonType type) {
    if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      treeView.getSelectionModel().clearSelection();
      Set<SipPreview> sipNodes = new HashSet<>();
      Set<SipPreview> fromRules = new HashSet<>();
      for (TreeItem<String> selected : selectedItems) {
        if (selected instanceof SipPreviewNode) {
          SipPreview currentSIP = ((SipPreviewNode) selected).getSip();
          sipNodes.add(currentSIP);
        }
        if (selected instanceof SchemaNode) {
          for (Rule r : ((SchemaNode) selected).getRules()) {
            fromRules.addAll(r.getSips());
          }
          // remove all the rules under this SchemaNode
          ((SchemaNode) selected).remove();
        }
        // remove the node from the tree
        removeNode(selected);
      }

      sipNodes.removeAll(fromRules);

      for (SipPreview currentSIP : sipNodes) {
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

  private void removeNode(TreeItem<String> selected) {
    TreeItem parent = selected.getParent();
    if (parent != null) {
      if (parent instanceof SchemaNode) {
        ((SchemaNode) parent).removeChild(selected);
        parent.getChildren().remove(selected);
      } else
        parent.getChildren().remove(selected);
    }
    schemaNodes.remove(selected);
    treeView.getSelectionModel().clearSelection();
  }

  public void createClassificationScheme() {
    if (!confirmUpdate()) {
      return;
    }
    setTop(topBox);
    setCenter(dropBox);
    setBottom(bottom);
    rootNode.getChildren().clear();
    hasClassificationScheme.setValue(true);
    AppProperties.setConfig("lastClassificationScheme", "");
    AppProperties.saveConfig();
    modifiedPlan = true;
  }

  private SchemaNode addNewLevel() {
    TreeItem<String> selectedItem = getSelectedItem();
    SchemaNode selected = null;
    if (selectedItem instanceof SchemaNode) {
      selected = (SchemaNode) selectedItem;
    }

    DescriptionObject dobj = new DescriptionObject();
    dobj.setId(UUID.randomUUID().toString());
    dobj.setTitle(I18n.t("SchemaPane.newNode"));
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
    treeView.getSelectionModel().clearSelection();
    treeView.getSelectionModel().select(newNode);
    treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());
    modifiedPlan = true;

    return newNode;
  }

  public void startAssociation() {
    if (hasClassificationScheme.get()) {
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
  }

  private void setOnDragDetected(SchemaTreeCell cell) {
    cell.setOnDragDetected(event -> {
      TreeItem item = cell.getTreeItem();
      Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      String s = "";
      if (item instanceof SchemaNode) {
        s = "scheme node - " + ((SchemaNode) item).getDob().getId();
      }
      if (item instanceof SipPreviewNode) {
        s = "sip preview - " + ((SipPreviewNode) item).getSip().getId();
      }
      content.putString(s);
      db.setContent(content);
      event.consume();
    });
  }

  private void setOnDragOver(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragOver(event -> {
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
    });
  }

  private void setOnDragEntered(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragEntered(event -> {
      TreeItem<String> treeItem = cell.getTreeItem();
      if (treeItem instanceof SchemaNode) {
        SchemaNode item = (SchemaNode) cell.getTreeItem();
        if (item != null && event.getGestureSource() != cell && event.getDragboard().hasString()) {
          cell.getStyleClass().add("schemaNodeHovered");
        }
      }
      event.consume();
    });
  }

  private void setOnDragExited(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragExited(event -> {
      cell.getStyleClass().remove("schemaNodeHovered");
      cell.updateItem(cell.getItem(), false);
      event.consume();
    });
  }

  private void setOnDragDropped(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragDropped(event -> {
      TreeItem treeItem = cell.getTreeItem();
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasString()) {
        // edit the classification scheme
        if (db.getString().startsWith("scheme")) {
          TreeItem selected = treeView.getSelectionModel().getSelectedItem();
          SchemaNode node = (SchemaNode) treeItem;

          // If the target item is a descendant of the source item, they would
          // both disappear since there would be no remaining connection to the
          // rest of the tree
          if (checkTargetIsDescendant(selected, node)) {
            return;
          }

          TreeItem parent = selected.getParent();
          parent.getChildren().remove(selected);
          modifiedPlan = true;

          SchemaNode schemaNode = (SchemaNode) selected;
          if (node == null) {
            rootNode.getChildren().add(selected);
            schemaNode.getDob().setParentId(null);
            sortRootChildren();
          } else {
            node.getChildren().add(selected);
            schemaNode.getDob().setParentId(node.getDob().getId());
            node.sortChildren();
          }
        } else if (db.getString().startsWith("sip preview")) {
          SchemaNode target = null;
          if (treeItem instanceof SipPreviewNode) {
            target = (SchemaNode) treeItem.getParent();
          } else
            target = (SchemaNode) treeItem;
          if (target == null) {
            target = rootNode;
          }

          List<TreeItem<String>> selectedItems = new ArrayList<>(treeView.getSelectionModel().getSelectedItems());
          for (TreeItem t : selectedItems) {
            if (t instanceof SipPreviewNode) {
              SipPreviewNode sourceSIP = (SipPreviewNode) t;

              // Remove the SIP from its parent and rule
              SchemaNode parent = (SchemaNode) sourceSIP.getParent();
              parent.removeChild(sourceSIP);
              sourceSIP.getSip().removeFromRule();

              // Add the SIP to the new parent
              String newParentID = null;
              if (target != rootNode)
                newParentID = target.getDob().getId();
              sourceSIP.getSip().setParentId(newParentID);
              target.addChild(UUID.randomUUID().toString(), sourceSIP);
              target.getChildren().add(sourceSIP);
              target.sortChildren();
            }
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
    });
  }

  private boolean checkTargetIsDescendant(TreeItem source, TreeItem target) {
    if (target == null) {
      return false;
    }
    TreeItem aux = target.getParent();
    boolean isChild = false;
    while (aux != null) {
      if (aux == source) {
        isChild = true;
        break;
      }
      aux = aux.getParent();
    }
    return isChild;
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

  public void showHelp() {
    rootNode.getChildren().clear();
    setTop(new HBox());
    setCenter(centerHelp);
    setBottom(new HBox());
    hasClassificationScheme.setValue(false);
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

  public boolean isModifiedPlan() {
    return modifiedPlan;
  }

  public void setModifiedPlan(boolean b) {
    modifiedPlan = b;
  }

  public BooleanProperty hasClassificationScheme() {
    return hasClassificationScheme;
  }

  /**
   * @return The Map with the SIPs of all the SchemaNodes in the TreeView
   */
  public Map<SipPreview, String> getSelectedSipPreviews() {
    schemaNodes.add(rootNode);
    Map<SipPreview, String> result = new HashMap<>();

    ObservableList<TreeItem<String>> selected = treeView.getSelectionModel().getSelectedItems();
    if (selected != null) {
      for (TreeItem<String> item : selected) {
        if (item instanceof SipPreviewNode) {
          SipPreviewNode sip = (SipPreviewNode) item;
          SchemaNode parent = (SchemaNode) sip.getParent();
          result.put(sip.getSip(), parent.getDob().getId());
        }
        if (item instanceof SchemaNode) {
          result.putAll(((SchemaNode) item).getSipPreviews());
        }
      }
    }
    if (result.isEmpty()) {// add all the SIPs to the result map
      for (SchemaNode sn : schemaNodes) {
        result.putAll(sn.getSipPreviews());
      }
    }
    return result;
  }
}
