package org.roda.rodain.schema.ui;

import java.util.*;

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
  private static Properties style;
  private TreeView<String> treeView;
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

    TreeItem<String> rootNode = new TreeItem<>();
    rootNode.setExpanded(true);

    // get the classification schema and add all its nodes to the tree
    ClassificationSchema cs = ClassificationSchema.instantiate();
    for (DescriptionObject obj : cs.getDos()) {
      SchemaNode sn = new SchemaNode(obj);
      rootNode.getChildren().add(sn);
      schemaNodes.add(sn);
    }

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
    Button removeLevel = new Button("Remove level");
    Button editLevel = new Button("Edit level");

    addLevel.setDisable(true);
    addLevel.setMinWidth(100);
    removeLevel.setDisable(true);
    removeLevel.setMinWidth(100);
    editLevel.setDisable(true);
    editLevel.setMinWidth(100);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    bottom.getChildren().addAll(associate, space, removeLevel, editLevel, addLevel);
  }

  private void startAssociation(SchemaNode descObj) {
    Set<SourceTreeItem> sourceSet = Main.getSourceSelectedItems();
    boolean valid = true;

    if (sourceSet != null && !sourceSet.isEmpty() && descObj != null) { // both
                                                                        // trees
                                                                        // need
                                                                        // to
                                                                        // have
                                                                        // 1
                                                                        // element
                                                                        // selected
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
          if (item != null && event.getGestureSource() != cell
            && event.getDragboard().hasString()) {
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

  /**
   * @param style
   *          Sets the Properties object to this class
   */
  public static void setStyleProperties(Properties style) {
    SchemaPane.style = style;
  }
}
