package org.roda.rodain.schema.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.Stage;
import javafx.util.Callback;

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
  private HBox dropBox;
  private static Stage primaryStage;

  private ArrayList<SchemaNode> schemaNodes;

  // center help
  private VBox centerHelp;
  private boolean hasClassificationScheme = false;

  /**
   * Creates a new SchemaPane object.
   *
   * @param stage The primary stage of the application.
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
    load.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        loadClassificationSchema();
      }
    });
    load.getStyleClass().add("helpButton");
    loadBox.getChildren().add(load);

    Hyperlink link = new Hyperlink(AppProperties.getLocalizedString("SchemaPane.create"));
    link.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        createClassificationScheme();
      }
    });

    TextFlow flow = new TextFlow(new Text(AppProperties.getLocalizedString("SchemaPane.or")), link);
    flow.setTextAlignment(TextAlignment.CENTER);

    box.getChildren().addAll(titleBox, loadBox);
    centerHelp.getChildren().addAll(box, flow);
  }

  private void createDropBox(){
    dropBox = new HBox();
    dropBox.setAlignment(Pos.CENTER);
    dropBox.getStyleClass().add("dropBox");
    dropBox.setMinHeight(200);

    Label title = new Label("2A. " + AppProperties.getLocalizedString("SchemaPane.help.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    dropBox.getChildren().add(title);

    dropBox.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        if (rootNode != null && event.getGestureSource() instanceof SourceTreeCell) {
          event.acceptTransferModes(TransferMode.COPY);
          if(!dropBox.getStyleClass().contains("dropBoxHovered"))
            dropBox.getStyleClass().add("dropBoxHovered");
          title.setText(AppProperties.getLocalizedString("InspectionPane.onDrop"));
        }
        event.consume();
      }
    });

    dropBox.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        RodaIn.getSchemaPane().startAssociation(rootNode);
        if(dropBox.getStyleClass().contains("dropBoxHovered"))
          dropBox.getStyleClass().remove("dropBoxHovered");
        event.consume();
      }
    });

    dropBox.setOnDragExited(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        title.setText("2A. " + AppProperties.getLocalizedString("SchemaPane.help.title"));
        if(dropBox.getStyleClass().contains("dropBoxHovered"))
          dropBox.getStyleClass().remove("dropBoxHovered");
        event.consume();
      }
    });
  }

  private void createTreeView() {
    // create tree pane
    treeBox = new VBox();
    VBox.setVgrow(treeBox, Priority.ALWAYS);

    DescriptionObject dobj = new DescriptionObject();
    rootNode = new SchemaNode(dobj);
    rootNode.setExpanded(true);

    // create the tree view
    treeView = new TreeView<>(rootNode);
    VBox.setVgrow(treeView, Priority.ALWAYS);
    treeView.setShowRoot(false);
    treeView.setEditable(true);
    treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
      @Override
      public TreeCell<String> call(TreeView<String> p) {
        SchemaTreeCell cell = new SchemaTreeCell();
        setDropEvent(cell);
        return cell;
      }
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
        if (newValue instanceof SipPreviewNode) {
          RodaIn.getInspectionPane().update((SipPreviewNode) newValue);
        }
        if (newValue instanceof SchemaNode) {
          RodaIn.getInspectionPane().update((SchemaNode) newValue);
        }
      }
    });
  }

  private SchemaNode getSelectedItem() {
    SchemaNode result = null;
    int selIndex = treeView.getSelectionModel().getSelectedIndex();
    if (selIndex != -1) {
      TreeItem selected = treeView.getTreeItem(selIndex);
      if (selected instanceof SchemaNode) {
        result = (SchemaNode) selected;
      }
    }
    return result;
  }

  /**
   * Creates a file chooser dialog so that the user can choose the classification scheme file to be loaded.
   * Then, loads the file and creates the tree.
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
   * Creates a ClassificationSchema object from the InputStream and builds a tree using it.
   * @param stream The stream with the JSON file used to create the ClassificationSchema
   */
  public void loadClassificationSchemeFromStream(InputStream stream){
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
    InputStream input = new FileInputStream(fileName);

    // create ObjectMapper instance
    ObjectMapper objectMapper = new ObjectMapper();

    // convert json string to object
    return objectMapper.readValue(input, ClassificationSchema.class);
  }

  private void updateClassificationSchema(ClassificationSchema cs) {
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

  private void sortRootChildren() {
    ArrayList<TreeItem<String>> aux = new ArrayList<>(rootNode.getChildren());
    Collections.sort(aux, new SchemaComparator());
    rootNode.getChildren().setAll(aux);
  }

  private void createBottom() {
    bottom = new HBox(10);
    bottom.setPadding(new Insets(10, 10, 10, 10));

    Button associate = new Button(AppProperties.getLocalizedString("associate"));
    associate.setMinWidth(100);
    associate.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        SchemaNode selected = getSelectedItem();
        if (selected != null) {
          startAssociation(selected);
        }
      }
    });

    Button removeLevel = new Button(AppProperties.getLocalizedString("SchemaPane.remove"));
    removeLevel.setMinWidth(100);
    removeLevel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        SchemaNode selected = getSelectedItem();
        if (selected != null) {
          TreeItem parent = selected.getParent();
          parent.getChildren().remove(selected);
          schemaNodes.remove(selected);
        }
      }
    });

    Button addLevel = new Button(AppProperties.getLocalizedString("SchemaPane.add"));
    addLevel.setMinWidth(100);

    addLevel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        addNewLevel();
      }
    });

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    bottom.getChildren().addAll(associate, space, removeLevel, addLevel);
  }

  public void createClassificationScheme() {
    setTop(topBox);
    setCenter(treeBox);
    setBottom(bottom);
    rootNode.getChildren().clear();
  }

  private SchemaNode addNewLevel() {
    SchemaNode selected = getSelectedItem();
    DescriptionObject dobj = new DescriptionObject();
    dobj.setId(UUID.randomUUID().toString());
    dobj.setTitle(AppProperties.getLocalizedString("SchemaPane.newNode"));
    dobj.setDescriptionlevel("class");
    SchemaNode newNode = new SchemaNode(dobj);
    if (selected != null) {
      dobj.setParentId(selected.getDob().getId());
      selected.getChildren().add(newNode);
      selected.sortChildren();
      if (!selected.isExpanded())
        selected.setExpanded(true);
    } else {
      newNode.updateDescLevel("fonds");
      rootNode.getChildren().add(newNode);
      sortRootChildren();
    }
    schemaNodes.add(newNode);
    // Edit the node's title as soon as it's created
    treeView.layout();
    treeView.edit(newNode);
    return newNode;
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
          event.acceptTransferModes(TransferMode.MOVE);
        }
        if (treeItem instanceof SchemaNode) {
          SchemaNode item = (SchemaNode) cell.getTreeItem();
          if (item != null && event.getGestureSource() != cell && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
          }
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
        SchemaNode node = (SchemaNode) cell.getTreeItem();
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
          if (db.getString().startsWith("scheme")) {
            TreeItem selected = treeView.getSelectionModel().getSelectedItem();
            TreeItem parent = selected.getParent();
            parent.getChildren().remove(selected);
            if (node == null) {
              rootNode.getChildren().add(selected);
              sortRootChildren();
            } else {
              node.getChildren().add(selected);
              node.sortChildren();
            }
          } else {
            startAssociation(node);
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

  public void showTree(){
    if(!hasClassificationScheme && getCenter() == centerHelp){
      VBox box = new VBox(10);
      box.getChildren().addAll(treeBox, dropBox);
      schemaNodes.add(rootNode);
      setCenter(box);
      setTop(topBox);
    }
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
    Map<SipPreview, String> result = new HashMap<>();
    for (SchemaNode sn : schemaNodes) {
      result.putAll(sn.getSipPreviews());
    }
    return result;
  }
}
