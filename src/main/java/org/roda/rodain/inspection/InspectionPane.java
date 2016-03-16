package org.roda.rodain.inspection;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;
import org.apache.commons.lang.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.inspection.documentation.DocumentationCreator;
import org.roda.rodain.inspection.documentation.SipDocumentationTreeView;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.rules.sip.MetadataValue;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.sip.SipRepresentation;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
  private SchemaNode currentSchema;
  private DescriptionObject currentDescOb;
  private ImageView topIcon;
  private Label paneTitle;

  private VBox centerHelp;
  // Metadata
  private VBox metadata;
  private CodeArea metaText;
  private GridPane metadataGrid;
  private ScrollPane metadataFormWrapper;
  private ToggleButton toggleForm;
  private HBox metadataLoadingPane, metadataTopBox;
  private TextField titleTextField;
  private Button validationButton;
  // SIP Content
  private BorderPane content;
  private VBox dataBox, documentationHelp;
  private SipDataTreeView sipFiles;
  private SipDocumentationTreeView sipDocumentation;
  private SipContentDirectory sipRoot, docsRoot;
  private HBox loadingPane, contentBottom, docsBottom;
  private static Image loadingGif;
  private Task<Void> contentTask, docsTask;
  private Task<Boolean> metadataTask;
  private Button ignore, removeRepresentation;
  private ToggleButton toggleDocumentation;
  // Rules
  private BorderPane rules;
  private ListView<RuleCell> ruleList;
  private VBox emptyRulesPane;

  /**
   * Creates a new inspection pane.
   *
   * @param stage
   *          The primary stage of the application
   */
  public InspectionPane(Stage stage) {
    createCenterHelp();
    createDocumentationHelp();
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

    metadataGrid = new GridPane();
    metadataGrid.setVgap(5);
    metadataGrid.setPadding(new Insets(5, 5, 5, 5));
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setPercentWidth(20);
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setPercentWidth(80);
    metadataGrid.getColumnConstraints().addAll(column1, column2);

    metadataFormWrapper = new ScrollPane();
    metadataFormWrapper.setContent(metadataGrid);
    metadataFormWrapper.setFitToWidth(true);

    metadataTopBox = new HBox();
    metadataTopBox.getStyleClass().add("hbox");
    metadataTopBox.setPadding(new Insets(5, 10, 5, 10));
    metadataTopBox.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label(AppProperties.getLocalizedString("InspectionPane.metadata"));
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    toggleForm = new ToggleButton();
    Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CODE, Color.WHITE);
    Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.LIST, Color.WHITE);
    ImageView toggleImage = new ImageView();
    toggleForm.setGraphic(toggleImage);
    toggleImage.imageProperty().bind(Bindings.when(toggleForm.selectedProperty()).then(selected).otherwise(unselected));

    validationButton = new Button();
    validationButton
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.WHITE)));
    validationButton.setOnAction(event -> {
      if (metadata.getChildren().contains(metadataFormWrapper)) {
        saveMetadata();
      }
      StringBuilder message = new StringBuilder();
      ValidationPopOver popOver = new ValidationPopOver();
      popOver.show(validationButton);

      Task<Boolean> validationTask = new Task<Boolean>() {
        @Override
        protected Boolean call() throws Exception {
          boolean result = false;
          try {
            if (currentDescOb != null) {
              List<DescObjMetadata> metadataList = currentDescOb.getMetadata();
              if (metadataList != null && !metadataList.isEmpty()) {
                DescObjMetadata metadata = metadataList.get(0);
                if (Utils.validateSchema(metaText.getText(), metadata.getSchema())) {
                  result = true;
                }
              }
            }
          } catch (SAXException e) {
            log.info("Error validating schema", e);
            message.append(e.getMessage());
          }
          return result;
        }
      };
      validationTask.setOnSucceeded(Void -> {
        if (validationTask.getValue()) {
          popOver.updateContent(true, message.toString());

          if (currentDescOb != null) {
            List<DescObjMetadata> metadataList = currentDescOb.getMetadata();
            if (metadataList != null && !metadataList.isEmpty()) {
              DescObjMetadata metadataObj = metadataList.get(0);
              if (metadataObj.getTemplateType() != null && "ead".equals(metadataObj.getTemplateType())) {
                toggleForm.setVisible(true);
                if (metadata.getChildren().contains(metaText)) {
                  toggleForm.setSelected(false);
                } else
                  toggleForm.setSelected(true);
              }
            }
          }
        } else {
          popOver.updateContent(false, message.toString());
          toggleForm.setVisible(false);
        }
      });
      new Thread(validationTask).start();

    });

    toggleForm.selectedProperty().addListener((observable, oldValue, newValue) -> {
      saveMetadata();
      // newValue == true means that the form will be displayed
      if (newValue) {
        metadata.getChildren().remove(metaText);
        metadataGrid.getChildren().clear();
        updateForm();
        if (!metadata.getChildren().contains(metadataFormWrapper)) {
          metadata.getChildren().add(metadataFormWrapper);
        }
      } else { // from the form to the metadata text
        metadata.getChildren().remove(metadataFormWrapper);
        if (!metadata.getChildren().contains(metaText))
          metadata.getChildren().add(metaText);
      }
    });

    metadataTopBox.getChildren().addAll(titleLabel, space, toggleForm, validationButton);

    metaText = new CodeArea();
    VBox.setVgrow(metaText, Priority.ALWAYS);
    metadata.getChildren().addAll(metadataTopBox, metaText);
    metaText.textProperty().addListener(
      (observable, oldValue, newValue) -> metaText.setStyleSpans(0, XMLEditor.computeHighlighting(newValue)));
    // set the tab size to 2 spaces
    metaText.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.TAB) {
        String s = StringUtils.repeat(" ", 2);
        metaText.insertText(metaText.getCaretPosition(), s);
        event.consume();
      }
    });

    /*
     * We listen to the focused property and not the text property because we
     * only need to update when the text area loses focus. Using text property,
     * we would update after every single character modification, making the
     * application slower
     */
    metaText.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { // lost focus, so update
        saveMetadata();
      }
    });
  }

  private void updateForm() {
    Map<String, MetadataValue> metadataValues;
    try {
      metadataValues = getMetadataValues();
    } catch (SAXException e) {
      log.info("Error validating metadata with the EAD2002 schema", e);
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
          datePicker.valueProperty()
            .addListener((observable1, oldValue1, newValue1) -> metadataValue.setValue(ldsc.toString(newValue1)));
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
            textField.setId("descObjTitle");
            titleTextField = textField;
            paneTitle.textProperty().bind(textField.textProperty());
            if (currentSIPNode != null) {
              textField.textProperty().bindBidirectional(currentSIPNode.valueProperty());
            } else {
              if (currentSchema != null) {
                textField.textProperty().bindBidirectional(currentSchema.valueProperty());
              }
            }
          }
          break;
      }
      metadataGrid.add(label, 0, i);
      metadataGrid.add(control, 1, i);
      i++;
    }
  }

  private void noForm() {
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metaText);
    toggleForm.setVisible(false);
  }

  private Map<String, MetadataValue> getMetadataValues() throws SAXException {
    if (currentDescOb != null) {
      return currentDescOb.getMetadataValues();
    } else {
      // error, there is no SIP or SchemaNode selected
      return null;
    }
  }

  /**
   * Saves the metadata from the text area in the SIP.
   */
  public void saveMetadata() {
    if (metadata.getChildren().contains(metadataFormWrapper)) {
      if (currentDescOb != null) {
        currentDescOb.applyMetadataValues();
        updateTextArea(currentDescOb.getMetadataWithReplaces().get(0).getContentDecoded());
      }
    } else {
      String oldMetadata = null, newMetadata = null;
      if (currentDescOb != null) {
        newMetadata = metaText.getText();
        List<DescObjMetadata> metadatas = currentDescOb.getMetadataWithReplaces();
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
        if (currentDescOb != null) {
          List<DescObjMetadata> metadatas = currentDescOb.getMetadataWithReplaces();
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

  private void createDocumentationHelp() {
    documentationHelp = new VBox();
    documentationHelp.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(documentationHelp, Priority.ALWAYS);
    documentationHelp.setAlignment(Pos.CENTER);

    VBox box = new VBox(40);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(10, 10, 10, 10));
    box.setMaxWidth(355);
    box.setMaxHeight(150);
    box.setMinHeight(150);

    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    Label title = new Label(AppProperties.getLocalizedString("InspectionPane.docsHelp.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    box.getChildren().addAll(titleBox);
    documentationHelp.getChildren().add(box);

    documentationHelp.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (event.getGestureSource() instanceof SourceTreeCell || db.hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
        title.setText(AppProperties.getLocalizedString("InspectionPane.onDropDocs"));
      }
      event.consume();
    });

    documentationHelp.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      if (db.hasFiles()) {
        Set<Path> paths = new HashSet<>();
        for (File file : db.getFiles()) {
          paths.add(file.toPath());
        }
        addDocumentationToSIP(null, paths);
      } else
        addDocumentationToSIP(null);

      dataBox.getChildren().clear();
      sipDocumentation.setRoot(docsRoot);
      dataBox.getChildren().add(sipDocumentation);
      content.setBottom(docsBottom);
      event.consume();
    });

    documentationHelp.setOnDragExited(event -> {
      title.setText(AppProperties.getLocalizedString("InspectionPane.docsHelp.title"));
      event.consume();
    });
  }

  private void createContent() {
    content = new BorderPane();
    content.getStyleClass().add("inspectionPart");
    VBox.setVgrow(content, Priority.ALWAYS);
    content.setMinHeight(200);

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(5, 10, 5, 10));

    Label title = new Label(AppProperties.getLocalizedString("data"));
    title.setPadding(new Insets(5, 0, 0, 0));
    top.getChildren().add(title);
    content.setTop(top);

    // create tree pane
    dataBox = new VBox();
    dataBox.setPadding(new Insets(5, 5, 5, 5));
    dataBox.setSpacing(10);

    sipFiles = new SipDataTreeView();
    // add everything to the tree pane
    dataBox.getChildren().addAll(sipFiles);
    VBox.setVgrow(sipFiles, Priority.ALWAYS);

    sipRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);
    sipRoot.setExpanded(true);
    sipFiles.setRoot(sipRoot);
    content.setCenter(dataBox);
    createContentBottom();

    // create documentation pane
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    sipDocumentation = new SipDocumentationTreeView();

    toggleDocumentation = new ToggleButton();
    Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.OPEN_FOLDER, Color.WHITE);
    Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.BOOK, Color.WHITE);
    ImageView toggleImage = new ImageView();
    toggleDocumentation.setGraphic(toggleImage);
    toggleImage.imageProperty()
      .bind(Bindings.when(toggleDocumentation.selectedProperty()).then(selected).otherwise(unselected));
    title.textProperty().bind(Bindings.when(toggleDocumentation.selectedProperty())
      .then(AppProperties.getLocalizedString("documentation")).otherwise(AppProperties.getLocalizedString("data")));

    toggleDocumentation.selectedProperty().addListener((observable, oldValue, newValue) -> {
      dataBox.getChildren().clear();
      // newValue == true means that the documentation will be displayed
      if (newValue) {
        if (docsRoot.getChildren().isEmpty()) {
          dataBox.getChildren().add(documentationHelp);
          content.setBottom(new HBox());
        } else {
          dataBox.getChildren().add(sipDocumentation);
          content.setBottom(docsBottom);
        }
      } else { // from the documentation to the representations
        dataBox.getChildren().add(sipFiles);
        content.setBottom(contentBottom);
      }
    });
    createDocsBottom();

    top.getChildren().addAll(space, toggleDocumentation);
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

    ignore = new Button(AppProperties.getLocalizedString("ignore"));
    ignore.setOnAction(event -> {
      InspectionTreeItem selected = (InspectionTreeItem) sipFiles.getSelectionModel().getSelectedItem();
      if (selected == null)
        return;
      Set<Path> paths = new HashSet<>();
      paths.add(selected.getPath());
      if (currentDescOb != null && currentDescOb instanceof SipPreview) {
        ((SipPreview) currentDescOb).ignoreContent(paths);
        TreeItem parent = selected.getParentDir();
        TreeItem child = (TreeItem) selected;
        parent.getChildren().remove(child);
      }
    });
    ignore.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    Button addRepresentation = new Button(AppProperties.getLocalizedString("InspectionPane.addRepresentation"));
    addRepresentation.setOnAction(event -> {
      int repCount = currentSIPNode.getSip().getRepresentations().size() + 1;
      SipRepresentation sipRep = new SipRepresentation("rep" + repCount);
      currentSIPNode.getSip().addRepresentation(sipRep);
      SipContentRepresentation sipContentRep = new SipContentRepresentation(sipRep);
      sipRoot.getChildren().add(sipContentRep);
    });
    addRepresentation.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    removeRepresentation = new Button(AppProperties.getLocalizedString("InspectionPane.removeRepresentation"));
    removeRepresentation.setOnAction(event -> {
      InspectionTreeItem selectedRaw = (InspectionTreeItem) sipFiles.getSelectionModel().getSelectedItem();
      if (selectedRaw instanceof SipContentRepresentation) {
        SipContentRepresentation selected = (SipContentRepresentation) selectedRaw;
        sipRoot.getChildren().remove(selectedRaw);
        currentSIPNode.getSip().removeRepresentation(selected.getRepresentation());
      }
    });
    removeRepresentation.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    contentBottom.getChildren().addAll(addRepresentation, removeRepresentation, ignore);
  }

  private void createDocsBottom() {
    docsBottom = new HBox(10);
    docsBottom.setPadding(new Insets(10, 10, 10, 10));
    docsBottom.setAlignment(Pos.CENTER_LEFT);

    Button remove = new Button(AppProperties.getLocalizedString("remove"));
    remove.setOnAction(event -> {
      List<InspectionTreeItem> selectedItems = new ArrayList<InspectionTreeItem>(
        sipDocumentation.getSelectionModel().getSelectedItems());
      for (InspectionTreeItem selected : selectedItems) {
        Set<Path> paths = new HashSet<>();
        if (selected instanceof SipContentDirectory || selected instanceof SipContentFile) {
          paths.add(selected.getPath());

          if (currentDescOb != null && currentDescOb instanceof SipPreview) {
            ((SipPreview) currentDescOb).removeDocumentation(paths);
            TreeItem parent = selected.getParentDir();
            TreeItem child = (TreeItem) selected;
            parent.getChildren().remove(child);
          }
        }
      }
      if (docsRoot.getChildren().isEmpty()) {
        dataBox.getChildren().clear();
        dataBox.getChildren().add(documentationHelp);
        content.setBottom(new HBox());
      }
    });
    remove.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    docsBottom.getChildren().addAll(remove);
  }

  private void createContent(SipPreviewNode node, boolean active) {
    SipContentDirectory newRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);
    if (active) {
      content.setCenter(loadingPane);
      content.setBottom(new HBox());
    }

    contentTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Set<SipRepresentation> representations = node.getSip().getRepresentations();
        for (SipRepresentation sr : representations) {
          SipContentRepresentation scr = new SipContentRepresentation(sr);
          for (TreeNode treeNode : sr.getFiles()) {
            TreeItem<Object> startingItem = recCreateSipContent(treeNode, scr);
            startingItem.setExpanded(true);
            scr.getChildren().add(startingItem);
          }
          scr.sortChildren();
          scr.setExpanded(true);
          newRoot.getChildren().add(scr);
        }
        return null;
      }
    };
    contentTask.setOnSucceeded(event -> {
      sipRoot = newRoot;
      if (active) {
        sipFiles.setRoot(sipRoot);
        content.setCenter(dataBox);
        content.setBottom(contentBottom);
      }
    });
    new Thread(contentTask).start();
  }

  private void createDocumentation(SipPreviewNode sip, boolean active) {
    SipContentDirectory newRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);

    if (active) {
      content.setCenter(loadingPane);
      content.setBottom(new HBox());
    }

    docsTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        for (TreeNode treeNode : sip.getSip().getDocumentation()) {
          TreeItem<Object> startingItem = recCreateSipContent(treeNode, newRoot);
          startingItem.setExpanded(true);
          newRoot.getChildren().add(startingItem);
        }
        newRoot.sortChildren();
        return null;
      }
    };
    docsTask.setOnSucceeded(event -> {
      docsRoot = newRoot;
      if (active) {
        sipDocumentation.setRoot(docsRoot);
        if (!docsRoot.getChildren().isEmpty()) {
          content.setCenter(documentationHelp);
          content.setBottom(docsBottom);
        } else
          content.setCenter(dataBox);
      }
    });
    new Thread(docsTask).start();
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
    rules.setMinHeight(200);

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

    emptyRulesPane.setOnDragOver(event -> {
      if (currentSchema != null && event.getGestureSource() instanceof SourceTreeCell) {
        event.acceptTransferModes(TransferMode.COPY);
        emptyText.setText(AppProperties.getLocalizedString("InspectionPane.onDrop"));
      }
      event.consume();
    });

    emptyRulesPane.setOnDragDropped(event -> {
      RodaIn.getSchemePane().startAssociation(currentSchema);
      event.consume();
    });

    emptyRulesPane.setOnDragExited(event -> {
      emptyText.setText(AppProperties.getLocalizedString("InspectionPane.help.ruleList"));
      event.consume();
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
   * @param sip
   *          The SipPreviewNode used to update the UI.
   * @see SipPreviewNode
   * @see SipContentDirectory
   * @see SipContentFile
   */
  public void update(SipPreviewNode sip) {
    setTop(topBox);
    setCenter(center);
    currentSIPNode = sip;
    currentDescOb = sip.getSip();
    currentSchema = null;
    if (contentTask != null && contentTask.isRunning()) {
      contentTask.cancel(true);
    }
    if (metadataTask != null && metadataTask.isRunning()) {
      metadataTask.cancel(true);
    }

    /* Top */
    paneTitle = new Label(sip.getValue());
    paneTitle.setWrapText(true);
    paneTitle.getStyleClass().add("title");

    HBox top = new HBox(5);
    top.setPadding(new Insets(0, 10, 10, 10));
    top.setAlignment(Pos.CENTER_LEFT);
    topIcon = new ImageView(sip.getIconBlack());
    top.getChildren().addAll(topIcon, paneTitle);
    Separator separatorTop = new Separator();

    topBox.setPadding(new Insets(10, 0, 10, 0));
    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, separatorTop);

    List<DescObjMetadata> metadataList = currentDescOb.getMetadataWithReplaces();
    if (metadataList != null && !metadataList.isEmpty()) {
      String schema = metadataList.get(0).getSchema();
      if (schema == null || "".equals(schema)) {
        validationButton.setVisible(false);
      } else
        validationButton.setVisible(true);
    }

    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metadataLoadingPane);
    metadataTask = new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        // metadata
        boolean result = false;
        List<DescObjMetadata> metadataList = currentDescOb.getMetadataWithReplaces();
        if (metadataList != null && !metadataList.isEmpty()) {
          String meta = metadataList.get(0).getContentDecoded();
          updateTextArea(meta);
          try {
            result = Utils.isEAD(metaText.getText());
          } catch (SAXException e) {
            log.info("Error validating metadata with the EAD2002 schema", e);
          }
        }
        return result;
      }
    };

    Task thisMetadataTask = metadataTask;
    metadataTask.setOnSucceeded((Void) -> {
      if (metadataTask != null && metadataTask == thisMetadataTask)
        showMetadataPane(metadataTask.getValue());
    });
    new Thread(metadataTask).start();

    /* Center */
    center.getChildren().clear();

    // content tree
    boolean documentation = toggleDocumentation.isSelected();
    createContent(sip, !documentation);
    createDocumentation(sip, documentation);
    if (documentation) {
      if (docsRoot.getChildren().isEmpty()) {
        content.setBottom(new HBox());
      } else {
        content.setBottom(docsBottom);
      }
    } else {
      content.setBottom(contentBottom);
    }

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
   * @param node
   *          The SchemaNode used to update the UI.
   * @see RuleCell
   * @see SchemaNode
   */
  public void update(SchemaNode node) {
    setTop(topBox);
    currentDescOb = node.getDob();
    currentSIPNode = null;
    currentSchema = node;
    if (contentTask != null && contentTask.isRunning()) {
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

    List<DescObjMetadata> metadataList = currentDescOb.getMetadataWithReplaces();
    if (metadataList != null && !metadataList.isEmpty()) {
      String schema = metadataList.get(0).getSchema();
      if (schema == null || "".equals(schema)) {
        validationButton.setVisible(false);
      } else
        validationButton.setVisible(true);
    }

    metadataTask = new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        // metadata
        List<DescObjMetadata> metadatas = currentDescOb.getMetadataWithReplaces();
        if (!metadatas.isEmpty()) {
          // For now we only get the first metadata object
          updateTextArea(metadatas.get(0).getContentDecoded());
        } else
          metaText.clear();
        boolean result = false;
        try {
          result = Utils.isEAD(metaText.getText());
        } catch (SAXException e) {
          log.info("Error validating metadata with the EAD2002 schema", e);
        }
        return result;
      }
    };
    Task thisMetadataTask = metadataTask;
    metadataTask.setOnSucceeded((Void) -> {
      if (metadataTask != null && metadataTask == thisMetadataTask) {
        showMetadataPane(metadataTask.getValue());
      }
    });
    new Thread(metadataTask).start();

    // rules
    updateRuleList();

    center.getChildren().addAll(metadata, rules);
    setCenter(center);
  }

  private void updateTextArea(String content) {
    metaText.replaceText(content);
    metaText.setStyleSpans(0, XMLEditor.computeHighlighting(content));
  }

  /**
   * Shows the help pane.
   */
  public void showHelp() {
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
    if (currentSchema != null) {
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
   * Adds documentation to the current SIP.
   *
   * @param target
   *          The item to where the documentation should go. This is NOT the SIP
   *          where we will be adding the documentation. This must be either an
   *          already added folder to the documentation or null (in which case
   *          we'll add to the root of the tree).
   */
  public void addDocumentationToSIP(TreeItem target) {
    Set<Path> paths = new HashSet<>();
    Set<SourceTreeItem> items = RodaIn.getSourceSelectedItems();
    for (SourceTreeItem item : items) {
      paths.add(Paths.get(item.getPath()));
    }

    addDocumentationToSIP(target, paths);
  }

  /**
   * Adds documentation to the current SIP.
   *
   * @param target
   *          The item to where the documentation should go. This is NOT the SIP
   *          where we will be adding the documentation. This must be either an
   *          already added folder to the documentation or null (in which case
   *          we'll add to the root of the tree).
   * @param paths
   *          The paths to be used to create the documentation.
   */
  public void addDocumentationToSIP(TreeItem target, Set<Path> paths) {
    Set<ContentFilter> filters = new HashSet<>();
    filters.add(new ContentFilter());

    DocumentationCreator dc = new DocumentationCreator(filters, paths);
    Set<TreeNode> result = dc.start();

    if (target instanceof SipContentDirectory) {
      SipContentDirectory dir = (SipContentDirectory) target;
      for (TreeNode tn : result)
        dir.getTreeNode().add(tn);
    } else
      currentSIPNode.getSip().addDocumentation(result);

    SipContentDirectory parent = target != null ? (SipContentDirectory) target : docsRoot;
    for (TreeNode treeNode : result) {
      TreeItem<Object> startingItem = recCreateSipContent(treeNode, parent);
      parent.getChildren().add(startingItem);
    }
    parent.sortChildren();
  }

  public void representationSelected(boolean b) {
    if (b) {
      removeRepresentation.setDisable(false);
      ignore.setDisable(true);
    } else {
      removeRepresentation.setDisable(true);
      ignore.setDisable(false);
    }
  }

  public List<InspectionTreeItem> getDocumentationSelectedItems() {
    List<InspectionTreeItem> result = new ArrayList<InspectionTreeItem>(
      sipDocumentation.getSelectionModel().getSelectedItems());
    return result;
  }

  public List<InspectionTreeItem> getDataSelectedItems() {
    List<InspectionTreeItem> result = new ArrayList<InspectionTreeItem>(
      sipFiles.getSelectionModel().getSelectedItems());
    return result;
  }
}
