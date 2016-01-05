package org.roda.rodain.inspection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.utils.UIPair;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 26-10-2015.
 */
public class InspectionPane extends BorderPane {
  private VBox topBox;
  private VBox center;
  private HBox topSpace;
  private ComboBox<UIPair> itemTypes;

  private SipPreview currentSIP;
  private SchemaNode currentSchema;
  private ImageView topIcon;

  private VBox centerHelp;
  // Metadata
  private VBox metadata;
  private TextArea metaText;
  // SIP Content
  private BorderPane content;
  private TreeView sipFiles;
  private SipContentDirectory sipRoot;
  private Button remove, flatten, skip;
  private VBox bottom;
  // Rules
  private BorderPane rules;
  private ListView<RuleCell> ruleList;
  private VBox emptyRulesPane;


  /**
   * Creates a new inspection pane.
   *
   * @param stage The primary stage of the application
   */
  public InspectionPane(Stage stage) {
    createCenterHelp();
    createTop();
    createMetadata();
    createContent();
    createRulesList();
    createBottom();

    center = new VBox(10);
    center.setPadding(new Insets(0, 10, 10, 10));

    setCenter(centerHelp);

    metadata.minHeightProperty().bind(stage.heightProperty().multiply(0.40));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
  }

  private void createTop() {
    Label top = new Label(" ");
    topBox = new VBox();
    topBox.getChildren().add(top);
    topBox.setPadding(new Insets(10, 0, 10, 0));
    topBox.setAlignment(Pos.CENTER_LEFT);

    topSpace = new HBox();
    HBox.setHgrow(topSpace, Priority.ALWAYS);

    itemTypes = new ComboBox<>();
    itemTypes.setId("itemLevels");
    ObservableList<UIPair> itemList = FXCollections.observableArrayList();
    String itemTypesRaw = AppProperties.getDescLevels("levels_ordered");
    String[] itemTypesArray = itemTypesRaw.split(",");
    for (String item : itemTypesArray) {
      UIPair pair = new UIPair(item, AppProperties.getDescLevels("label.en." + item));
      itemList.add(pair);
    }
    itemTypes.setItems(itemList);
    itemTypes.valueProperty().addListener(new ChangeListener<UIPair>() {
      @Override
      public void changed(ObservableValue ov, UIPair t, UIPair t1) {
        if (currentSchema != null) {
          currentSchema.updateDescLevel(t1.getKey().toString());
          topIcon.setImage(currentSchema.getImage());
        }
      }
    });

  }

  private void createMetadata() {
    metadata = new VBox();
    metadata.getStyleClass().add("inspectionPart");

    HBox box = new HBox();
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(5, 10, 5, 10));
    box.setAlignment(Pos.CENTER_LEFT);

    Label title = new Label(AppProperties.getLocalizedString("InspectionPane.metadata"));

    box.getChildren().add(title);

    metaText = new TextArea();
    metaText.setWrapText(true);
    VBox.setVgrow(metaText, Priority.ALWAYS);
    metadata.getChildren().addAll(box, metaText);

    /*
     * We listen to the focused property and not the text property because we
     * only need to update when the text area loses focus. Using text property,
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

  /**
   * Saves the metadata from the text area in the SIP.
   */
  public void saveMetadata() {
    String oldMetadata = null, newMetadata = null;
    if (currentSIP != null) {
      oldMetadata = currentSIP.getMetadataContent();
      newMetadata = metaText.getText();

    } else if (currentSchema != null) {
      newMetadata = metaText.getText();
      List<DescObjMetadata> metadatas = currentSchema.getDob().getMetadata();
      if (!metadatas.isEmpty()) {
        oldMetadata = metadatas.get(0).getContentDecoded();
      }
    }
    // only update if there's been modifications or there's no old
    // metadata and the new isn't empty
    boolean update = false;
    if (newMetadata != null) {
      if (oldMetadata == null)
        update = true;
      else if (!oldMetadata.equals(newMetadata))
        update = true;
    }
    if (update) {
      if (currentSIP != null) {
        currentSIP.updateMetadata(metaText.getText());
      } else if (currentSchema != null) {
        List<DescObjMetadata> metadatas = currentSchema.getDob().getMetadata();
        if (!metadatas.isEmpty()) {
          metadatas.get(0).setContentDecoded(newMetadata);
        } else {
          DescObjMetadata newObjMetadata = new DescObjMetadata();
          newObjMetadata.setContentEncoding("Base64");
          newObjMetadata.setContentDecoded(newMetadata);
          metadatas.add(newObjMetadata);
        }
      }
    }
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
    Label title = new Label(AppProperties.getLocalizedString("InspectionPane.help.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    box.getChildren().addAll(titleBox);
    centerHelp.getChildren().add(box);
  }

  private void createContent() {
    content = new BorderPane();
    content.getStyleClass().add("inspectionPart");
    VBox.setVgrow(content, Priority.ALWAYS);

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(5, 10, 5, 10));

    Label title = new Label(AppProperties.getLocalizedString("content"));
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

    Button ignore = new Button(AppProperties.getLocalizedString("ignore"));
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
    flatten = new Button(AppProperties.getLocalizedString("InspectionPane.flatten"));
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
    skip = new Button(AppProperties.getLocalizedString("InspectionPane.skip"));
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

    ignore.minWidthProperty().bind(this.widthProperty().multiply(0.25));
    flatten.minWidthProperty().bind(this.widthProperty().multiply(0.25));
    skip.minWidthProperty().bind(this.widthProperty().multiply(0.25));

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
    VBox.setVgrow(rules, Priority.ALWAYS);

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(5, 10, 5, 10));

    Label title = new Label(AppProperties.getLocalizedString("InspectionPane.rules"));
    top.getChildren().add(title);
    rules.setTop(top);
    ruleList = new ListView<>();

    emptyRulesPane = new VBox();
    emptyRulesPane.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(emptyRulesPane, Priority.ALWAYS);
    emptyRulesPane.setAlignment(Pos.CENTER);

    VBox box = new VBox(40);
    box.setPadding(new Insets(10, 10, 10, 10));

    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    Label emptyText = new Label(AppProperties.getLocalizedString("InspectionPane.help.ruleList"));
    emptyText.getStyleClass().add("helpTitle");
    emptyText.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(emptyText);

    box.getChildren().addAll(titleBox);
    emptyRulesPane.getChildren().add(box);
    rules.setCenter(emptyRulesPane);
  }

  private void createBottom() {
    bottom = new VBox(10);
    bottom.setPadding(new Insets(0, 0, 10, 0));
    bottom.setAlignment(Pos.CENTER_LEFT);

    Separator separatorBottom = new Separator();

    HBox buttonBox = new HBox();
    buttonBox.setPadding(new Insets(0, 10, 0, 10));

    remove = new Button("Remove");
    remove.setMinWidth(100);
    remove.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        currentSIP.setRemoved();
        currentSIP.changedAndNotify();
        setCenter(centerHelp);
        setTop(new HBox());
        setBottom(new HBox());
      }
    });
    buttonBox.getChildren().add(remove);
    bottom.getChildren().addAll(separatorBottom, buttonBox);
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
   * <p/>
   * <p>
   * This method gets the id, metadata and content of a SipPreviewNode and uses
   * them to update the UI. The content is used to create a tree of
   * SipContentDirectory and SipContentFile, which are used to populate a
   * TreeView.
   * </p>
   *
   * @param sip The SipPreviewNode used to update the UI.
   * @see SipPreviewNode
   * @see SipContentDirectory
   * @see SipContentFile
   */
  public void update(SipPreviewNode sip) {
    setBottom(bottom);
    setTop(topBox);
    setCenter(center);
    currentSIP = sip.getSip();
    currentSchema = null;

    /* Top */
    Label title = new Label(sip.getValue());
    title.setWrapText(true);
    title.getStyleClass().add("title");

    HBox top = new HBox(5);
    top.setPadding(new Insets(0, 10, 10, 10));
    top.setAlignment(Pos.CENTER_LEFT);
    top.getChildren().addAll(sip.getGraphic(), title);
    Separator separatorTop = new Separator();

    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, separatorTop);

    /* Center */
    center.getChildren().clear();
    // id
    HBox idBox = new HBox(5);
    Label idKey = new Label("ID:");
    idKey.getStyleClass().add("sipId");

    Label id = new Label(sip.getSip().getId());
    id.setWrapText(true);
    id.getStyleClass().add("sipId");
    id = makeSelectable(id);

    idBox.getChildren().addAll(idKey, id);

    // metadata
    String meta = sip.getSip().getMetadataContent();
    metaText.setText(meta);

    // content tree
    createContent(sip);

    center.getChildren().addAll(idBox, metadata, content);
    setCenter(center);

    remove.setDisable(false);
  }

  /**
   * Updates the UI using a SchemaNode.
   * <p/>
   * <p>
   * Uses the metadata and rule list to update the UI. The rule list is used to
   * create a ListView of RuleCell.
   * </p>
   *
   * @param node The SchemaNode used to update the UI.
   * @see RuleCell
   * @see SchemaNode
   */
  public void update(SchemaNode node) {
    setBottom(bottom);
    setTop(topBox);
    currentSIP = null;
    currentSchema = node;

    /* top */
    // title
    Label title = new Label(node.getValue());
    title.getStyleClass().add("title");
    title.textProperty().bindBidirectional(node.valueProperty());

    HBox top = new HBox(5);
    top.setPadding(new Insets(0, 10, 10, 10));
    top.setAlignment(Pos.CENTER_LEFT);
    topIcon = new ImageView(node.getImage());
    top.getChildren().addAll(topIcon, title, topSpace, itemTypes);

    // Select current description level
    String currentDescLevel = node.getDob().getDescriptionlevel();
    for (UIPair pair : itemTypes.getItems()) {
      if (currentDescLevel.equals(pair.getKey())) {
        itemTypes.getSelectionModel().select(pair);
        break;
      }
    }

    Separator separatorTop = new Separator();
    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, separatorTop);

    /* center */
    center.getChildren().clear();
    // id
    HBox idBox = new HBox(5);
    Label idKey = new Label("ID:");
    idKey.getStyleClass().add("sipId");

    Label id = new Label(node.getDob().getId());
    id.setWrapText(true);
    id.getStyleClass().add("sipId");
    id = makeSelectable(id);

    idBox.getChildren().addAll(idKey, id);

    // metadata
    List<DescObjMetadata> metadatas = node.getDob().getMetadata();
    if (!metadatas.isEmpty()) {
      // For now we only get the first metadata object
      metaText.setText(metadatas.get(0).getContentDecoded());
    } else
      metaText.clear();

    // rules
    updateRuleList();

    center.getChildren().addAll(idBox, metadata, rules);
    setCenter(center);

    remove.setDisable(true);
  }

  private void updateRuleList() {
    ruleList.getItems().clear();
    Set<Rule> currentRules = currentSchema.getRules();

    for (Rule rule : currentRules) {
      RuleCell cell = new RuleCell(currentSchema, rule);
      rule.addObserver(cell);
      ruleList.getItems().add(cell);
    }
    if (currentRules.isEmpty()) {
      rules.setCenter(emptyRulesPane);
    } else
      rules.setCenter(ruleList);
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
   * <p/>
   * <p>
   * Used by ContentClickedEventHandler to set the state of the SIP content
   * buttons, since they are only enabled when a directory is selected.
   * </p>
   *
   * @param state The new state of the buttons.
   */
  public void setStateContentButtons(boolean state) {
    flatten.setDisable(state);
    skip.setDisable(state);
  }
}
