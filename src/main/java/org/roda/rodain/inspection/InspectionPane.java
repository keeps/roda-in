package org.roda.rodain.inspection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 26-10-2015.
 */
public class InspectionPane extends BorderPane {
  private VBox topBox;
  private VBox center;

  private VBox metadata;
  private TextArea metaText;

  private BorderPane content;
  private TreeView sipFiles;
  private SipContentDirectory sipRoot;

  private BorderPane rules;
  private ListView<RuleCell> ruleList;

  private Button remove, flatten, skip;
  private HBox bottom;

  private SipPreview currentSIP;
  private SchemaNode currentSchema;

  public InspectionPane(Stage stage) {
    createTop();
    createMetadata();
    createContent();
    createRulesList();
    createBottom();

    center = new VBox(10);
    center.setPadding(new Insets(10, 10, 10, 10));

    metadata.minHeightProperty().bind(stage.heightProperty().multiply(0.40));
    this.prefWidthProperty().bind(stage.widthProperty().multiply(0.32));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
  }

  private void createTop() {
    Label top = new Label(" ");
    topBox = new VBox(5);
    topBox.getChildren().add(top);
    topBox.setPadding(new Insets(10, 10, 5, 10));
    topBox.setAlignment(Pos.CENTER_LEFT);
    setTop(topBox);
  }

  private void createMetadata() {
    metadata = new VBox();
    metadata.getStyleClass().add("inspectionPart");

    HBox box = new HBox();
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(5, 10, 5, 10));
    box.setAlignment(Pos.CENTER_LEFT);

    Label title = new Label("Metadata");

    box.getChildren().add(title);

    metaText = new TextArea();
    metaText.setWrapText(true);
    HBox.setHgrow(metaText, Priority.ALWAYS);
    VBox.setVgrow(metaText, Priority.ALWAYS);
    metadata.getChildren().addAll(box, metaText);

    /*
     * We listen to the focused property and not the text property because we
     * only need to update when the text area loses focus Using text property,
     * we would update after every single character modification, making the
     * application slower
     */
    metaText.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        if (!t1) { // lost focus, so update
          saveMetadata();
        }
      }
    });
  }

  public void saveMetadata(){
    if(currentSIP != null) {
      String oldMetadata = currentSIP.getMetadataContent();
      String newMetadata = metaText.getText();
      // only update if there's been modifications or there's no old
      // metadata and the new isn't empty
      boolean update = false;
      if (newMetadata != null) {
        if (oldMetadata == null)
          update = true;
        else if (!oldMetadata.equals(newMetadata))
          update = true;
      }
      if (update)
        currentSIP.updateMetadata(metaText.getText());
    }
  }

  private void createContent() {
    content = new BorderPane();
    content.getStyleClass().add("inspectionPart");
    VBox.setVgrow(content, Priority.ALWAYS);

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(5, 10, 5, 10));

    Label title = new Label("Content");
    top.getChildren().add(title);
    content.setTop(top);

    // create tree pane
    VBox treeBox = new VBox();
    treeBox.setPadding(new Insets(5, 5, 5, 5));
    treeBox.setSpacing(10);

    sipFiles = new TreeView<>();
    // add everything to the tree pane
    treeBox.getChildren().addAll(sipFiles);
    VBox.setVgrow(sipFiles, Priority.ALWAYS);
    HBox.setHgrow(sipFiles, Priority.ALWAYS);

    sipFiles.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
      @Override
      public TreeCell<String> call(TreeView<String> p) {
        return new InspectionTreeCell();
      }
    });

    sipFiles.setOnMouseClicked(new ContentClickedEventHandler(sipFiles, this));

    sipRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);
    sipRoot.setExpanded(true);
    sipFiles.setRoot(sipRoot);
    sipFiles.setShowRoot(false);
    content.setCenter(treeBox);
    createContentBottom();
  }

  private void createContentBottom() {
    HBox box = new HBox(10);
    box.setPadding(new Insets(10, 10, 10, 10));
    box.setAlignment(Pos.CENTER);
    HBox.setHgrow(box, Priority.ALWAYS);

    Button ignore = new Button("Ignore");
    ignore.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        InspectionTreeItem selected = (InspectionTreeItem) sipFiles.getSelectionModel().getSelectedItem();
        if (selected == null)
          return;
        Set<Path> paths = new HashSet<>();
        paths.add(selected.getPath());
        if (currentSIP != null) {
          currentSIP.ignoreContent(paths);
          TreeItem parent = selected.getParentDir();
          TreeItem child = (TreeItem) selected;
          parent.getChildren().remove(child);
        }
      }
    });
    flatten = new Button("Flatten directory");
    flatten.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        Object selected = sipFiles.getSelectionModel().getSelectedItem();
        if (selected instanceof SipContentDirectory) {
          SipContentDirectory dir = (SipContentDirectory) selected;
          dir.flatten();
        }
      }
    });
    skip = new Button("Skip Directory");
    skip.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        Object selected = sipFiles.getSelectionModel().getSelectedItem();
        if (selected instanceof SipContentDirectory) {
          SipContentDirectory dir = (SipContentDirectory) selected;
          dir.skip();
          // clear the parent and recreate the children based on the updated
          // tree nodes
          SipContentDirectory parent = (SipContentDirectory) dir.getParent();
          TreeItem newParent = recCreateSipContent(parent.getTreeNode(), parent.getParent());
          parent.getChildren().clear();
          parent.getChildren().addAll(newParent.getChildren());
          parent.sortChildren();
        }
      }
    });

    ignore.minWidthProperty().bind(content.widthProperty().multiply(0.3));
    flatten.minWidthProperty().bind(content.widthProperty().multiply(0.3));
    skip.minWidthProperty().bind(content.widthProperty().multiply(0.3));

    setStateContentButtons(true);

    box.getChildren().addAll(ignore, flatten, skip);
    content.setBottom(box);
  }

  private void createContent(SipPreviewNode node) {
    sipRoot.getChildren().clear();
    Set<TreeNode> files = node.getSip().getFiles();
    for (TreeNode treeNode : files) {
      TreeItem<Object> startingItem = recCreateSipContent(treeNode, sipRoot);
      startingItem.setExpanded(true);
      sipRoot.getChildren().add(startingItem);
    }
    sipRoot.sortChildren();
  }

  private TreeItem<Object> recCreateSipContent(TreeNode node, TreeItem parent) {
    SipContentDirectory result;
    Path path = node.getPath();
    if (Files.isDirectory(path))
      result = new SipContentDirectory(node, parent);
    else
      return new SipContentFile(path, parent);

    for (String key : node.getKeys()) {
      TreeItem<Object> temp = recCreateSipContent(node.get(key), result);
      result.getChildren().add(temp);
    }
    result.sortChildren();
    return result;
  }

  private void createRulesList() {
    rules = new BorderPane();
    rules.getStyleClass().add("inspectionPart");
    VBox.setVgrow(content, Priority.ALWAYS);

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(5, 10, 5, 10));

    Label title = new Label("Rules");
    top.getChildren().add(title);
    rules.setTop(top);

    ruleList = new ListView<>();
    rules.setCenter(ruleList);
  }

  private void createBottom() {
    bottom = new HBox();
    bottom.setPadding(new Insets(10, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_LEFT);

    remove = new Button("Remove");
    remove.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        currentSIP.setRemoved();
        currentSIP.changedAndNotify();
      }
    });
    bottom.getChildren().add(remove);
  }

  /**
   * @return the passed in label made selectable.
   */
  private Label makeSelectable(Label label) {
    StackPane textStack = new StackPane();
    textStack.setAlignment(Pos.CENTER_LEFT);

    TextField textField = new TextField(label.getText());
    textField.setEditable(false);
    textField.getStyleClass().add("hiddenTextField");

    textField.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() == 1) {
          textField.requestFocus();
          textField.selectAll();
        }
      }
    });

    // the invisible label is a hack to get the textField to size like a label.
    Label invisibleLabel = new Label();
    invisibleLabel.setMinWidth(200);
    invisibleLabel.textProperty().bind(label.textProperty());
    invisibleLabel.setVisible(false);

    textStack.getChildren().addAll(invisibleLabel, textField);
    label.textProperty().bindBidirectional(textField.textProperty());
    label.setGraphic(textStack);
    label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    return label;
  }

  /**
   * Updates the UI using a SipPreviewNode.
   *
   * <p>
   * This method gets the id, metadata and content of a SipPreviewNode and uses
   * them to update the UI. The content is used to create a tree of
   * SipContentDirectory and SipContentFile, which are used to populate a
   * TreeView.
   * </p>
   *
   * @see SipPreviewNode
   * @see SipContentDirectory
   * @see SipContentFile
   * @param sip
   *          The SipPreviewNode used to update the UI.
   */
  public void update(SipPreviewNode sip) {
    setBottom(bottom);
    currentSIP = sip.getSip();
    currentSchema = null;
    createContent(sip);
    center.getChildren().clear();
    center.getChildren().addAll(metadata, content);
    setCenter(center);

    String meta = sip.getSip().getMetadataContent();
    metaText.setText(meta);

    Label title = new Label(sip.getValue());
    title.setWrapText(true);
    title.getStyleClass().add("title");

    // ID labels
    HBox idBox = new HBox(5);
    Label idKey = new Label("ID:");
    idKey.getStyleClass().add("sipId");

    Label id = new Label(sip.getSip().getId());
    id.setWrapText(true);
    id.getStyleClass().add("sipId");
    id = makeSelectable(id);

    idBox.getChildren().addAll(idKey, id);

    HBox top = new HBox(5);
    top.setAlignment(Pos.CENTER_LEFT);
    top.getChildren().addAll(sip.getGraphic(), title);

    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, idBox);

    remove.setDisable(false);
  }

  /**
   * Updates the UI using a SchemaNode.
   *
   * <p>
   * Uses the metadata and rule list to update the UI. The rule list is used to
   * create a ListView of RuleCell.
   * </p>
   *
   * @see RuleCell
   * @see SchemaNode
   * @param node
   *          The SchemaNode used to update the UI.
   */
  public void update(SchemaNode node) {
    setBottom(bottom);
    currentSIP = null;
    currentSchema = node;
    metaText.clear();

    center.getChildren().clear();
    center.getChildren().add(metadata);

    Label title = new Label(node.getValue());
    title.getStyleClass().add("title");

    HBox top = new HBox(5);
    top.setAlignment(Pos.CENTER_LEFT);
    top.getChildren().addAll(node.getGraphic(), title);

    updateRuleList();
    center.getChildren().add(rules);
    setCenter(center);

    topBox.getChildren().clear();
    topBox.getChildren().add(top);

    remove.setDisable(true);
  }

  private void updateRuleList() {
    ruleList.getItems().clear();
    for (Rule rule : currentSchema.getRules()) {
      RuleCell cell = new RuleCell(currentSchema, rule);
      ruleList.getItems().add(cell);
    }
  }

  /**
   * Notifies this pane that something changed. Checks if the selected schema
   * node's rules changed and updates the rule list.
   */
  public void notifyChange() {
    if (ruleList != null && currentSchema != null && ruleList.getItems().size() != currentSchema.getRules().size()) {
      updateRuleList();
    }
  }

  /**
   * Sets the state of the SIP content buttons: "Flatten directory" and
   * "Skip directory".
   *
   * <p>
   * Used by ContentClickedEventHandler to set the state of the SIP content
   * buttons, since they are only enabled when a directory is selected.
   * </p>
   * 
   * @param state
   *          The new state of the buttons.
   */
  public void setStateContentButtons(boolean state) {
    flatten.setDisable(state);
    skip.setDisable(state);
  }
}
