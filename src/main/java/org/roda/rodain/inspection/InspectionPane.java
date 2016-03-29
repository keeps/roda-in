package org.roda.rodain.inspection;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.inspection.documentation.DocumentationCreator;
import org.roda.rodain.inspection.documentation.SipDocumentationTreeView;
import org.roda.rodain.rules.MetadataTypes;
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
import org.roda.rodain.utils.ModalStage;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.roda_project.commons_ip.utils.EARKEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 26-10-2015.
 */
public class InspectionPane extends BorderPane {
  private static final Logger log = LoggerFactory.getLogger(InspectionPane.class.getName());
  private VBox topBox;
  private VBox center;
  private HBox topSpace;
  private Stage stage;

  private SipPreviewNode currentSIPNode;
  private SchemaNode currentSchema;
  private DescriptionObject currentDescOb;
  private ImageView topIcon;
  private Label paneTitle;

  private VBox centerHelp;
  // Metadata
  private VBox metadata, metadataHelpBox;
  private CodeArea metaText;
  private GridPane metadataGrid;
  private ScrollPane metadataFormWrapper;
  private ToggleButton toggleForm;
  private HBox metadataLoadingPane, metadataTopBox;
  private Button validationButton, addMetadata, removeMetadata;
  private ComboBox<UIPair> metadataCombo;
  private Set<Control> topButtons;
  private Separator metadataTopSeparator;
  // SIP Content
  private BorderPane content;
  private VBox dataBox, documentationHelp;
  private SipDataTreeView sipFiles;
  private SipDocumentationTreeView sipDocumentation;
  private SipContentDirectory sipRoot, docsRoot;
  private HBox loadingPane, contentBottom, docsBottom;
  private static Image loadingGif;
  private Task<Void> contentTask, docsTask, metadataTask;
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
    this.stage = stage;

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
    metadataGrid.setStyle(AppProperties.getStyle("backgroundWhite"));
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setPercentWidth(20);
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setPercentWidth(80);
    metadataGrid.getColumnConstraints().addAll(column1, column2);

    metadataFormWrapper = new ScrollPane();
    metadataFormWrapper.setContent(metadataGrid);
    metadataFormWrapper.setFitToWidth(true);

    createMetadataHelp();
    createMetadataTop();
    createMetadataTextBox();
    metadata.getChildren().addAll(metadataTopBox, metaText);
  }

  private void createMetadataHelp() {
    metadataHelpBox = new VBox();
    metadataHelpBox.setPadding(new Insets(0, 10, 0, 10));
    VBox.setVgrow(metadataHelpBox, Priority.ALWAYS);
    metadataHelpBox.setAlignment(Pos.CENTER);

    VBox box = new VBox(40);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(10, 10, 10, 10));
    box.setMaxWidth(355);
    box.setMaxHeight(200);
    box.setMinHeight(200);

    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    Label title = new Label(I18n.t("InspectionPane.addMetadata"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    Button addMetadata = new Button(I18n.t("add"));
    addMetadata.setMinHeight(65);
    addMetadata.setMinWidth(130);
    addMetadata.setMaxWidth(130);
    addMetadata.setOnAction(event -> addMetadataAction());
    addMetadata.getStyleClass().add("helpButton");

    box.getChildren().addAll(titleBox, addMetadata);
    metadataHelpBox.getChildren().add(box);
  }

  private void createMetadataTextBox() {
    metaText = new CodeArea();
    VBox.setVgrow(metaText, Priority.ALWAYS);
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

  private void createMetadataTop() {
    metadataTopBox = new HBox();
    metadataTopBox.getStyleClass().add("hbox");
    metadataTopBox.setPadding(new Insets(5, 10, 5, 10));
    metadataTopBox.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label(I18n.t("InspectionPane.metadata"));
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    toggleForm = new ToggleButton();
    toggleForm.setTooltip(new Tooltip(I18n.t("InspectionPane.textContent")));
    Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CODE, Color.WHITE);
    Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.LIST, Color.WHITE);
    ImageView toggleImage = new ImageView();
    toggleForm.setGraphic(toggleImage);
    toggleImage.imageProperty().bind(Bindings.when(toggleForm.selectedProperty()).then(selected).otherwise(unselected));
    toggleForm.selectedProperty().addListener((observable, oldValue, newValue) -> {
      saveMetadata();
      // newValue == true means that the form will be displayed
      if (newValue) {
        toggleForm.setTooltip(new Tooltip(I18n.t("InspectionPane.textContent")));
        metadata.getChildren().remove(metaText);
        metadataGrid.getChildren().clear();
        updateForm();
        if (!metadata.getChildren().contains(metadataFormWrapper)) {
          metadata.getChildren().add(metadataFormWrapper);
        }
      } else { // from the form to the metadata text
        toggleForm.setTooltip(new Tooltip(I18n.t("InspectionPane.form")));
        metadata.getChildren().remove(metadataFormWrapper);
        if (!metadata.getChildren().contains(metaText))
          metadata.getChildren().add(metaText);
      }
    });

    validationButton = new Button();
    validationButton.setTooltip(new Tooltip(I18n.t("InspectionPane.validate")));
    validationButton
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.WHITE)));
    validationButton.setOnAction(event -> validationAction());

    addMetadata = new Button();
    addMetadata.setTooltip(new Tooltip(I18n.t("InspectionPane.addMetadata")));
    addMetadata.setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PLUS, Color.WHITE)));
    addMetadata.setOnAction(event -> addMetadataAction());

    removeMetadata = new Button();
    removeMetadata.setTooltip(new Tooltip(I18n.t("InspectionPane.removeMetadata")));
    removeMetadata
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.MINUS, Color.WHITE)));
    removeMetadata.setOnAction(event -> removeMetadataAction());

    metadataTopSeparator = new Separator(Orientation.VERTICAL);

    topButtons = new HashSet<>();
    topButtons.add(addMetadata);
    topButtons.add(toggleForm);
    topButtons.add(validationButton);

    metadataCombo = new ComboBox<>();
    metadataCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      // only display the comboBox if there's more than one metadata object
      if (metadataCombo.getItems().size() > 1) {
        topButtons.add(metadataCombo);
        topButtons.add(removeMetadata);
      } else {
        topButtons.remove(metadataCombo);
        topButtons.remove(removeMetadata);
      }
      updateMetadataTop();
    });
    metadataCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        updateSelectedMetadata((DescObjMetadata) newValue.getKey());
      } else {
        showMetadataHelp();
      }
    });

    metadataTopBox.getChildren().addAll(titleLabel, space);
    updateMetadataTop();
  }

  private void updateMetadataTop() {
    metadataTopBox.getChildren().removeAll(addMetadata, removeMetadata, toggleForm, validationButton, metadataCombo);
    metadataTopBox.getChildren().remove(metadataTopSeparator);

    if (topButtons.contains(toggleForm))
      metadataTopBox.getChildren().add(toggleForm);

    if (topButtons.contains(validationButton))
      metadataTopBox.getChildren().add(validationButton);

    if (topButtons.contains(toggleForm) || topButtons.contains(validationButton))
      metadataTopBox.getChildren().add(metadataTopSeparator);

    if (topButtons.contains(addMetadata))
      metadataTopBox.getChildren().add(addMetadata);

    if (topButtons.contains(removeMetadata))
      metadataTopBox.getChildren().add(removeMetadata);

    if (topButtons.contains(metadataCombo))
      metadataTopBox.getChildren().add(metadataCombo);
  }

  private void removeMetadataAction() {
    if (metadataCombo.getSelectionModel().getSelectedIndex() == -1)
      return;

    String content = I18n.t("InspectionPane.removeMetadata.content");
    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t("InspectionPane.removeMetadata.header"));
    dlg.setTitle(I18n.t("InspectionPane.removeMetadata.title"));
    dlg.setContentText(content);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);
    dlg.showAndWait();

    if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      DescObjMetadata toRemove = (DescObjMetadata) metadataCombo.getSelectionModel().getSelectedItem().getKey();
      currentDescOb.getMetadata().remove(toRemove);
      metadataCombo.getItems().remove(metadataCombo.getSelectionModel().getSelectedItem());
      metadataCombo.getSelectionModel().selectFirst();
      RodaIn.getSchemePane().setModifiedPlan(true);
    }
  }

  private void addMetadataAction() {
    ModalStage modalStage = new ModalStage(stage);
    AddMetadataPane addMetadataPane = new AddMetadataPane(modalStage, currentDescOb);
    modalStage.setRoot(addMetadataPane);
  }

  private void validationAction() {
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
            UIPair selectedInCombo = metadataCombo.getSelectionModel().getSelectedItem();
            if (selectedInCombo != null) {
              DescObjMetadata metadata = (DescObjMetadata) selectedInCombo.getKey();
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
          UIPair selectedInCombo = metadataCombo.getSelectionModel().getSelectedItem();
          if (selectedInCombo != null) {
            DescObjMetadata metadataObj = (DescObjMetadata) selectedInCombo.getKey();
            if (metadataObj.getTemplateType() != null && "ead".equals(metadataObj.getTemplateType())) {
              topButtons.add(toggleForm);
              updateMetadataTop();
              if (metadata.getChildren().contains(metaText)) {
                toggleForm.setSelected(false);
              } else
                toggleForm.setSelected(true);
            }
          }
        }
      } else {
        popOver.updateContent(false, message.toString());
        topButtons.remove(toggleForm);
        updateMetadataTop();
      }
    });
    new Thread(validationTask).start();
  }

  private void updateForm() {
    Map<String, MetadataValue> metadataValues = getMetadataValues();
    if (metadataValues == null || metadataValues.isEmpty()) {
      noForm();
      return;
    }
    int i = 0;
    for (MetadataValue metadataValue : metadataValues.values()) {
      Label label = new Label(metadataValue.getTitle());
      label.getStyleClass().add("formLabel");

      TextField textField = new TextField(metadataValue.getValue());
      HBox.setHgrow(textField, Priority.ALWAYS);
      textField.setUserData(metadataValue);
      textField.textProperty().addListener((observable2, oldValue2, newValue2) -> {
        metadataValue.setValue(newValue2);
      });
      if (metadataValue.getId().equals("title")) {
        textField.setId("descObjTitle");
        paneTitle.textProperty().bind(textField.textProperty());
        if (currentSIPNode != null) {
          textField.textProperty().bindBidirectional(currentSIPNode.valueProperty());
        } else {
          if (currentSchema != null) {
            textField.textProperty().bindBidirectional(currentSchema.valueProperty());
          }
        }
      }

      metadataGrid.add(label, 0, i);
      metadataGrid.add(textField, 1, i);
      i++;
    }
  }

  private void noForm() {
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metaText);
    topButtons.remove(toggleForm);
    updateMetadataTop();
  }

  private Map<String, MetadataValue> getMetadataValues() {
    if (currentDescOb != null) {
      UIPair selectedPair = metadataCombo.getSelectionModel().getSelectedItem();
      if (selectedPair != null) {
        DescObjMetadata dom = (DescObjMetadata) selectedPair.getKey();
        if (dom != null)
          return currentDescOb.getMetadataValueMap(dom);
      }
    }
    // error, there is no SIP or SchemaNode selected
    return null;
  }

  /**
   * Saves the metadata from the text area in the SIP.
   */
  public void saveMetadata() {
    if (metadataCombo == null)
      return;
    UIPair selectedObject = metadataCombo.getSelectionModel().getSelectedItem();
    DescObjMetadata selectedDescObjMetadata;
    if (selectedObject != null && selectedObject.getKey() instanceof DescObjMetadata)
      selectedDescObjMetadata = (DescObjMetadata) selectedObject.getKey();
    else
      return;

    if (metadata.getChildren().contains(metadataFormWrapper)) {
      if (currentDescOb != null) {
        updateTextArea(currentDescOb.getMetadataWithReplaces(selectedDescObjMetadata));
      }
    } else {
      String oldMetadata = null, newMetadata = null;
      if (currentDescOb != null) {
        newMetadata = metaText.getText();
        oldMetadata = selectedDescObjMetadata.getContentDecoded();
      }
      // only update if there's been modifications or there's no old
      // metadata and the new isn't empty
      boolean update = false;
      if (selectedDescObjMetadata.getType() == MetadataTypes.TEMPLATE) {
        currentDescOb.updatedMetadata(selectedDescObjMetadata);
      } else {
        if (newMetadata != null) {
          if (oldMetadata == null)
            update = true;
          else if (!oldMetadata.equals(newMetadata))
            update = true;
        }
      }

      if (update) {
        if (currentDescOb != null) {
          selectedDescObjMetadata.setContentDecoded(newMetadata);
        } else {
          DescObjMetadata newObjMetadata = new DescObjMetadata();
          newObjMetadata.setContentEncoding("Base64");
          newObjMetadata.setContentDecoded(newMetadata);
          currentDescOb.getMetadata().add(newObjMetadata);
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
    Label title = new Label(I18n.t("InspectionPane.help.title"));
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
    Label title = new Label(I18n.t("InspectionPane.docsHelp.title"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    box.getChildren().addAll(titleBox);
    documentationHelp.getChildren().add(box);

    documentationHelp.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (event.getGestureSource() instanceof SourceTreeCell || db.hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
        title.setText(I18n.t("InspectionPane.onDropDocs"));
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
      content.setCenter(dataBox);
      content.setBottom(docsBottom);
      event.consume();
    });

    documentationHelp.setOnDragExited(event -> {
      title.setText(I18n.t("InspectionPane.docsHelp.title"));
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

    Label title = new Label(I18n.t("data"));
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
    toggleDocumentation.setTooltip(new Tooltip(I18n.t("documentation")));
    Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.OPEN_FOLDER, Color.WHITE);
    Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.BOOK, Color.WHITE);
    ImageView toggleImage = new ImageView();
    toggleDocumentation.setGraphic(toggleImage);
    toggleImage.imageProperty()
      .bind(Bindings.when(toggleDocumentation.selectedProperty()).then(selected).otherwise(unselected));
    title.textProperty().bind(
      Bindings.when(toggleDocumentation.selectedProperty()).then(I18n.t("documentation")).otherwise(I18n.t("data")));

    toggleDocumentation.selectedProperty().addListener((observable, oldValue, newValue) -> {
      dataBox.getChildren().clear();
      // newValue == true means that the documentation will be displayed
      if (newValue) {
        toggleDocumentation.setTooltip(new Tooltip(I18n.t("data")));
        if (docsRoot.getChildren().isEmpty()) {
          dataBox.getChildren().add(documentationHelp);
          content.setBottom(new HBox());
        } else {
          dataBox.getChildren().add(sipDocumentation);
          content.setBottom(docsBottom);
        }
      } else { // from the documentation to the representations
        toggleDocumentation.setTooltip(new Tooltip(I18n.t("documentation")));
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

    ignore = new Button(I18n.t("ignore"));
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

    Button addRepresentation = new Button(I18n.t("InspectionPane.addRepresentation"));
    addRepresentation.setOnAction(event -> {
      int repCount = currentSIPNode.getSip().getRepresentations().size() + 1;
      SipRepresentation sipRep = new SipRepresentation("rep" + repCount);
      currentSIPNode.getSip().addRepresentation(sipRep);
      SipContentRepresentation sipContentRep = new SipContentRepresentation(sipRep);
      sipRoot.getChildren().add(sipContentRep);
    });
    addRepresentation.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    removeRepresentation = new Button(I18n.t("InspectionPane.removeRepresentation"));
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

    Button remove = new Button(I18n.t("remove"));
    remove.setOnAction(event -> {
      List<InspectionTreeItem> selectedItems = new ArrayList<>(sipDocumentation.getSelectionModel().getSelectedItems());
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
          content.setCenter(dataBox);
          content.setBottom(docsBottom);
        } else {
          content.setCenter(documentationHelp);
          content.setBottom(new HBox());
        }
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

    Label title = new Label(I18n.t("InspectionPane.rules"));
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
    Label emptyText = new Label(I18n.t("InspectionPane.help.ruleList"));
    emptyText.getStyleClass().add("helpTitle");
    emptyText.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(emptyText);

    emptyRulesPane.setOnDragOver(event -> {
      if (currentSchema != null && event.getGestureSource() instanceof SourceTreeCell) {
        event.acceptTransferModes(TransferMode.COPY);
        emptyText.setText(I18n.t("InspectionPane.onDrop"));
      }
      event.consume();
    });

    emptyRulesPane.setOnDragDropped(event -> {
      RodaIn.getSchemePane().startAssociation(currentSchema);
      event.consume();
    });

    emptyRulesPane.setOnDragExited(event -> {
      emptyText.setText(I18n.t("InspectionPane.help.ruleList"));
      event.consume();
    });

    box.getChildren().addAll(titleBox);
    emptyRulesPane.getChildren().add(box);
    rules.setCenter(emptyRulesPane);
  }

  private void updateSelectedMetadata(DescObjMetadata dom) {
    metadata.getChildren().removeAll(metadataFormWrapper, metaText, metadataHelpBox);
    if (!metadata.getChildren().contains(metadataLoadingPane))
      metadata.getChildren().add(metadataLoadingPane);

    metadataTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        // metadata
        if (dom != null) {
          String meta = currentDescOb.getMetadataWithReplaces(dom);
          updateTextArea(meta);
        }
        return null;
      }
    };

    Task thisMetadataTask = metadataTask;
    metadataTask.setOnSucceeded((Void) -> {
      if (metadataTask != null && metadataTask == thisMetadataTask) {
        Map<String, MetadataValue> values = currentDescOb.getMetadataValueMap(dom);
        boolean show = values != null && !values.isEmpty();
        showMetadataPane(show);
      }

      String schema = dom.getSchema();
      if (schema == null || "".equals(schema)) {
        topButtons.remove(validationButton);
      } else
        topButtons.add(validationButton);
      updateMetadataTop();
    });
    metadataTask.setOnFailed(event -> {
      // recreate the metadata text box and retry
      createMetadataTextBox();
      metadata.getChildren().clear();
      metadata.getChildren().addAll(metadataTopBox, metaText);
      int selectedIndex = metadataCombo.getSelectionModel().getSelectedIndex();
      updateMetadataCombo();
      metadataCombo.getSelectionModel().select(selectedIndex);
    });
    new Thread(metadataTask).start();
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
    top.setPadding(new Insets(0, 10, 5, 10));
    top.setAlignment(Pos.CENTER_LEFT);
    topIcon = new ImageView(sip.getIconBlack());
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);
    // Content Type combo box
    ComboBox<EARKEnums.ContentType> contentType = new ComboBox<>();
    List<EARKEnums.ContentType> contTypeList = new ArrayList<>();
    for (EARKEnums.ContentType ct : EARKEnums.ContentType.values()) {
      contTypeList.add(ct);
    }
    // sort the list as strings
    Collections.sort(contTypeList, (o1, o2) -> o1.toString().compareTo(o2.toString()));
    contentType.setItems(new ObservableListWrapper<>(contTypeList));
    contentType.getSelectionModel().select(sip.getSip().getContentType());
    contentType.valueProperty().addListener((obs, old, newValue) -> sip.getSip().setContentType(newValue));
    contentType.setMinWidth(85);

    top.getChildren().addAll(topIcon, paneTitle, space, contentType);
    Separator separatorTop = new Separator();

    topBox.setPadding(new Insets(10, 0, 10, 0));
    topBox.getChildren().clear();
    topBox.getChildren().addAll(top, separatorTop);

    updateMetadataCombo();

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

    updateMetadataCombo();

    // rules
    updateRuleList();

    center.getChildren().addAll(metadata, rules);
    setCenter(center);
  }

  private void updateMetadataCombo() {
    metadataCombo.getSelectionModel().clearSelection();
    metadataCombo.getItems().clear();
    List<DescObjMetadata> metadataList = currentDescOb.getMetadata();
    List<UIPair> comboList = new ArrayList<>();
    for (DescObjMetadata dom : metadataList) {
      comboList.add(new UIPair(dom, dom.getId()));
    }
    if (comboList.isEmpty()) {
      showMetadataHelp();
    } else {
      metadataCombo.getItems().addAll(comboList);
      metadataCombo.getSelectionModel().selectFirst();
    }
  }

  private void showMetadataHelp() {
    topButtons.clear();
    topButtons.add(addMetadata);
    updateMetadataTop();
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metadataHelpBox);
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

  private void showMetadataPane(boolean toShow) {
    metadata.getChildren().clear();
    metadata.getChildren().add(metadataTopBox);

    if (toShow) {
      topButtons.add(toggleForm);
      updateMetadataTop();
      toggleForm.setSelected(false);
      toggleForm.fire();
    } else {
      topButtons.remove(toggleForm);
      updateMetadataTop();
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
    List<InspectionTreeItem> result = new ArrayList<>(sipDocumentation.getSelectionModel().getSelectedItems());
    return result;
  }

  public List<InspectionTreeItem> getDataSelectedItems() {
    List<InspectionTreeItem> result = new ArrayList<>(sipFiles.getSelectionModel().getSelectedItems());
    return result;
  }

  public void updateMetadataList(DescriptionObject descriptionObject) {
    if (descriptionObject == currentDescOb) {
      updateMetadataCombo();
      metadataCombo.getSelectionModel().clearSelection();
      metadataCombo.getSelectionModel().selectLast();
      RodaIn.getSchemePane().setModifiedPlan(true);
    }
  }

  public void showAddMetadataError(DescObjMetadata metadataToAdd) {
    String content = String.format(I18n.t("InspectionPane.addMetadataError.content"), metadataToAdd.getId());
    Alert dlg = new Alert(Alert.AlertType.INFORMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t("InspectionPane.addMetadataError.header"));
    dlg.setTitle(I18n.t("InspectionPane.addMetadataError.title"));
    dlg.setContentText(content);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);
    dlg.getDialogPane().setMinHeight(180);
    dlg.show();
  }
}
