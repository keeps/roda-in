package org.roda.rodain.ui.inspection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.fxmisc.richtext.CodeArea;
import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.Constants.SipType;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.rules.filters.ContentFilter;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.IPContentType;
import org.roda.rodain.core.schema.RepresentationContentType;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.core.template.TemplateFieldValue;
import org.roda.rodain.ui.ModalStage;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.inspection.trees.ContentCreator;
import org.roda.rodain.ui.inspection.trees.InspectionTreeItem;
import org.roda.rodain.ui.inspection.trees.SipContentDirectory;
import org.roda.rodain.ui.inspection.trees.SipContentFile;
import org.roda.rodain.ui.inspection.trees.SipContentRepresentation;
import org.roda.rodain.ui.inspection.trees.SipDataTreeView;
import org.roda.rodain.ui.inspection.trees.SipDocumentationTreeView;
import org.roda.rodain.ui.rules.Rule;
import org.roda.rodain.ui.schema.ui.SchemaNode;
import org.roda.rodain.ui.schema.ui.SipPreviewNode;
import org.roda.rodain.ui.source.SourceTreeCell;
import org.roda.rodain.ui.source.items.SourceTreeItem;
import org.roda.rodain.ui.utils.FontAwesomeImageCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 26-10-2015.
 */
public class InspectionPane extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(InspectionPane.class.getName());
  private HBox topBox;
  private VBox center;
  private HBox topSubtitle;
  private Stage stage;

  private SipPreviewNode currentSIPNode;
  private SchemaNode currentSchema;
  private Sip currentDescOb;
  private List<TreeItem<String>> selectedItems;
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
  private ComboBox<Pair> metadataCombo;
  private Set<Control> topButtons;
  private Separator metadataTopSeparator;
  private boolean textBoxCancelledChange = false;
  private VBox multSelectedBottom;
  // SIP Content
  private BorderPane content;
  private VBox dataBox, documentationHelp;
  private SipDataTreeView sipFiles;
  private SipDocumentationTreeView sipDocumentation;
  private SipContentDirectory sipRoot, docsRoot;
  private HBox loadingPane, contentBottom, docsBottom;
  private static Image loadingGif;
  private Task<Void> contentTask, docsTask, metadataTask;
  private Button ignore;
  private ToggleButton toggleDocumentation;
  // Rules
  private BorderPane rules;
  private ListView<RuleCell> ruleList;
  private VBox emptyRulesPane;

  private HBox exportBox;
  private Button export;

  private Button editRepresentationTypeButton;
  private Label representationTypeLabel;
  private SipContentRepresentation currentRepresentation;
  private HBox representationTypeBox;

  private ComboBox<Pair<IPContentType, String>> contentType;

  private Button editButton;

  /**
   * Creates a new inspection pane.
   *
   * @param stage
   *          The primary stage of the application
   */
  public InspectionPane(Stage stage) {
    this.stage = stage;

    setPadding(new Insets(10, 10, 0, 10));

    createCenterHelp();
    createDocumentationHelp();
    createTop();
    createMetadata();
    createContent();
    createRulesList();
    createExportBox();
    createLoadingPanes();
    createMultipleSelectedBottom();
    center = new VBox(10);
    center.setPadding(new Insets(10, 0, 10, 0));

    setCenter(centerHelp);
    setBottom(exportBox);
    setTop(topBox);

    metadata.minHeightProperty().bind(stage.heightProperty().multiply(0.40));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.3));

    editRepresentationTypeButton.setVisible(false);

    representationTypeBox = new HBox();
    representationTypeBox.getStyleClass().add(Constants.CSS_TITLE_BOX);
    representationTypeBox.setAlignment(Pos.CENTER);

  }

  private void createTop() {
    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_TITLE).toUpperCase());
    title.getStyleClass().add(Constants.CSS_TITLE);
    title.setMinWidth(110);
    topSubtitle = new HBox(1);
    HBox.setHgrow(topSubtitle, Priority.ALWAYS);

    topBox = new HBox(15);
    topBox.getStyleClass().add(Constants.CSS_TITLE_BOX);
    topBox.getChildren().addAll(title, topSubtitle);
    topBox.setPadding(new Insets(15, 15, 15, 15));
    topBox.setAlignment(Pos.CENTER_LEFT);

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(topBox, new Tooltip(I18n.help("tooltip.inspectionPanel")));
    }
  }

  public void resetTop() {
    createTop();
    setTop(topBox);
  }

  private void createMetadata() {
    metadata = new VBox();
    metadata.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    VBox.setVgrow(metadata, Priority.ALWAYS);

    metadataGrid = new GridPane();
    metadataGrid.setVgap(5);
    metadataGrid.setPadding(new Insets(5, 5, 5, 5));
    metadataGrid.setStyle(ConfigurationManager.getStyle(Constants.CSS_BACKGROUNDWHITE));
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setPercentWidth(25);
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setPercentWidth(75);
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
    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_ADDMETADATA));
    title.getStyleClass().add(Constants.CSS_HELPTITLE);
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    Button addMetadataBtn = new Button(I18n.t(Constants.I18N_ADD));
    addMetadataBtn.setMinHeight(65);
    addMetadataBtn.setMinWidth(130);
    addMetadataBtn.setMaxWidth(130);
    addMetadataBtn.setOnAction(event -> addMetadataAction());
    addMetadataBtn.getStyleClass().add(Constants.CSS_HELPBUTTON);

    box.getChildren().addAll(titleBox, addMetadataBtn);
    metadataHelpBox.getChildren().add(box);
  }

  private void createMetadataTextBox() {
    metaText = new CodeArea();
    metaText.setWrapText(true);
    VBox.setVgrow(metaText, Priority.ALWAYS);
    metaText.textProperty().addListener((observable, oldValue, newValue) -> {
      metaText.setStyleSpans(0, XMLEditor.computeHighlighting(newValue));
      Pair selectedPair = metadataCombo.getSelectionModel().getSelectedItem();
      DescriptiveMetadata selected = (DescriptiveMetadata) selectedPair.getKey();

      String metadataLabel = "";
      if (selected.getMetadataType() != null) {
        metadataLabel = "( " + selected.getMetadataType();
        if (selected.getMetadataVersion() != null) {
          metadataLabel += " : " + selected.getMetadataVersion();
        }
        metadataLabel += " )";
      }

      if (oldValue != null && !"".equals(oldValue) && topButtons != null && topButtons.contains(toggleForm)
        && !textBoxCancelledChange) {
        String changeContent = I18n.t(Constants.I18N_INSPECTIONPANE_CHANGE_TEMPLATE_CONTENT);
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.initStyle(StageStyle.UNDECORATED);
        dlg.setHeaderText(I18n.t(Constants.I18N_INSPECTIONPANE_CHANGE_TEMPLATE_HEADER));
        dlg.setTitle(I18n.t(Constants.I18N_INSPECTIONPANE_CHANGE_TEMPLATE_TITLE));
        dlg.setContentText(changeContent);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(stage);
        dlg.showAndWait();

        if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
          selected.setContentDecoded(newValue);
          selected.setValues(null);
          selected.setCreatorOption(MetadataOption.NEW_FILE);
          topButtons.remove(toggleForm);
          updateMetadataTop();
        } else {
          textBoxCancelledChange = true;
          updateTextArea(currentDescOb.getMetadataWithReplaces(selected));
        }
      } else {
        textBoxCancelledChange = false;
      }
    });

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
        textBoxCancelledChange = true;
        saveMetadataPrivate();
      }
    });
  }

  private void createMetadataTop() {
    metadataTopBox = new HBox(10);
    metadataTopBox.getStyleClass().add(Constants.CSS_HBOX);
    metadataTopBox.setPadding(new Insets(5, 15, 5, 15));
    metadataTopBox.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_METADATA).toUpperCase());
    titleLabel.getStyleClass().add(Constants.CSS_TITLE);
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    toggleForm = new ToggleButton();
    toggleForm.getStyleClass().add(Constants.CSS_DARK_BUTTON);
    toggleForm.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_TEXTCONTENT)));
    Platform.runLater(() -> {
      Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CODE, Color.WHITE);
      Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.LIST, Color.WHITE);
      ImageView toggleImage = new ImageView();
      toggleForm.setGraphic(toggleImage);
      toggleImage.imageProperty()
        .bind(Bindings.when(toggleForm.selectedProperty()).then(selected).otherwise(unselected));
    });
    toggleForm.selectedProperty().addListener((observable, oldValue, newValue) -> {
      textBoxCancelledChange = true;
      saveMetadataPrivate();
      // newValue == true means that the form will be displayed
      if (newValue) {
        toggleForm.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_TEXTCONTENT)));
        textBoxCancelledChange = false;
        metadata.getChildren().remove(metaText);
        metadataGrid.getChildren().clear();
        updateForm();
        if (!metadata.getChildren().contains(metadataFormWrapper)) {
          metadata.getChildren().add(metadataFormWrapper);
        }
      } else { // from the form to the metadata text
        toggleForm.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_FORM)));
        metadata.getChildren().remove(metadataFormWrapper);
        if (!metadata.getChildren().contains(metaText))
          metadata.getChildren().add(metaText);
      }
    });

    validationButton = new Button();
    validationButton.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_VALIDATE)));
    Platform.runLater(() -> validationButton
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.WHITE))));

    validationButton.setOnAction(event -> validationAction());
    validationButton.getStyleClass().add(Constants.CSS_DARK_BUTTON);

    addMetadata = new Button();
    addMetadata.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_ADDMETADATA)));
    Platform.runLater(() -> addMetadata
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PLUS, Color.WHITE))));
    addMetadata.setOnAction(event -> addMetadataAction());
    addMetadata.getStyleClass().add(Constants.CSS_DARK_BUTTON);

    removeMetadata = new Button();
    removeMetadata.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_REMOVE_METADATA)));
    Platform.runLater(() -> removeMetadata
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.MINUS, Color.WHITE))));
    removeMetadata.setOnAction(event -> removeMetadataAction());
    removeMetadata.getStyleClass().add(Constants.CSS_DARK_BUTTON);

    metadataTopSeparator = new Separator(Orientation.VERTICAL);

    topButtons = new HashSet<>();
    topButtons.add(addMetadata);
    topButtons.add(toggleForm);
    topButtons.add(validationButton);

    metadataCombo = new ComboBox<>();
    metadataCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      // only display the comboBox if there's more than one metadata
      // object
      if (metadataCombo.getItems().size() > 1) {
        topButtons.add(metadataCombo);
        topButtons.add(removeMetadata);
      } else {
        topButtons.add(metadataCombo);
        topButtons.remove(removeMetadata);
      }
      updateMetadataTop();
    });

    metadataCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        if (oldValue != null) {
          saveMetadataPrivate((DescriptiveMetadata) oldValue.getKey());
        }
        // we need this to prevent the alert from being shown
        textBoxCancelledChange = true;
        updateSelectedMetadata((DescriptiveMetadata) newValue.getKey());
      } else {
        showMetadataHelp();
      }
    });
    metadataTopBox.getChildren().add(titleLabel);

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(metadataTopBox, new Tooltip(I18n.help("tooltip.inspectionPanel.metadata")));
    }

    metadataTopBox.getChildren().add(space);
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

    metadataTopBox.requestFocus();
  }

  private void removeMetadataAction() {
    if (metadataCombo.getSelectionModel().getSelectedIndex() == -1)
      return;

    String remContent = I18n.t(Constants.I18N_INSPECTIONPANE_REMOVE_METADATA_CONTENT);
    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t(Constants.I18N_INSPECTIONPANE_REMOVE_METADATA_HEADER));
    dlg.setTitle(I18n.t(Constants.I18N_INSPECTIONPANE_REMOVE_METADATA_TITLE));
    dlg.setContentText(remContent);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);
    dlg.showAndWait();

    if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      DescriptiveMetadata toRemove = (DescriptiveMetadata) metadataCombo.getSelectionModel().getSelectedItem().getKey();
      currentDescOb.getMetadata().remove(toRemove);
      metadataCombo.getItems().remove(metadataCombo.getSelectionModel().getSelectedItem());
      metadataCombo.getSelectionModel().selectFirst();
    }
  }

  private void addMetadataAction() {
    ModalStage modalStage = new ModalStage(stage);
    AddMetadataPane addMetadataPane = new AddMetadataPane(modalStage, currentDescOb);
    modalStage.setRoot(addMetadataPane, false);
  }

  private void validationAction() {
    if (metadata.getChildren().contains(metadataFormWrapper)) {
      textBoxCancelledChange = true;
      saveMetadataPrivate();
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
            Pair selectedInCombo = metadataCombo.getSelectionModel().getSelectedItem();
            if (selectedInCombo != null) {
              DescriptiveMetadata dom = (DescriptiveMetadata) selectedInCombo.getKey();
              if (Controller.validateSchema(metaText.getText(), dom.getSchema())) {
                result = true;
              }
            }
          }
        } catch (SAXException e) {
          LOGGER.info("Error validating schema", e);
          message.append(e.getMessage());
        }
        return result;
      }
    };

    validationTask.setOnSucceeded(Void -> popOver.updateContent(validationTask.getValue(), message.toString()));
    new Thread(validationTask).start();
  }

  private void updateForm() {
    Locale l = ConfigurationManager.getLocale();
    Set<TemplateFieldValue> metadataValues = getMetadataValues();
    if (metadataValues == null || metadataValues.isEmpty()) {
      noForm();
      return;
    }

    // 20170313 hsilva: this is required to ensure that we will not try to
    // present the form, using template, when there is a mismatch between
    // choosen metadata type/version & the file content
    Pair selectedInCombo = metadataCombo.getSelectionModel().getSelectedItem();
    if (selectedInCombo != null) {
      DescriptiveMetadata dom = (DescriptiveMetadata) selectedInCombo.getKey();
      try {
        if ((dom.getCreatorOption() != MetadataOption.TEMPLATE)
          && !Controller.validateSchema(metaText.getText(), dom.getSchema())) {
          noForm();
          return;
        }
      } catch (SAXException e) {
        noForm();
        return;
      }
    }

    int i = 0;
    for (TemplateFieldValue metadataValue : metadataValues) {
      // do not process this entry if it's marked as hidden
      if (getBooleanOption(metadataValue.get("hidden")))
        continue;
      String templateLabel = (String) metadataValue.get("label");

      String templateLabelI18N = (String) metadataValue.get("labeli18n");

      JSONObject jsonTemplateLabel = new JSONObject(templateLabel);
      Label label;
      String labelText;

      if (templateLabelI18N != null && I18n.t(templateLabelI18N) != null) {
        labelText = I18n.t(templateLabelI18N);
      } else if (jsonTemplateLabel.keySet().contains(l.toString())) {
        labelText = (String) jsonTemplateLabel.get(l.toString());
      } else if (jsonTemplateLabel.keySet().contains(l.getLanguage())) {
        labelText = (String) jsonTemplateLabel.get(l.getLanguage());
      } else {
        labelText = "N/A";
      }
      if (getBooleanOption(metadataValue.get("mandatory"))) {
        labelText += " *";
      }
      label = new Label(labelText);

      label.setWrapText(true);
      label.getStyleClass().add(Constants.CSS_FORMLABEL);
      if (metadataValue.get("description") != null) {
        label.setTooltip(new Tooltip((String) metadataValue.get("description")));
      }

      String controlType = (String) metadataValue.get("type");
      Control control;
      if (controlType == null) {
        control = createFormTextField(metadataValue);
      } else {
        switch (controlType) {
          case "text":
            control = createFormTextField(metadataValue);
            break;
          case "textarea":
          case "big-text":
          case "text-area":
            control = createFormTextArea(metadataValue);
            break;
          case "list":
            control = createFormCombo(metadataValue, l);
            break;
          case "date":
            control = createFormDatePicker(metadataValue);
            break;
          case "separator":
            control = null;
            break;
          default:
            control = createFormTextField(metadataValue);
            break;
        }
      }
      if (control != null) {
        metadataGrid.add(label, 0, i);
        metadataGrid.add(control, 1, i);
      } else {
        label.getStyleClass().add(Constants.CSS_FORMSEPARATOR);
        metadataGrid.add(label, 0, i, 2, 1);
      }
      i++;
    }
  }

  private boolean getBooleanOption(Object option) {
    boolean result = false;
    if (option != null) {
      if (option instanceof Boolean) {
        result = (Boolean) option;
      } else if (option instanceof String) {
        result = Boolean.parseBoolean((String) option);
      }
    }
    return result;
  }

  private TextField createFormTextField(TemplateFieldValue metadataValue) {
    TextField textField = new TextField((String) metadataValue.get("value"));
    HBox.setHgrow(textField, Priority.ALWAYS);
    textField.setUserData(metadataValue);
    textField.textProperty().addListener((observable2, oldValue2, newValue2) -> metadataValue.set("value", newValue2));
    if ("title".equals(metadataValue.getId())) {
      textField.setId("descObjTitle");
    }
    addListenersToUpdateUI(metadataValue, textField.textProperty());
    return textField;
  }

  private TextArea createFormTextArea(TemplateFieldValue metadataValue) {
    TextArea textArea = new TextArea((String) metadataValue.get("value"));
    textArea.setWrapText(true);
    HBox.setHgrow(textArea, Priority.ALWAYS);
    textArea.setUserData(metadataValue);
    textArea.textProperty().addListener((observable, oldValue, newValue) -> metadataValue.set("value", newValue));
    textArea.getStyleClass().add(Constants.CSS_FORM_TEXT_AREA);
    textArea.setWrapText(true);
    addListenersToUpdateUI(metadataValue, textArea.textProperty());
    return textArea;
  }

  private ComboBox<Pair> createFormCombo(TemplateFieldValue metadataValue, Locale l) {
    ObservableList<Pair> comboList = FXCollections.observableArrayList();
    String options = (String) metadataValue.get("options");
    JSONArray optionsArray = null;
    if (options != null) {
      optionsArray = new JSONArray(options);
    }

    String optionsLabels = (String) metadataValue.get("optionsLabels");
    String optionsLabelsI18NPrefix = null;
    if (metadataValue.get("optionsLabelI18nKeyPrefix") != null) {
      optionsLabelsI18NPrefix = (String) metadataValue.get("optionsLabelI18nKeyPrefix");
    }

    comboList.add(new Pair("", ""));
    Map<String, Pair> optionsMap = new HashMap<>();
    if (optionsLabels != null) {
      JSONObject jsonOptionsLabel = new JSONObject(optionsLabels);
      if (optionsArray != null) {
        for (int pos = 0; pos < optionsArray.length(); pos++) {
          String option = optionsArray.get(pos).toString();
          if (optionsLabelsI18NPrefix != null
            && I18n.t(optionsLabelsI18NPrefix + Constants.MISC_DOT + option) != null) {
            comboList.add(new Pair(option, I18n.t(optionsLabelsI18NPrefix + Constants.MISC_DOT + option)));
          } else if (jsonOptionsLabel.keySet().contains(option)) {
            // MAP
            JSONObject labelOption = jsonOptionsLabel.getJSONObject(option);
            Pair pair;
            if (labelOption.keySet().contains(l.toString())) {
              pair = new Pair(option, labelOption.getString(l.toString()));
            } else if (labelOption.keySet().contains(l.getLanguage())) {
              pair = new Pair(option, labelOption.getString(l.getLanguage()));
            } else if (labelOption.keySet().contains("i18n")) {
              pair = new Pair(option, "I18N");
            } else {
              pair = new Pair(option, "NOT FOUND");
            }
            optionsMap.put(option, pair);
            comboList.add(pair);
          } else {
            comboList.add(new Pair(option, option));
          }
        }
      }
    } else {
      if (optionsArray != null) {
        for (int pos = 0; pos < optionsArray.length(); pos++) {
          String option = optionsArray.get(pos).toString();
          Pair pair = new Pair(option, option);
          optionsMap.put(option, pair);
          comboList.add(pair);
        }
      }
    }
    ComboBox<Pair> comboBox = new ComboBox<>(comboList);
    HBox.setHgrow(comboBox, Priority.ALWAYS);
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.setUserData(metadataValue);
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> metadataValue.set("value", newValue));

    if (metadataValue.get("value") != null) {
      if (metadataValue.get("value") instanceof String) {
        String currentValue = (String) metadataValue.get("value");
        if (currentValue != null && optionsMap.containsKey(currentValue)) {
          comboBox.getSelectionModel().select(optionsMap.get(currentValue));
        } else if (!"".equals(currentValue.trim())) {
          Pair other = new Pair(currentValue, currentValue);
          comboBox.getItems().add(other);
          comboBox.getSelectionModel().select(other);
        }
      } else if (metadataValue.get("value") instanceof Pair) {
        Pair currentValue = (Pair) metadataValue.get("value");
        if (currentValue != null && optionsMap.containsKey(currentValue.getKey())) {
          comboBox.getSelectionModel().select(optionsMap.get(currentValue.getKey()));
        }
      }
    }

    addListenersToUpdateUI(metadataValue, comboBox.valueProperty());
    return comboBox;
  }

  private boolean isDomSynchronized() {
    boolean domIsSynchronized = true;
    Pair selectedPair = metadataCombo.getSelectionModel().getSelectedItem();
    if (selectedPair != null) {
      DescriptiveMetadata dom = (DescriptiveMetadata) selectedPair.getKey();
      if (dom != null) {
        String synchronize = ConfigurationManager
          .getMetadataConfig(dom.getTemplateType() + Constants.CONF_K_SUFFIX_SYNCHRONIZED);

        // 2017-04-28 bferreira: assume TRUE by default, unless 'false' is
        // specificed (Boolean.valueOf can not be used as it has the opposite
        // behaviour)
        domIsSynchronized = !("false".equalsIgnoreCase(synchronize));
      }
    }
    return domIsSynchronized;
  }

  private void addListenersToUpdateUI(TemplateFieldValue metadataValue, Property property) {
    if (isDomSynchronized()) {
      if ("title".equals(metadataValue.getId())) {
        paneTitle.textProperty().bind(property);
        if (currentSIPNode != null) {
          property.bindBidirectional(currentSIPNode.valueProperty());
        } else {
          if (currentSchema != null) {
            property.bindBidirectional(currentSchema.valueProperty());
          }
        }
      }

      if ("level".equals(metadataValue.getId())) {
        property.addListener((observable, oldValue, newValue) -> {
          TreeItem<String> itemToForceUpdate = null;
          // Update the icons of the description level
          if (currentSIPNode != null) {
            if (newValue instanceof String) {
              currentSIPNode.updateDescriptionLevel((String) newValue);
              itemToForceUpdate = currentSIPNode;
            } else if (newValue instanceof Pair) {
              currentSIPNode.updateDescriptionLevel((String) ((Pair) newValue).getKey());
            }
          } else if (currentSchema != null) {
            if (newValue instanceof String) {
              currentSchema.updateDescriptionLevel((String) newValue);
            } else if (newValue instanceof Pair) {
              currentSchema.updateDescriptionLevel((String) ((Pair) newValue).getKey());
            }
            itemToForceUpdate = currentSchema;
          }

          // Force update
          if (itemToForceUpdate != null) {
            String value = itemToForceUpdate.getValue();
            itemToForceUpdate.setValue("");
            itemToForceUpdate.setValue(value);
          }
        });
      }
    }
  }

  private DatePicker createFormDatePicker(TemplateFieldValue metadataValue) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_4);
    LocalDateStringConverter ldsc = new LocalDateStringConverter(formatter, null);

    String currentValue = metadataValue.get("value") != null ? (String) metadataValue.get("value") : "";

    LocalDate date = null;
    try {
      date = ldsc.fromString(currentValue);
    } catch (DateTimeParseException e) {
      // maybe because of {{mixed}}
    }
    DatePicker datePicker = new DatePicker(date);
    datePicker.setMaxWidth(Double.MAX_VALUE);
    datePicker.setConverter(new StringConverter<LocalDate>() {
      private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_4);

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
      .addListener((observable, oldValue, newValue) -> metadataValue.set("value", ldsc.toString(newValue)));
    addListenersToUpdateUI(metadataValue, datePicker.valueProperty());
    return datePicker;
  }

  private void noForm() {
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metaText);
    topButtons.remove(toggleForm);
    updateMetadataTop();
  }

  private Set<TemplateFieldValue> getMetadataValues() {
    if (currentDescOb != null) {
      Pair selectedPair = metadataCombo.getSelectionModel().getSelectedItem();
      if (selectedPair != null) {
        DescriptiveMetadata dom = (DescriptiveMetadata) selectedPair.getKey();
        if (dom != null) {
          return currentDescOb.getMetadataValueMap(dom);
        }
      }
    }
    // error, there is no SIP or SchemaNode selected
    return new HashSet<>();
  }

  private void saveMetadataPrivate(DescriptiveMetadata selectedDescObjMetadata) {
    String oldMetadata = null, newMetadata = null;
    if (currentDescOb != null) {
      newMetadata = metaText.getText();
      oldMetadata = selectedDescObjMetadata.getContentDecoded();
    }
    // only update if there's been modifications or there's no old
    // metadata and the new isn't empty
    boolean update = false;
    if (selectedDescObjMetadata.getCreatorOption() == MetadataOption.TEMPLATE) {
      if (currentDescOb != null) {
        currentDescOb.updatedMetadata(selectedDescObjMetadata);
      }
    } else {
      if (newMetadata != null) {
        if (oldMetadata == null || !oldMetadata.equals(newMetadata)) {
          update = true;
        }
      }
    }

    if (update) {
      if (currentDescOb != null) {
        selectedDescObjMetadata.setContentDecoded(newMetadata);
      } else {
        DescriptiveMetadata newObjMetadata = new DescriptiveMetadata();
        newObjMetadata.setContentEncoding(Constants.ENCODING_BASE64);
        newObjMetadata.setContentDecoded(newMetadata);
        currentDescOb.getMetadata().add(newObjMetadata);
      }
    }
  }

  public void saveMetadata() {
    textBoxCancelledChange = true;
    saveMetadataPrivate();
  }

  /**
   * Saves the metadata from the text area in the SIP.
   */
  private void saveMetadataPrivate() {
    if (metadataCombo == null)
      return;
    Pair selectedObject = metadataCombo.getSelectionModel().getSelectedItem();
    DescriptiveMetadata selectedDescObjMetadata;
    if (selectedObject == null || !(selectedObject.getKey() instanceof DescriptiveMetadata)) {
      return;
    }

    selectedDescObjMetadata = (DescriptiveMetadata) selectedObject.getKey();
    if (metadata.getChildren().contains(metadataFormWrapper)) {
      if (currentDescOb != null) {
        currentDescOb.updatedMetadata(selectedDescObjMetadata);
        String updatedContent = currentDescOb.getMetadataWithReplaces(selectedDescObjMetadata);
        updateTextArea(updatedContent);
        selectedDescObjMetadata.setContentDecoded(updatedContent);
        saveMetadataPrivate(selectedDescObjMetadata);
      }
    } else {
      saveMetadataPrivate(selectedDescObjMetadata);
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
    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_HELP_TITLE));
    title.getStyleClass().add(Constants.CSS_HELPTITLE);
    title.setTextAlignment(TextAlignment.CENTER);
    title.setWrapText(true);
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
    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_DOCS_HELP_TITLE));
    title.getStyleClass().add(Constants.CSS_HELPTITLE);
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    box.getChildren().addAll(titleBox);
    documentationHelp.getChildren().add(box);

    documentationHelp.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (event.getGestureSource() instanceof SourceTreeCell || db.hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
        title.setText(I18n.t(Constants.I18N_INSPECTIONPANE_ON_DROP_DOCS));
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
      } else {
        addDocumentationToSIP(null);
      }

      dataBox.getChildren().clear();
      sipDocumentation.setRoot(docsRoot);
      dataBox.getChildren().add(sipDocumentation);
      content.setCenter(dataBox);
      content.setBottom(docsBottom);
      event.consume();
    });

    documentationHelp.setOnDragExited(event -> {
      title.setText(I18n.t(Constants.I18N_INSPECTIONPANE_DOCS_HELP_TITLE));
      event.consume();
    });
  }

  private void createContent() {
    content = new BorderPane();
    content.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    VBox.setVgrow(content, Priority.ALWAYS);
    content.setMinHeight(200);

    HBox top = new HBox(10);
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setPadding(new Insets(5, 15, 5, 15));
    top.setAlignment(Pos.CENTER);

    Label title = new Label(I18n.t(Constants.I18N_DATA).toUpperCase());
    title.getStyleClass().add(Constants.CSS_TITLE);

    PopOver editRepresentationTypePopOver = new PopOver();
    editRepresentationTypePopOver.setDetachable(false);
    editRepresentationTypePopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label sipTypeLabel = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_REPRESENTATION_TYPE_TOOLTIP));
    sipTypeLabel.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    popOverContent.getChildren().addAll(sipTypeLabel, createRepresentationTypeComboBox());
    editRepresentationTypePopOver.setContentNode(popOverContent);

    editRepresentationTypeButton = new Button();
    editRepresentationTypeButton.getStyleClass().add(Constants.CSS_DARK_BUTTON);
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PENCIL, Color.WHITE, 16));
      editRepresentationTypeButton.setGraphic(iv);
    });
    editRepresentationTypeButton.setGraphicTextGap(5);
    editRepresentationTypeButton.setContentDisplay(ContentDisplay.RIGHT);
    editRepresentationTypeButton.setOnAction(event -> editRepresentationTypePopOver.show(editRepresentationTypeButton));

    top.getChildren().addAll(title);
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
    docsRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);
    sipDocumentation.setRoot(docsRoot);
    toggleDocumentation = new ToggleButton();
    toggleDocumentation.getStyleClass().addAll(Constants.CSS_DARK_BUTTON, Constants.CSS_BOLDTEXT);
    toggleDocumentation.setText(I18n.t(Constants.I18N_DOCUMENTATION));
    toggleDocumentation.setTooltip(new Tooltip(I18n.t(Constants.I18N_DOCUMENTATION)));
    Platform.runLater(() -> {
      Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.OPEN_FOLDER, Color.WHITE);
      Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.BOOK, Color.WHITE);
      ImageView toggleImage = new ImageView();
      toggleDocumentation.setGraphic(toggleImage);
      toggleImage.imageProperty()
        .bind(Bindings.when(toggleDocumentation.selectedProperty()).then(selected).otherwise(unselected));
    });
    toggleDocumentation.setGraphicTextGap(5);
    toggleDocumentation.setContentDisplay(ContentDisplay.RIGHT);
    title.textProperty().bind(Bindings.when(toggleDocumentation.selectedProperty())
      .then(I18n.t(Constants.I18N_DOCUMENTATION).toUpperCase()).otherwise(I18n.t(Constants.I18N_DATA).toUpperCase()));

    toggleDocumentation.selectedProperty().addListener((observable, oldValue, newValue) -> {
      dataBox.getChildren().clear();
      // newValue == true means that the documentation will be displayed
      if (newValue) {
        toggleDocumentation.setTooltip(new Tooltip(I18n.t(Constants.I18N_DATA)));
        toggleDocumentation.setText(I18n.t(Constants.I18N_DATA));
        if (docsRoot.getChildren().isEmpty()) {
          dataBox.getChildren().add(documentationHelp);
          content.setBottom(new HBox());
        } else {
          dataBox.getChildren().add(sipDocumentation);
          content.setBottom(docsBottom);
        }
      } else { // from the documentation to the representations
        toggleDocumentation.setTooltip(new Tooltip(I18n.t(Constants.I18N_DOCUMENTATION)));
        toggleDocumentation.setText(I18n.t(Constants.I18N_DOCUMENTATION));
        dataBox.getChildren().clear();
        dataBox.getChildren().add(sipFiles);
        content.setCenter(dataBox);
        content.setBottom(contentBottom);
      }
    });
    createDocsBottom();

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(top, new Tooltip(I18n.help("tooltip.inspectionPanel.data")));
    }
    top.getChildren().addAll(space, editRepresentationTypeButton, toggleDocumentation);
  }

  private void createLoadingPanes() {
    loadingPane = new HBox();
    metadataLoadingPane = new HBox();
    loadingPane.setAlignment(Pos.CENTER);
    metadataLoadingPane.setAlignment(Pos.CENTER);
    VBox.setVgrow(metadataLoadingPane, Priority.ALWAYS);
    try {
      if (loadingGif == null) {
        loadingGif = new Image(ClassLoader.getSystemResource(Constants.RSC_LOADING_GIF).openStream());
      }
      loadingPane.getChildren().add(new ImageView(loadingGif));
      metadataLoadingPane.getChildren().add(new ImageView(loadingGif));
    } catch (IOException e) {
      LOGGER.error("Error reading loading GIF", e);
    }
  }

  private void createContentBottom() {
    contentBottom = new HBox(10);
    contentBottom.setPadding(new Insets(10, 10, 10, 10));
    contentBottom.setAlignment(Pos.CENTER);

    ignore = new Button(I18n.t(Constants.I18N_REMOVE));
    ignore.setOnAction(event -> {
      InspectionTreeItem selectedRaw = (InspectionTreeItem) sipFiles.getSelectionModel().getSelectedItem();
      if (selectedRaw == null)
        return;
      if (selectedRaw instanceof SipContentRepresentation) {
        SipContentRepresentation selected = (SipContentRepresentation) selectedRaw;
        sipRoot.getChildren().remove(selectedRaw);
        currentSIPNode.getSip().removeRepresentation(selected.getRepresentation());
      } else {
        Set<Path> paths = new HashSet<>();
        paths.add(selectedRaw.getPath());
        if (currentDescOb != null && currentDescOb instanceof SipPreview) {
          ((SipPreview) currentDescOb).ignoreContent(paths);
          TreeItem parent = selectedRaw.getParentDir();
          TreeItem child = (TreeItem) selectedRaw;
          parent.getChildren().remove(child);
        }
      }
    });
    ignore.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    Button addRepresentation = new Button(I18n.t(Constants.I18N_INSPECTIONPANE_ADD_REPRESENTATION));
    addRepresentation.setOnAction(event -> {
      int repCount = currentSIPNode.getSip().getRepresentations().size();
      String representationName;
      boolean representationExists = false;

      do {
        repCount++;
        representationName = Constants.SIP_REP_PREFIX + repCount;

        if (currentDescOb != null && currentDescOb instanceof SipPreview) {
          for (SipRepresentation sipRepresentation : ((SipPreview) currentDescOb).getRepresentations()) {
            if (representationName.equals(sipRepresentation.getName())) {
              representationExists = true;
            }
          }
        }
      } while (representationExists);

      SipRepresentation sipRep = new SipRepresentation(representationName);
      currentSIPNode.getSip().addRepresentation(sipRep);
      SipContentRepresentation sipContentRep = new SipContentRepresentation(sipRep);
      sipRoot.getChildren().add(sipContentRep);
    });
    addRepresentation.minWidthProperty().bind(this.widthProperty().multiply(0.25));

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    contentBottom.getChildren().addAll(addRepresentation, space, ignore);
  }

  private void createDocsBottom() {
    docsBottom = new HBox(10);
    docsBottom.setPadding(new Insets(10, 10, 10, 10));
    docsBottom.setAlignment(Pos.CENTER_LEFT);

    Button remove = new Button(I18n.t(Constants.I18N_REMOVE));
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

  private void createMultipleSelectedBottom() {
    BorderPane help = new BorderPane();
    help.getStyleClass().add(Constants.CSS_INSPECTIONPART);

    HBox top = new HBox();
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setPadding(new Insets(5, 15, 10, 15));

    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_MULTIPLE_SELECTED_HELP_TITLE).toUpperCase());
    title.setPadding(new Insets(5, 0, 0, 0));
    title.getStyleClass().add(Constants.CSS_TITLE);
    top.getChildren().add(title);
    help.setTop(top);

    Label multSelectedHelp = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_MULTIPLE_SELECTED_HELP));
    multSelectedHelp.setPadding(new Insets(10, 10, 10, 10));
    multSelectedHelp.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    multSelectedHelp.setWrapText(true);

    help.setCenter(multSelectedHelp);

    BorderPane confirm = new BorderPane();
    confirm.getStyleClass().add(Constants.CSS_INSPECTIONPART);

    HBox confirmTop = new HBox();
    confirmTop.getStyleClass().add(Constants.CSS_HBOX);
    confirmTop.setPadding(new Insets(5, 15, 10, 15));

    Label confirmTitle = new Label(I18n.t(Constants.I18N_APPLY).toUpperCase());
    confirmTitle.setAlignment(Pos.CENTER_LEFT);
    confirmTitle.setPadding(new Insets(5, 0, 0, 0));
    confirmTitle.getStyleClass().add(Constants.CSS_TITLE);
    confirmTop.getChildren().add(confirmTitle);
    confirm.setTop(confirmTop);

    PopOver applyPopOver = new PopOver();
    applyPopOver.setDetachable(false);
    applyPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);

    HBox popOverContent = new HBox(10);
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label popOverTitle = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_MULTIPLE_SELECTED_APPLIED_MESSAGE));
    popOverTitle.setStyle(Constants.CSS_FX_FONT_SIZE_16PX);
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.GREEN, 32));
      popOverContent.getChildren().addAll(popOverTitle, iv);
    });
    applyPopOver.setContentNode(popOverContent);

    HBox multSelectedSaveBox = new HBox(5);
    multSelectedSaveBox.setPadding(new Insets(10, 10, 10, 10));
    multSelectedSaveBox.getStyleClass();
    multSelectedSaveBox.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    Label confirmationLabel = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_MULTIPLE_SELECTED_CONFIRM));
    confirmationLabel.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    Button save = new Button(I18n.t(Constants.I18N_APPLY));
    save.setOnAction(event -> {
      applyMetadatasToMultipleItems();
      applyPopOver.show(save);
    });

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    multSelectedSaveBox.getChildren().addAll(confirmationLabel, space, save);
    confirm.setCenter(multSelectedSaveBox);

    multSelectedBottom = new VBox(10);
    multSelectedBottom.getChildren().addAll(help, confirm);
  }

  private void applyMetadatasToMultipleItems() {
    List<DescriptiveMetadata> metadataList = currentDescOb.getMetadata();
    if (!metadataList.isEmpty()) {
      selectedItems.forEach(item -> {
        Sip itemDO = null;
        if (item instanceof SchemaNode) {
          itemDO = ((SchemaNode) item).getDob();
        } else if (item instanceof SipPreviewNode) {
          itemDO = ((SipPreviewNode) item).getSip();
        }

        if (itemDO != null) {
          applyIPTypeToDescriptionObject(itemDO,
            (IPContentType) contentType.getSelectionModel().getSelectedItem().getKey());
          for (DescriptiveMetadata metadataObj : metadataList) {
            applyMetadataFileToDescriptionObject(metadataObj, itemDO, item);
          }
        }
      });
    }
  }

  private void applyIPTypeToDescriptionObject(Sip itemDO, IPContentType setContentType) {
    IPContentType mixedMergeIPType = new IPContentType("{{mixed}}");
    if (!setContentType.getValue().equalsIgnoreCase(mixedMergeIPType.getValue())) {
      itemDO.setContentType(setContentType);
    }
  }

  private void applyMetadataFileToDescriptionObject(DescriptiveMetadata metadataObj, Sip descObj,
    TreeItem<String> treeItem) {
    if (metadataObj.getCreatorOption() != MetadataOption.TEMPLATE) {
      // remove the metadata files with the same ID as the new one
      List<DescriptiveMetadata> toRemove = new ArrayList<>();
      descObj.getMetadata().forEach(descObjMetadata -> {
        if (descObjMetadata.getId().equals(metadataObj.getId()))
          toRemove.add(descObjMetadata);
      });
      descObj.getMetadata().removeAll(toRemove);
      // add a clone of the new metadata
      descObj.getMetadata().add(metadataObj.clone());
    } else {
      boolean merged = false;
      for (DescriptiveMetadata descObjMetadata : descObj.getMetadata()) {
        if (descObjMetadata.getId().equals(metadataObj.getId())) {
          merged = true;
          Set<TemplateFieldValue> metadataObjValues = metadataObj.getValues();
          Set<TemplateFieldValue> descObjMetadataValues = descObjMetadata.getValues();
          for (TemplateFieldValue metadataObjValue : metadataObjValues) {
            for (TemplateFieldValue descObjMetadataValue : descObjMetadataValues) {
              if (metadataObjValue.getId().equals(descObjMetadataValue.getId())) {
                // ignore the new value when it's {{auto-generate}} or {{mixed}}
                if (metadataObjValue.get("value") == null || (!metadataObjValue.get("value").equals("{{auto-generate}}")
                  && !metadataObjValue.get("value").equals("{{mixed}}"))) {
                  descObjMetadataValue.set("value", metadataObjValue.get("value"));
                  descObjMetadataValue.set("auto-generate", null);

                  // we need to set the value of the tree item here, otherwise
                  // the "title" option will be overriden by the UI
                  if (metadataObjValue.getId().equals("title"))
                    treeItem.setValue((String) metadataObjValue.get("value"));
                }
              }
            }
          }
        }
      }
      if (!merged) {
        descObj.getMetadata().add(metadataObj.clone());
      }
    }
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
        dataBox.getChildren().clear();
        dataBox.getChildren().add(sipFiles);
        content.setCenter(dataBox);
        content.setBottom(contentBottom);
      }
    });
    new Thread(contentTask).start();
  }

  private void createDocumentation(SipPreviewNode sip, boolean active) {
    docsRoot.getChildren().clear();
    if (active) {
      content.setCenter(loadingPane);
      content.setBottom(new HBox());
    }

    docsTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        for (TreeNode treeNode : sip.getSip().getDocumentation()) {
          TreeItem<Object> startingItem = recCreateSipContent(treeNode, docsRoot);
          startingItem.setExpanded(true);
          docsRoot.getChildren().add(startingItem);
        }
        docsRoot.sortChildren();
        return null;
      }
    };
    docsTask.setOnSucceeded(event -> {
      if (active) {
        if (!docsRoot.getChildren().isEmpty()) {
          if (!dataBox.getChildren().contains(sipDocumentation)) {
            dataBox.getChildren().clear();
            dataBox.getChildren().add(sipDocumentation);
          }
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
    rules.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    VBox.setVgrow(rules, Priority.ALWAYS);
    rules.setMinHeight(200);

    HBox top = new HBox(10);
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setPadding(new Insets(10, 15, 10, 15));

    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_RULES).toUpperCase());
    title.getStyleClass().add(Constants.CSS_TITLE);
    top.getChildren().add(title);

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(top, new Tooltip(I18n.help("tooltip.inspectionPanel.associations")));
    }

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
    Label emptyText = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_HELP_RULE_LIST));
    emptyText.getStyleClass().add(Constants.CSS_HELPTITLE);
    emptyText.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(emptyText);

    emptyRulesPane.setOnDragOver(event -> {
      if (currentSchema != null && event.getGestureSource() instanceof SourceTreeCell) {
        event.acceptTransferModes(TransferMode.COPY);
        emptyText.setText(I18n.t(Constants.I18N_INSPECTIONPANE_ON_DROP));
      }
      event.consume();
    });

    emptyRulesPane.setOnDragDropped(event -> {
      RodaInApplication.getSchemePane().startAssociation(currentSchema);
      event.consume();
    });

    emptyRulesPane.setOnDragExited(event -> {
      emptyText.setText(I18n.t(Constants.I18N_INSPECTIONPANE_HELP_RULE_LIST));
      event.consume();
    });

    box.getChildren().addAll(titleBox);
    emptyRulesPane.getChildren().add(box);
    rules.setCenter(emptyRulesPane);
  }

  private void createExportBox() {
    exportBox = new HBox(10);
    exportBox.setPadding(new Insets(10, 10, 10, 10));
    exportBox.setAlignment(Pos.CENTER);
    exportBox.getStyleClass().add(Constants.CSS_TITLE_BOX);

    Label title = new Label(I18n.t(Constants.I18N_EXPORT_BOX_TITLE).toUpperCase());
    title.getStyleClass().add(Constants.CSS_TITLE);
    title.setMinWidth(110);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    export = new Button(I18n.t(Constants.I18N_EXPORT));
    export.setMinWidth(100);
    export.setOnAction(event -> RodaInApplication.exportSIPs());
    export.getStyleClass().add(Constants.CSS_EXPORT_BUTTON);

    exportBox.getChildren().addAll(title, space, export);

    if (Boolean.parseBoolean(ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED))) {
      Tooltip.install(export, new Tooltip(I18n.help("tooltip.export")));
    }
  }

  private void updateSelectedMetadata(DescriptiveMetadata dom) {
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
    metadataTask.setOnSucceeded(Void -> {
      if (metadataTask != null && metadataTask == thisMetadataTask) {
        Set<TemplateFieldValue> values = currentDescOb.getMetadataValueMap(dom);
        boolean show = values != null && !values.isEmpty();
        showMetadataPane(show);
      }

      InputStream schema = dom.getSchema();
      if (schema == null) {
        topButtons.remove(validationButton);
      } else {
        topButtons.add(validationButton);
      }
      updateMetadataTop();
    });
    metadataTask.setOnFailed(event -> {
      // recreate the metadata text box and retry
      createMetadataTextBox();
      metadata.getChildren().clear();
      metadata.getChildren().addAll(metadataTopBox, metaText);
      int selectedIndex = metadataCombo.getSelectionModel().getSelectedIndex();
      updateMetadataCombo(false);
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
    hideEditRepresentationTypeButton();
    setTop(topBox);
    setCenter(center);
    setBottom(exportBox);
    currentSIPNode = sip;
    currentDescOb = sip.getSip();
    currentSchema = null;
    selectedItems = null;
    if (contentTask != null && contentTask.isRunning()) {
      contentTask.cancel(true);
    }
    if (metadataTask != null && metadataTask.isRunning()) {
      metadataTask.cancel(true);
    }

    /* Top */
    ImageView iconView = new ImageView(sip.getIconWhite());
    createTopSubtitle(iconView, sip.getValue());
    sip.graphicProperty()
      .addListener((observable, oldValue, newValue) -> iconView.setImage(((ImageView) newValue).getImage()));

    HBox top = new HBox(5);
    top.setAlignment(Pos.CENTER_RIGHT);
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);
    // we need to account for the size of the combo-box, otherwise the top
    // box
    // is too tall
    topBox.setPadding(new Insets(11, 15, 11, 15));

    PopOver editPopOver = new PopOver();
    editPopOver.setDetachable(false);
    editPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);

    editButton = new Button();
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PENCIL, Color.WHITE, 16));
      editButton.setGraphic(iv);
    });
    editButton.getStyleClass().add(Constants.CSS_TOP_SUBTITLE);
    editButton.setGraphicTextGap(5);
    editButton.setContentDisplay(ContentDisplay.RIGHT);
    editButton.setWrapText(true);
    editButton.setOnAction(event -> editPopOver.show(editButton));

    representationTypeBox.getChildren().clear();
    representationTypeBox.getChildren().addAll(space, editButton);

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label sipTypeLabel = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_SIP_TYPE_TOOLTIP));
    sipTypeLabel.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    popOverContent.getChildren().addAll(sipTypeLabel, createTypeComboBox(sip.getSip()));
    editPopOver.setContentNode(popOverContent);

    topBox.getChildren().remove(representationTypeBox);
    topBox.getChildren().add(representationTypeBox);

    topSubtitle.getChildren().addAll(space, top);

    updateMetadataCombo(false);

    /* Center */
    center.getChildren().clear();

    // content tree
    boolean documentation = toggleDocumentation.isSelected();
    createContent(sip, !documentation);
    createDocumentation(sip, documentation);

    center.getChildren().addAll(metadata, content);
    setCenter(center);
    setBottom(exportBox);
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
    selectedItems = null;
    currentSchema = node;
    if (contentTask != null && contentTask.isRunning()) {
      contentTask.cancel(true);
    }
    if (metadataTask != null && metadataTask.isRunning()) {
      metadataTask.cancel(true);
    }

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);
    PopOver editPopOver = new PopOver();
    editPopOver.setDetachable(false);
    editPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);

    editButton = new Button();
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PENCIL, Color.WHITE, 16));
      editButton.setGraphic(iv);
    });
    editButton.getStyleClass().add(Constants.CSS_TOP_SUBTITLE);
    editButton.setGraphicTextGap(5);
    editButton.setContentDisplay(ContentDisplay.RIGHT);
    editButton.setWrapText(true);
    editButton.setOnAction(event -> editPopOver.show(editButton));

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label sipTypeLabel = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_SIP_TYPE_TOOLTIP));
    sipTypeLabel.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    popOverContent.getChildren().addAll(sipTypeLabel, createTypeComboBox(node.getDob()));
    editPopOver.setContentNode(popOverContent);

    representationTypeBox.getChildren().clear();
    representationTypeBox.getChildren().addAll(space, editButton);

    topBox.getChildren().remove(representationTypeBox);
    topBox.getChildren().add(representationTypeBox);

    topBox.setPadding(new Insets(15, 15, 15, 15));
    ImageView iconView = new ImageView(node.getIconWhite());
    node.graphicProperty()
      .addListener((observable, oldValue, newValue) -> iconView.setImage(((ImageView) newValue).getImage()));
    createTopSubtitle(iconView, node.getValue());

    /* center */
    center.getChildren().clear();
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox, metadataLoadingPane);
    updateMetadataCombo(false);

    // rules
    updateRuleList();

    center.getChildren().addAll(metadata, rules);
    setCenter(center);
    setBottom(exportBox);
  }

  public void update(List<TreeItem<String>> selectedItems) {
    setTop(topBox);

    currentSchema = null;
    currentSIPNode = null;
    this.selectedItems = selectedItems;
    // create a temporary description object to hold the metadata

    Map<String, TemplateFieldValue> commonMetadataValues = new HashMap<>();
    String commonTemplate = null, commonVersion = null, commonMetadataType = null;
    IPContentType commonIPType = null;
    boolean common = true;
    for (TreeItem ti : selectedItems) {

      if (!common)
        continue;

      Sip dob;
      if (ti instanceof SipPreviewNode)
        dob = ((SipPreviewNode) ti).getSip();
      else if (ti instanceof SchemaNode)
        dob = ((SchemaNode) ti).getDob();
      else
        continue;

      if (commonIPType == null) {
        commonIPType = dob.getContentType();
      } else {
        if (!commonIPType.getValue().equalsIgnoreCase(dob.getContentType().getValue())) {
          commonIPType = new IPContentType("{{mixed}}");
        }
      }
      for (DescriptiveMetadata dobm : dob.getMetadata()) {
        // Check if the creator option, template and version are the
        // same as the previously analysed items
        if (dobm.getCreatorOption() != MetadataOption.TEMPLATE) {
          common = false;
          continue;
        }
        if (commonTemplate == null)
          commonTemplate = dobm.getTemplateType();
        if (commonVersion == null)
          commonVersion = dobm.getMetadataVersion();
        if (commonMetadataType == null) {
          commonMetadataType = dobm.getMetadataType();
        }

        common = commonTemplate != null && commonTemplate.equalsIgnoreCase(dobm.getTemplateType())
          && commonVersion != null && commonVersion.equalsIgnoreCase(dobm.getMetadataVersion())
          && commonMetadataType != null && commonMetadataType.equalsIgnoreCase(dobm.getMetadataType());
        // Add the metadata values to the common set
        for (TemplateFieldValue mv : dob.getMetadataValueMap(dobm)) {
          if (commonMetadataValues.containsKey(mv.getId())) {
            if (mv.get("value") == null && commonMetadataValues.get(mv.getId()).get("value") == null)
              continue;
            if (mv.get("value") instanceof String) {
              String mvValue = (String) mv.get("value");
              if (commonMetadataValues.get(mv.getId()).get("value") instanceof String) {
                String commonMVvalue = (String) commonMetadataValues.get(mv.getId()).get("value");
                if (mvValue == null || !mvValue.equals(commonMVvalue)) {
                  commonMetadataValues.get(mv.getId()).set("value", "{{mixed}}");
                }
              } else if (commonMetadataValues.get(mv.getId()).get("value") instanceof Pair) {
                Pair commonMVvalue = (Pair) commonMetadataValues.get(mv.getId()).get("value");
                if (mvValue == null || !mvValue.equals(commonMVvalue.getKey())) {
                  commonMetadataValues.get(mv.getId()).set("value", "{{mixed}}");
                }
              }
            } else if (mv.get("value") instanceof Pair) {
              Pair mvValue = (Pair) mv.get("value");
              if (commonMetadataValues.get(mv.getId()).get("value") instanceof String) {
                String commonMVvalue = (String) commonMetadataValues.get(mv.getId()).get("value");
                if (mvValue == null || !mvValue.getKey().equals(commonMVvalue)) {
                  commonMetadataValues.get(mv.getId()).set("value", "{{mixed}}");
                }
              } else if (commonMetadataValues.get(mv.getId()).get("value") instanceof Pair) {
                Pair commonMVvalue = (Pair) commonMetadataValues.get(mv.getId()).get("value");
                if (mvValue == null || !mvValue.getKey().equals(commonMVvalue.getKey())) {
                  commonMetadataValues.get(mv.getId()).set("value", "{{mixed}}");
                }
              }
            }
          } else {
            commonMetadataValues.put(mv.getId(), new TemplateFieldValue(mv));
          }
        }
      }
    }
    currentDescOb = new Sip(
      new DescriptiveMetadata(MetadataOption.TEMPLATE, commonTemplate, commonMetadataType, commonVersion));

    PopOver editPopOver = new PopOver();
    editPopOver.setDetachable(false);
    editPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);

    editButton = new Button();
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PENCIL, Color.WHITE, 16));
      editButton.setGraphic(iv);
    });
    editButton.getStyleClass().add(Constants.CSS_TOP_SUBTITLE);
    editButton.setGraphicTextGap(5);
    editButton.setContentDisplay(ContentDisplay.RIGHT);
    editButton.setWrapText(true);
    editButton.setOnAction(event -> editPopOver.show(editButton));

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add(Constants.CSS_INSPECTIONPART);
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label sipTypeLabel = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_SIP_TYPE_TOOLTIP));
    sipTypeLabel.setStyle(Constants.CSS_FX_TEXT_FILL_BLACK);
    popOverContent.getChildren().addAll(sipTypeLabel, createTypeComboBox(currentDescOb));
    editPopOver.setContentNode(popOverContent);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    representationTypeBox.getChildren().clear();
    representationTypeBox.getChildren().addAll(space, editButton);

    if (commonIPType != null) {
      Pair commonIPTypePair = new Pair(commonIPType, commonIPType.getValue());
      contentType.getSelectionModel().select(commonIPTypePair);
      editButton.setText(((IPContentType) commonIPTypePair.getKey()).getValue());
      currentDescOb.setContentType(commonIPType);
    }

    currentDescOb.getMetadata().clear();
    if (common) {
      DescriptiveMetadata dobm = new DescriptiveMetadata(MetadataOption.TEMPLATE, commonTemplate, commonMetadataType,
        commonVersion);
      dobm.setValues(new TreeSet<>(commonMetadataValues.values()));
      currentDescOb.getMetadata().add(dobm);
      currentDescOb.updatedMetadata(dobm);
    } else {
      currentDescOb.setTitle("{{auto-generate}}");
      currentDescOb.setId("{{auto-generate}}");
      currentDescOb.setDescriptionlevel("{{auto-generate}}");
    }

    if (contentTask != null && contentTask.isRunning()) {
      contentTask.cancel(true);
    }
    if (metadataTask != null && metadataTask.isRunning()) {
      metadataTask.cancel(true);
    }

    /* Top */
    topBox.setPadding(new Insets(15, 15, 15, 15));
    createTopSubtitle(null, String.format("%d %s", selectedItems.size(), I18n.t(Constants.I18N_ITEMS)));

    /* center */
    center.getChildren().clear();
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox);

    updateMetadataCombo(false);

    center.getChildren().addAll(metadata, multSelectedBottom);
    setCenter(center);
    setBottom(exportBox);
  }

  private HBox createTypeComboBox(Sip descOb) {
    HBox result = new HBox(5);
    result.setAlignment(Pos.CENTER_LEFT);

    // Text field for the OTHER content type
    TextField otherTextField = new TextField();
    otherTextField.textProperty().addListener((obs, oldValue, newValue) -> {
      if (Constants.SIP_CONTENT_TYPE_OTHER.equalsIgnoreCase(descOb.getContentType().getValue())) {
        descOb.getContentType().setOtherValue(newValue);
      } else {
        descOb.getContentType().setValue(newValue);
      }
      editButton.setText(newValue);

    });
    // Content Type combo box
    contentType = new ComboBox<>();
    List<Pair<IPContentType, String>> contTypeList = new ArrayList<>();

    result.getChildren().addAll(contentType);

    Pair selected = null;
    for (Pair<SipType, List<IPContentType>> ct : SipType.getFilteredIpContentTypes()) {
      for (IPContentType ipContentType : ct.getValue()) {
        IPContentType ipCT = new IPContentType(ipContentType);
        Pair toAdd = new Pair(ipCT, ipCT.asString());
        contTypeList.add(toAdd);
        if (ipCT.getValue().equalsIgnoreCase(descOb.getContentType().getValue())) {
          selected = toAdd;
        }
      }
    }
    // sort the list as strings
    Collections.sort(contTypeList, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
    contentType.setItems(FXCollections.observableList(contTypeList));
    contentType.getSelectionModel().select(selected);
    editButton.setText(((IPContentType) selected.getKey()).getValue());

    contentType.valueProperty().addListener((obs, oldValue, newValue) -> {
      IPContentType newContentType = (IPContentType) newValue.getKey();
      descOb.setContentType(newContentType);
      if (oldValue != null || IPContentType.defaultIPContentType() != newContentType) {

        if (Constants.SIP_CONTENT_TYPE_OTHER.equalsIgnoreCase(descOb.getContentType().getValue())) {
          if ("".equals(descOb.getContentType().getOtherValue())) {
            editButton.setText(newContentType.getValue());
          } else {
            editButton.setText(newContentType.getOtherValue());
          }
        } else {
          editButton.setText(newContentType.getValue());
        }
      }

      Node otherTextFieldNode = null;
      for (Node node : result.getChildren()) {
        if (node instanceof TextField) {
          otherTextFieldNode = node;
          break;
        }
      }
      if (Constants.SIP_CONTENT_TYPE_OTHER.equalsIgnoreCase(newContentType.getValue())) {
        if (otherTextFieldNode == null) {
          result.getChildren().add(otherTextField);
          otherTextField.setText("");
        } else if (!oldValue.getKey().getPackageType().equalsIgnoreCase(newContentType.getPackageType())) {
          ((TextField) otherTextFieldNode).setText("");
        }
      } else {
        if (otherTextFieldNode != null) {
          result.getChildren().remove(otherTextFieldNode);
        }
      }
    });
    contentType.getSelectionModel().select(new Pair(descOb.getContentType(), descOb.getContentType().getValue()));
    contentType.setMinWidth(85);
    contentType.setCellFactory(param -> new ComboBoxListCell<Pair<IPContentType, String>>() {
      @Override
      public void updateItem(Pair<IPContentType, String> item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && item.getKey() != null) {
          String translation = I18n
            .t(Constants.I18N_INSPECTIONPANE_IP_CONTENT_TYPE_PREFIX + item.getValue().toString());
          if (translation == null || "".equals(translation))
            translation = item.getValue().toString();
          setTooltip(new Tooltip(translation));
        }
      }
    });
    contentType.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_SIP_TYPE_TOOLTIP)));
    return result;
  }

  private void createTopSubtitle(ImageView icon, String text) {
    paneTitle = new Label(text);
    paneTitle.setWrapText(true);
    paneTitle.getStyleClass().add(Constants.CSS_TOP_SUBTITLE);
    topSubtitle.setAlignment(Pos.CENTER_LEFT);
    topSubtitle.getChildren().clear();
    if (icon != null)
      topSubtitle.getChildren().addAll(icon, paneTitle);
    else
      topSubtitle.getChildren().add(paneTitle);
  }

  public void updateMetadataCombo(boolean selectLast) {
    metadataCombo.getSelectionModel().clearSelection();
    metadataCombo.getItems().clear();
    List<DescriptiveMetadata> metadataList = currentDescOb.getMetadata();
    List<Pair> comboList = new ArrayList<>();
    for (DescriptiveMetadata dom : metadataList) {
      String title = ConfigurationManager.getMetadataConfig(dom.getTemplateType() + Constants.CONF_K_SUFFIX_TITLE);
      if (title == null) {
        title = dom.getMetadataType();
      }
      if (title == null) {
        title = "N/A";
      }
      comboList.add(new Pair(dom, title)); // TODO
    }
    if (comboList.isEmpty()) {
      showMetadataHelp();
    } else {
      metadataCombo.getItems().addAll(comboList);
      if (selectLast) {
        metadataCombo.getSelectionModel().selectLast();
      } else {
        metadataCombo.getSelectionModel().selectFirst();
      }
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
    Platform.runLater(() -> {
      metaText.replaceText(content);
      try {
        metaText.setStyleSpans(0, XMLEditor.computeHighlighting(content));
      } catch (IndexOutOfBoundsException e) {
        LOGGER.warn("Error setting the StyleSpans", e);
      }
      metaText.moveTo(0);
    });
  }

  /**
   * Shows the help pane.
   */
  public void showHelp() {
    setCenter(centerHelp);
    setBottom(exportBox);
    setTop(topBox);
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

  public void addDataToSIP(TreeItem target) {
    Set<Path> paths = new HashSet<>();
    Set<SourceTreeItem> items = RodaInApplication.getSourceSelectedItems();
    for (SourceTreeItem item : items) {
      paths.add(Paths.get(item.getPath()));
    }

    addDataToSIP(target, paths);
  }

  public void addDataToSIP(TreeItem target, Set<Path> paths) {
    Set<ContentFilter> filters = new HashSet<>();
    filters.add(new ContentFilter());

    ContentCreator dc = new ContentCreator(filters, paths);
    Set<TreeNode> result = dc.start();

    // Set paths as mapped
    for (TreeNode tn : result) {
      PathCollection.addPath(tn.getPath(), PathState.MAPPED);
    }

    // Add to the SIP, either to a Directory or to a Representation
    // Also add to the tree
    if (target instanceof SipContentDirectory) {
      SipContentDirectory dir = (SipContentDirectory) target;

      SipContentDirectory parent = (SipContentDirectory) target;
      for (TreeNode treeNode : result) {
        dir.getTreeNode().add(treeNode);
        TreeItem<Object> startingItem = recCreateSipContent(treeNode, parent);
        parent.getChildren().add(startingItem);
      }
      parent.sortChildren();
    } else if (target instanceof SipContentRepresentation) {
      SipRepresentation rep = ((SipContentRepresentation) target).getRepresentation();

      SipContentRepresentation parent = (SipContentRepresentation) target;
      for (TreeNode treeNode : result) {
        rep.addFile(treeNode);
        TreeItem<Object> startingItem = recCreateSipContent(treeNode, parent);
        parent.getChildren().add(startingItem);
      }
      parent.sortChildren();
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
    Set<SourceTreeItem> items = RodaInApplication.getSourceSelectedItems();
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

    ContentCreator dc = new ContentCreator(filters, paths);
    Set<TreeNode> result = dc.start();

    if (target instanceof SipContentDirectory) {
      SipContentDirectory dir = (SipContentDirectory) target;
      for (TreeNode tn : result) {
        dir.getTreeNode().add(tn);
      }
    } else {
      currentSIPNode.getSip().addDocumentation(result);
    }
    SipContentDirectory parent = target != null ? (SipContentDirectory) target : docsRoot;
    for (TreeNode treeNode : result) {
      TreeItem<Object> startingItem = recCreateSipContent(treeNode, parent);
      if (!containsDocumentation(parent.getChildren(), startingItem)) {
        parent.getChildren().add(startingItem);
      }
    }
    parent.sortChildren();
  }

  private boolean containsDocumentation(ObservableList<TreeItem<Object>> list, TreeItem<Object> itemToAdd) {
    boolean contains = false;
    if (list != null && list.size() > 0) {
      Iterator<TreeItem<Object>> it = list.iterator();
      while (it.hasNext()) {
        TreeItem<Object> next = it.next();
        String nextValue = (String) next.getValue();
        String itemValue = (String) itemToAdd.getValue();
        if (nextValue.equalsIgnoreCase(itemValue)) {
          contains = true;
          break;
        }
      }

    }
    return contains;
  }

  public List<InspectionTreeItem> getDocumentationSelectedItems() {
    return new ArrayList<>(sipDocumentation.getSelectionModel().getSelectedItems());
  }

  public List<InspectionTreeItem> getDataSelectedItems() {
    return new ArrayList<>(sipFiles.getSelectionModel().getSelectedItems());
  }

  public void updateMetadataList(Sip descriptionObject) {
    if (descriptionObject == currentDescOb) {
      updateMetadataCombo(true);
    }
  }

  public void showAddMetadataError(DescriptiveMetadata metadataToAdd) {
    String showContent = String.format(I18n.t(Constants.I18N_INSPECTIONPANE_ADD_METADATA_ERROR_CONTENT),
      metadataToAdd.getId());
    Alert dlg = new Alert(Alert.AlertType.INFORMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t(Constants.I18N_INSPECTIONPANE_ADD_METADATA_ERROR_HEADER));
    dlg.setTitle(I18n.t(Constants.I18N_INSPECTIONPANE_ADD_METADATA_ERROR_TITLE));
    dlg.setContentText(showContent);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);
    dlg.getDialogPane().setMinHeight(180);
    dlg.show();
  }

  private HBox createRepresentationTypeComboBox() {
    HBox result = new HBox(5);
    result.setAlignment(Pos.CENTER_LEFT);

    TextField otherTextField = new TextField();
    otherTextField.textProperty().addListener((obs, oldValue, newValue) -> {
      if (sipFiles.getSelectionModel().getSelectedItem().getClass() == SipContentRepresentation.class) {
        SipContentRepresentation scr = (SipContentRepresentation) sipFiles.getSelectionModel().getSelectedItem();
        RepresentationContentType other = new RepresentationContentType(scr.getRepresentation().getType());
        if (Constants.SIP_CONTENT_TYPE_OTHER.equalsIgnoreCase(other.getValue())) {
          other.setOtherValue(newValue);
        }
        scr.getRepresentation().setType(other);
        editRepresentationTypeButton.setText(newValue);
      }
    });
    ComboBox<Pair<RepresentationContentType, String>> representationContentType = new ComboBox<>();
    List<Pair<RepresentationContentType, String>> representationContentTypeList = new ArrayList<>();

    result.getChildren().addAll(representationContentType);

    for (Pair<SipType, List<RepresentationContentType>> rct : SipType.getFilteredRepresentationContentTypes()) {
      for (RepresentationContentType repContentType : rct.getValue()) {
        RepresentationContentType rCT = new RepresentationContentType(repContentType);
        Pair<RepresentationContentType, String> toAdd = new Pair<>(rCT, rCT.asString());
        representationContentTypeList.add(toAdd);
      }
    }
    Collections.sort(representationContentTypeList, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
    representationContentType.setItems(FXCollections.observableList(representationContentTypeList));
    representationContentType.valueProperty().addListener((obs, old, newValue) -> {
      RepresentationContentType newRep = new RepresentationContentType((RepresentationContentType) newValue.getKey());

      Node otherTextFieldNode = null;
      for (Node node : result.getChildren()) {
        if (node instanceof TextField) {
          otherTextFieldNode = node;
          break;
        }
      }

      if (Constants.SIP_CONTENT_TYPE_OTHER.equalsIgnoreCase(newRep.getValue())) {
        if (otherTextFieldNode == null) {
          otherTextField.setText("");
          result.getChildren().add(otherTextField);
        }
      } else {
        if (otherTextFieldNode != null) {
          result.getChildren().remove(otherTextFieldNode);
        }
      }
      if (sipFiles.getSelectionModel().getSelectedItem().getClass() == SipContentRepresentation.class) {
        SipContentRepresentation scr = (SipContentRepresentation) sipFiles.getSelectionModel().getSelectedItem();
        scr.getRepresentation().setType(newRep);
        editRepresentationTypeButton.setText(scr.getRepresentation().getType().getValue());
      }
    });

    representationContentType.setMinWidth(85);
    representationContentType.setCellFactory(param -> new ComboBoxListCell<Pair<RepresentationContentType, String>>() {
      @Override
      public void updateItem(Pair<RepresentationContentType, String> item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && item.getKey() != null) {
          String translation = I18n
            .t(Constants.I18N_INSPECTIONPANE_REPRESENTATION_CONTENT_TYPE_PREFIX + item.getValue().toString());
          if (translation == null || "".equals(translation))
            translation = item.getValue().toString();
          setTooltip(new Tooltip(translation));
        }
      }
    });
    representationContentType.setTooltip(new Tooltip(I18n.t(Constants.I18N_INSPECTIONPANE_SIP_TYPE_TOOLTIP)));
    return result;
  }

  public void showEditRepresentationTypeButton(SipContentRepresentation scr) {
    setCurrentRepresentation(scr);
    RepresentationContentType type = scr.getRepresentation().getType();
    editRepresentationTypeButton.setText(type.getValue());
    editRepresentationTypeButton.setVisible(true);
  }

  public void hideEditRepresentationTypeButton() {
    setCurrentRepresentation(null);
    editRepresentationTypeButton.setVisible(false);
  }

  public SipContentRepresentation getCurrentRepresentation() {
    return currentRepresentation;
  }

  public void setCurrentRepresentation(SipContentRepresentation currentRepresentation) {
    this.currentRepresentation = currentRepresentation;
  }

}
