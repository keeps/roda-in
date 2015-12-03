package org.roda.rodain.schema.ui;

import java.util.*;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.roda.rodain.core.Main;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.ClassificationSchema;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class SchemaPane extends BorderPane {
  private TreeView<String> treeView;
  private TreeItem<String> rootNode;
  private HBox refresh;
  private HBox bottom;
  private Stage primaryStage;

  private ArrayList<SchemaNode> schemaNodes;

  public SchemaPane(Stage stage) {
    super();
    primaryStage = stage;
    schemaNodes = new ArrayList<>();

    createTreeView();
    createTop();
    createBottom();

    this.setTop(refresh);
    this.setCenter(treeView);
    this.setBottom(bottom);

    this.prefWidthProperty().bind(stage.widthProperty().multiply(0.33));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
  }

  private void createTop() {
    Label title = new Label("Classification Schema");
    title.getStyleClass().add("title");

    refresh = new HBox();
    refresh.setPadding(new Insets(10, 10, 10, 10));
    refresh.setAlignment(Pos.CENTER_LEFT);
    refresh.getChildren().add(title);
  }

  private void createTreeView() {
    // create tree pane
    VBox treeBox = new VBox();
    treeBox.setPadding(new Insets(10, 10, 10, 10));

    rootNode = new TreeItem<>();
    rootNode.setExpanded(true);

    // create the tree view
    treeView = new TreeView<>(rootNode);
    treeView.setShowRoot(false);
    treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
      @Override
      public TreeCell<String> call(TreeView<String> p) {
        SchemaTreeCell cell = new SchemaTreeCell();
        setDropEvent(cell);
        return cell;
      }
    });

    // add everything to the tree pane
    treeBox.getChildren().add(treeView);
    treeView.setOnMouseClicked(new SchemaClickedEventHandler(this));
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

  public void loadClassificationSchema(ClassificationSchema cs) throws MalformedSchemaException {
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
          throw new MalformedSchemaException(message);
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
        }
        parentNode.getChildren().add(node);
      }
    }

    // Add all the root nodes as children of the hidden rootNode
    for (SchemaNode sn : roots) {
      rootNode.getChildren().add(sn);
      schemaNodes.add(sn);
    }
  }

  private void createBottom() {
    bottom = new HBox(10);
    bottom.setPadding(new Insets(10, 10, 10, 10));

    Button associate = new Button("Associate");
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

    Button addLevel = new Button("Add level");

    addLevel.setDisable(true);
    addLevel.setMinWidth(100);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    bottom.getChildren().addAll(associate, space, addLevel);
  }

  private void startAssociation(SchemaNode descObj) {
    Set<SourceTreeItem> sourceSet = Main.getSourceSelectedItems();
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
    if (sourceSet.isEmpty())
      valid = false;

    if (valid)
      RuleModalController.newAssociation(primaryStage, sourceSet, descObj);
  }

  private void setDropEvent(SchemaTreeCell cell) {
    setOnDragOver(cell);
    setOnDragEntered(cell);
    setOnDragExited(cell);
    setOnDragDropped(cell);
    setOnDragDone(cell);
  }

  private void setOnDragOver(final SchemaTreeCell cell) {
    // on a Target
    cell.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        TreeItem<String> treeItem = cell.getTreeItem();
        if (treeItem instanceof SchemaNode) {
          SchemaNode item = (SchemaNode) cell.getTreeItem();
          if (item != null && event.getGestureSource() != cell && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY);
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
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
          success = true;
          SchemaNode descObj = (SchemaNode) cell.getTreeItem();
          startAssociation(descObj);
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
