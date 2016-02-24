package org.roda.rodain.inspection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import org.fxmisc.richtext.CodeArea;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.rules.InvalidEADException;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.MetadataValue;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 26-10-2015.
 */
public class InspectionPane extends BorderPane {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(InspectionPane.class.getName());
  private VBox topBox;
  private VBox center;
  private HBox topSpace;

  private SipPreviewNode currentSIPNode;
  private SipPreview currentSIP;
  private SchemaNode currentSchema;
  private ImageView topIcon;
  private Label paneTitle;

  private VBox centerHelp;
  // Metadata
  private VBox metadata;
  private CodeArea metaText;
  private GridPane metadataForm;
  private ToggleButton toggleForm;
  private HBox metadataLoadingPane, metadataTopBox;
  private TextField titleTextField;
  // SIP Content
  private BorderPane content;
  private VBox treeBox;
  private TreeView sipFiles;
  private SipContentDirectory sipRoot;
  private Button flatten, skip;
  private HBox loadingPane, contentBottom;
  private static Image loadingGif;
  private Task<Void> contentTask;
  private Task<Boolean> metadataTask;
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
    createLoadingPanes();

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
  }

  private void createMetadata() {
    metadata = new VBox();
    metadata.getStyleClass().add("inspectionPart");

    metadataForm = new GridPane();
    metadataForm.setVgap(5);
    metadataForm.setPadding(new Insets(5, 5, 5, 5));
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setPercentWidth(20);
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setPercentWidth(80);
    metadataForm.getColumnConstraints().addAll(column1, column2);

    metadataTopBox = new HBox();
    metadataTopBox.getStyleClass().add("hbox");
    metadataTopBox.setPadding(new Insets(5, 10, 5, 10));
    metadataTopBox.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label(AppProperties.getLocalizedString("InspectionPane.metadata"));
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    toggleForm = new ToggleButton();
    Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.code, Color.WHITE);
    Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.list, Color.WHITE);
    ImageView toggleImage = new ImageView();
    toggleForm.setGraphic(toggleImage);
    toggleImage.imageProperty().bind(Bindings.when(toggleForm.selectedProperty()).then(selected).otherwise(unselected));

    Button saveButton = new Button();
    saveButton.setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.floppy, Color.WHITE)));
    saveButton.setOnAction(event -> {
      saveMetadata();
      if (Utils.isEAD(metaText.getText())) {
        toggleForm.setVisible(true);
        if (metadata.getChildren().contains(metaText)) {
          toggleForm.setSelected(false);
        } else
          toggleForm.setSelected(true);
      }
    });

    toggleForm.selectedProperty().addListener((observable, oldValue, newValue) -> {
      saveMetadata();
      // newValue == true means that the form will be displayed
      if (newValue) {
        metadata.getChildren().remove(metaText);
        metadataForm.getChildren().clear();
        updateForm();
        if (!metadata.getChildren().contains(metadataForm)) {
          metadata.getChildren().add(metadataForm);
        }
      } else { // from the form to the metadata text
        metadata.getChildren().remove(metadataForm);
        if (!metadata.getChildren().contains(metaText))
          metadata.getChildren().add(metaText);
      }
    });

    metadataTopBox.getChildren().addAll(titleLabel, space, toggleForm, saveButton);

    metaText = new CodeArea();
    VBox.setVgrow(metaText, Priority.ALWAYS);
    metadata.getChildren().addAll(metadataTopBox, metaText);

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

  private void updateForm() {
    Map<String, MetadataValue> metadataValues;
    try {
      metadataValues = getMetadataValues();
    } catch (InvalidEADException e) {
      noForm();
      return;
    }
    if (metadataValues == null || metadataValues.isEmpty()) {
      noForm();
      return;
    }
    int i = 0;
    for (MetadataValue metadataValue : metadataValues.values()) {
      Label label = new Label(metadataValue.getTitle());
      label.getStyleClass().add("formLabel");

      Control control;
      switch (metadataValue.getFieldType()) {
        case "date":
          String pattern = "yyyy-MM-dd";
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
          LocalDateStringConverter ldsc = new LocalDateStringConverter(formatter, null);

          DatePicker datePicker = new DatePicker(ldsc.fromString(metadataValue.getValue()));
          datePicker.setMaxWidth(Double.MAX_VALUE);
          datePicker.setConverter(new StringConverter<LocalDate>() {
            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate localDate) {
              if (localDate == null)
                return "";
              return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
              if (dateString == null || dateString.trim().isEmpty())
                return null;
              return LocalDate.parse(dateString, dateTimeFormatter);
            }
          });
          datePicker.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            metadataValue.setValue(ldsc.toString(newValue1));
          });
          control = datePicker;
          break;
        case "combo":
          ComboBox<UIPair> itemTypes = new ComboBox<>();
          itemTypes.setMaxWidth(Double.MAX_VALUE);
          control = itemTypes;
          itemTypes.setId("itemLevels");
          ObservableList<UIPair> itemList = FXCollections.observableArrayList(metadataValue.getFieldOptions());
          itemTypes.setItems(itemList);
          // Select current value
          for (UIPair pair : itemTypes.getItems()) {
            if (metadataValue.getValue().equals(pair.getKey())) {
              itemTypes.getSelectionModel().select(pair);
              break;
            }
          }
          itemTypes.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            metadataValue.setValue(newValue1.getKey().toString());
            if (metadataValue.getId().equals("level")) {
              if (currentSchema != null) {
                currentSchema.updateDescLevel(newValue1.getKey().toString());
                topIcon.setImage(currentSchema.getIconBlack());
              }
              if (currentSIPNode != null) {
                currentSIPNode.setDescriptionLevel(newValue1.getKey().toString());
                topIcon.setImage(currentSIPNode.getIconBlack());
              }
              if (titleTextField != null) {
                // force update
                String title = titleTextField.getText();
                titleTextField.setText("");
                titleTextField.setText(title);
              }
            }
          });
          break;
        default:
          TextField textField = new TextField(metadataValue.getValue());
          HBox.setHgrow(textField, Priority.ALWAYS);
          textField.setUserData(metadataValue);
          control = textField;
          textField.textProperty().addListener((observable2, oldValue2, newValue2) -> {
            metadataValue.setValue(newValue2);
          });
          if (metadataValue.getId().equals("title")) {
            titleTextField = textField;
            paneTitle.textProperty().bind(textField.textProperty());
            if (currentSIPNode != null) {
              currentSIPNode.valueProperty().bind(textField.textProperty());
            } else {
              if (currentSchema != null) {
                currentSchema.valueProperty().bind(textField.textProperty());
              }
            }
          }
          break;
      }
      metadataForm.add(label, 0, i);
      metadataForm.add(control, 1, i);
      i++;
    }
  }

  private void updateTextArea(String text) {
    metaText.replaceText(text);
    metaText.setStyleSpans(0, XMLEditor.computeHighlighting(text));
  }

  private void noForm() {
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metaText);
    toggleForm.setVisible(false);
  }

  private Map<String, MetadataValue> getMetadataValues() throws InvalidEADException {
    if (currentSIP != null) {
      return currentSIP.getMetadataValues();
    } else {
      if (currentSchema != null) {
        return currentSchema.getDob().getMetadataValues();
      } else {
        // error, there is no SIP or SchemaNode selected
        return null;
      }
    }
  }

  /**
   * Saves the metadata from the text area in the SIP.
   */
  public void saveMetadata() {
    if (metadata.getChildren().contains(metadataForm)) {
      if (currentSIP != null) {
        currentSIP.applyMetadataValues();
        updateTextArea(currentSIP.getMetadataContent());
      }
      if (currentSchema != null) {
        currentSchema.getDob().applyMetadataValues();
        updateTextArea(currentSchema.getDob().getMetadataWithReplaces().get(0).getContentDecoded());
      }
    } else {
      String oldMetadata = null, newMetadata = null;
      if (currentSIP != null) {
        oldMetadata = currentSIP.getMetadataContent();
        newMetadata = metaText.getText();

      } else if (currentSchema != null) {
        newMetadata = metaText.getText();
        List<DescObjMetadata> metadatas = currentSchema.getDob().getMetadataWithReplaces();
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
          currentSIP.updateMetadata(newMetadata);
        } else if (currentSchema != null) {
          List<DescObjMetadata> metadatas = currentSchema.getDob().getMetadataWithReplaces();
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
    top.setPadding(new Insets(10, 10, 10, 10));

    Label title = new Label(AppProperties.getLocalizedString("content"));
    top.getChildren().add(title);
    content.setTop(top);

    // create tree pane
    treeBox = new VBox();
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

  private void createLoadingPanes() {
    loadingPane = new HBox();
    metadataLoadingPane = new HBox();
    loadingPane.setAlignment(Pos.CENTER);
    metadataLoadingPane.setAlignment(Pos.CENTER);
    VBox.setVgrow(metadataLoadingPane, Priority.ALWAYS);
    try {
      if (loadingGif == null)
        loadingGif = new Image(ClassLoader.getSystemResource("loading.GIF").openStream());
      loadingPane.getChildren().add(new ImageView(loadingGif));
      metadataLoadingPane.getChildren().add(new ImageView(loadingGif));
    } catch (IOException e) {
      log.error("Error reading loading GIF", e);
    }
  }

  private void createContentBottom() {
    contentBottom = new HBox(10);
    contentBottom.setPadding(new Insets(10, 10, 10, 10));
    contentBottom.setAlignment(Pos.CENTER);

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
          SipContentDirectory parent = (SipContentDirectory) dir.getParent();
          dir.skip();
          // update the SIP's internal content representation
          Set<TreeNode> newFiles = new HashSet<>();
          for (String s : sipRoot.getTreeNode().getKeys())
            newFiles.add(sipRoot.getTreeNode().get(s));
          currentSIP.setFiles(newFiles);
          // clear the parent and recreate the children based on the updated
          // tree nodes
          TreeItem grandparent = parent.getParent();
          if (grandparent == null) {
            grandparent = parent;
          }
          TreeItem newParent = recCreateSipContent(parent.getTreeNode(), grandparent);
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

    contentBottom.getChildren().addAll(ignore, flatten, skip);
    content.setBottom(contentBottom);
  }

  private void createContent(SipPreviewNode node) {
    SipContentDirectory newRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);
    content.setCenter(loadingPane);
    content.setBottom(new HBox());

    contentTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Set<TreeNode> files = node.getSip().getFiles();
        for (TreeNode treeNode : files) {
          TreeItem<Object> startingItem = recCreateSipContent(treeNode, newRoot);
          startingItem.setExpanded(true);
          newRoot.getChildren().add(startingItem);
        }
        newRoot.sortChildren();
        return null;
      }
    };
    contentTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
      @Override
      public void handle(WorkerStateEvent workerStateEvent) {
        sipRoot = newRoot;
        sipFiles.setRoot(sipRoot);
        content.setCenter(treeBox);
        content.setBottom(contentBottom);
      }
    });
    new Thread(contentTask).start();

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
    top.setPadding(new Insets(10, 10, 10, 10));

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

    emptyRulesPane.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        if (currentSchema != null && event.getGestureSource() instanceof SourceTreeCell) {
          event.acceptTransferModes(TransferMode.COPY);
          emptyText.setText(AppProperties.getLocalizedString("InspectionPane.onDrop"));
        }
        event.consume();
      }
    });

    emptyRulesPane.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        RodaIn.getSchemaPane().startAssociation(currentSchema);
        event.consume();
      }
    });

    emptyRulesPane.setOnDragExited(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        emptyText.setText(AppProperties.getLocalizedString("InspectionPane.help.ruleList"));
        event.consume();
      }
    });


    box.getChildren().addAll(titleBox);
    emptyRulesPane.getChildren().add(box);
    rules.setCenter(emptyRulesPane);
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
    setTop(topBox);
    setCenter(center);
    currentSIPNode = sip;
    currentSIP = sip.getSip();
    currentSchema = null;
    if(contentTask != null && contentTask.isRunning()){
      contentTask.cancel(true);
    }

    /* Top */
    paneTitle = new Label(sip.getValue());
    paneTitle.setWrapText(true);
    paneTitle.getStyleClass().add("title");

    HBox top = new HBox(5);
    top.setPadding(new Insets(0, 10, 10, 10));
    top.setAlignment(Pos.CENTER_LEFT);
    ImageView imageView = new ImageView(sip.getIconBlack());
    top.getChildren().addAll(imageView, paneTitle);
    Separator separatorTop = new Separator();

    topBox.setPadding(new Insets(10, 0, 10, 0));
    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, separatorTop);

    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metadataLoadingPane);
    metadataTask = new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        // metadata
        String meta = sip.getSip().getMetadataContent();
        updateTextArea(meta);
        return Utils.isEAD(metaText.getText());
      }
    };
    metadataTask.setOnSucceeded((Void) -> showMetadataPane(metadataTask.getValue()));
    new Thread(metadataTask).start();

    /* Center */
    center.getChildren().clear();

    // content tree
    createContent(sip);

    center.getChildren().addAll(metadata, content);
    setCenter(center);
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
    setTop(topBox);
    currentSIP = null;
    currentSIPNode = null;
    currentSchema = node;
    if(contentTask != null && contentTask.isRunning()){
      contentTask.cancel(true);
    }
    if (metadataTask != null && metadataTask.isRunning()) {
      metadataTask.cancel(true);
    }

    /* top */
    // title
    paneTitle = new Label(node.getValue());
    paneTitle.setWrapText(true);
    paneTitle.getStyleClass().add("title");

    HBox top = new HBox(5);
    top.setPadding(new Insets(5, 10, 10, 10));
    top.setAlignment(Pos.CENTER_LEFT);
    topIcon = new ImageView(node.getIconBlack());
    top.getChildren().addAll(topIcon, paneTitle);

    Separator separatorTop = new Separator();
    topBox.setPadding(new Insets(5, 0, 5, 0));
    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, separatorTop);

    /* center */
    center.getChildren().clear();
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metadataLoadingPane);

    metadataTask = new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        // metadata
        List<DescObjMetadata> metadatas = node.getDob().getMetadataWithReplaces();
        if (!metadatas.isEmpty()) {
          // For now we only get the first metadata object
          updateTextArea(metadatas.get(0).getContentDecoded());
        } else
          metaText.clear();
        return Utils.isEAD(metaText.getText());
      }
    };
    metadataTask.setOnSucceeded((Void) -> showMetadataPane(metadataTask.getValue()));
    new Thread(metadataTask).start();

    // rules
    updateRuleList();

    center.getChildren().addAll(metadata, rules);
    setCenter(center);
  }

  public void showHelp(){
    setCenter(centerHelp);
    setTop(new HBox());
  }

  private void showMetadataPane(boolean isEAD) {
    metadata.getChildren().clear();
    metadata.getChildren().add(metadataTopBox);

    if (isEAD) {
      toggleForm.setVisible(true);
      toggleForm.setSelected(false);
      toggleForm.fire();
    } else {
      toggleForm.setVisible(false);
      metadata.getChildren().add(metaText);
    }
  }

  /**
   * Updates the rule list of the currently selected scheme node.
   */
  public void updateRuleList() {
    if(currentSchema != null) {
      ruleList.getItems().clear();
      Set<Rule> currentRules = currentSchema.getRules();

      for (Rule rule : currentRules) {
        RuleCell cell = new RuleCell(currentSchema, rule);
        cell.maxWidthProperty().bind(widthProperty().subtract(36));
        rule.addObserver(cell);
        ruleList.getItems().add(cell);
      }
      if (currentRules.isEmpty()) {
        rules.setCenter(emptyRulesPane);
      } else
        rules.setCenter(ruleList);
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
