package org.roda.rodain.inspection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
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
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import org.apache.commons.lang.StringUtils;
import org.controlsfx.control.PopOver;
import org.fxmisc.richtext.CodeArea;
import org.json.JSONArray;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.inspection.trees.*;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.sip.MetadataValue;
import org.roda.rodain.sip.SipPreview;
import org.roda.rodain.sip.SipRepresentation;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.ModalStage;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.roda_project.commons_ip.model.IPContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
  private DescriptionObject currentDescOb;
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
  private ComboBox<UIPair> metadataCombo;
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
    createLoadingPanes();
    createMultipleSelectedBottom();

    center = new VBox(10);
    center.setPadding(new Insets(10, 0, 10, 0));

    setCenter(centerHelp);

    metadata.minHeightProperty().bind(stage.heightProperty().multiply(0.40));
    this.minWidthProperty().bind(stage.widthProperty().multiply(0.3));
  }

  private void createTop() {
    Label title = new Label(I18n.t("InspectionPane.title").toUpperCase());
    title.getStyleClass().add("title");
    title.setMinWidth(110);
    topSubtitle = new HBox(1);
    HBox.setHgrow(topSubtitle, Priority.ALWAYS);

    topBox = new HBox(15);
    topBox.getStyleClass().add("title-box");
    topBox.getChildren().addAll(title, topSubtitle);
    topBox.setPadding(new Insets(15, 15, 15, 15));
    topBox.setAlignment(Pos.CENTER_LEFT);
  }

  private void createMetadata() {
    metadata = new VBox();
    metadata.getStyleClass().add("inspectionPart");
    VBox.setVgrow(metadata, Priority.ALWAYS);

    metadataGrid = new GridPane();
    metadataGrid.setVgap(5);
    metadataGrid.setPadding(new Insets(5, 5, 5, 5));
    metadataGrid.setStyle(AppProperties.getStyle("backgroundWhite"));
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
    Label title = new Label(I18n.t("InspectionPane.addMetadata"));
    title.getStyleClass().add("helpTitle");
    title.setTextAlignment(TextAlignment.CENTER);
    titleBox.getChildren().add(title);

    Button addMetadataBtn = new Button(I18n.t("add"));
    addMetadataBtn.setMinHeight(65);
    addMetadataBtn.setMinWidth(130);
    addMetadataBtn.setMaxWidth(130);
    addMetadataBtn.setOnAction(event -> addMetadataAction());
    addMetadataBtn.getStyleClass().add("helpButton");

    box.getChildren().addAll(titleBox, addMetadataBtn);
    metadataHelpBox.getChildren().add(box);
  }

  private void createMetadataTextBox() {
    metaText = new CodeArea();
    VBox.setVgrow(metaText, Priority.ALWAYS);
    metaText.textProperty().addListener((observable, oldValue, newValue) -> {
      metaText.setStyleSpans(0, XMLEditor.computeHighlighting(newValue));
      UIPair selectedPair = metadataCombo.getSelectionModel().getSelectedItem();
      DescObjMetadata selected = (DescObjMetadata) selectedPair.getKey();
      if (oldValue != null && !"".equals(oldValue) && topButtons != null && topButtons.contains(toggleForm)
        && !textBoxCancelledChange) {
        String changeContent = I18n.t("InspectionPane.changeTemplate.content");
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.initStyle(StageStyle.UNDECORATED);
        dlg.setHeaderText(I18n.t("InspectionPane.changeTemplate.header"));
        dlg.setTitle(I18n.t("InspectionPane.changeTemplate.title"));
        dlg.setContentText(changeContent);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(stage);
        dlg.showAndWait();

        if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
          selected.setContentDecoded(newValue);
          selected.setValues(null);
          selected.setCreatorOption(MetadataOptions.NEW_FILE);
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
    metadataTopBox = new HBox();
    metadataTopBox.getStyleClass().add("hbox");
    metadataTopBox.setPadding(new Insets(5, 15, 5, 15));
    metadataTopBox.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label(I18n.t("InspectionPane.metadata").toUpperCase());
    titleLabel.getStyleClass().add("title");
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    toggleForm = new ToggleButton();
    toggleForm.setTooltip(new Tooltip(I18n.t("InspectionPane.textContent")));
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
        toggleForm.setTooltip(new Tooltip(I18n.t("InspectionPane.textContent")));
        textBoxCancelledChange = false;
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
    Platform.runLater(() -> validationButton
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.WHITE))));

    validationButton.setOnAction(event -> validationAction());

    addMetadata = new Button();
    addMetadata.setTooltip(new Tooltip(I18n.t("InspectionPane.addMetadata")));
    Platform.runLater(() -> addMetadata
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PLUS, Color.WHITE))));
    addMetadata.setOnAction(event -> addMetadataAction());

    removeMetadata = new Button();
    removeMetadata.setTooltip(new Tooltip(I18n.t("InspectionPane.removeMetadata")));
    Platform.runLater(() -> removeMetadata
      .setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.MINUS, Color.WHITE))));
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
        if (oldValue != null)
          saveMetadataPrivate((DescObjMetadata) oldValue.getKey());
        // we need this to prevent the alert from being shown
        textBoxCancelledChange = true;
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

    String remContent = I18n.t("InspectionPane.removeMetadata.content");
    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t("InspectionPane.removeMetadata.header"));
    dlg.setTitle(I18n.t("InspectionPane.removeMetadata.title"));
    dlg.setContentText(remContent);
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
            UIPair selectedInCombo = metadataCombo.getSelectionModel().getSelectedItem();
            if (selectedInCombo != null) {
              DescObjMetadata dom = (DescObjMetadata) selectedInCombo.getKey();
              if (Utils.validateSchema(metaText.getText(), dom.getSchema())) {
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
    Set<MetadataValue> metadataValues = getMetadataValues();
    if (metadataValues == null || metadataValues.isEmpty()) {
      noForm();
      return;
    }
    int i = 0;
    for (MetadataValue metadataValue : metadataValues) {
      // do not process this entry if it's marked as hidden
      if (getBooleanOption(metadataValue.get("hidden")))
        continue;

      Label label = new Label((String) metadataValue.get("label"));
      label.setWrapText(true);
      label.getStyleClass().add("formLabel");
      if (getBooleanOption(metadataValue.get(("mandatory"))))
        label.setStyle(AppProperties.getStyle("boldFont"));

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
            control = createFormCombo(metadataValue);
            break;
          case "date":
            control = createFormDatePicker(metadataValue);
            break;
          default:
            control = createFormTextField(metadataValue);
            break;
        }
      }

      metadataGrid.add(label, 0, i);
      metadataGrid.add(control, 1, i);
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

  private TextField createFormTextField(MetadataValue metadataValue) {
    TextField textField = new TextField((String) metadataValue.get("value"));
    HBox.setHgrow(textField, Priority.ALWAYS);
    textField.setUserData(metadataValue);
    textField.textProperty().addListener((observable2, oldValue2, newValue2) -> metadataValue.set("value", newValue2));
    if (metadataValue.getId().equals("title")) {
      textField.setId("descObjTitle");
    }
    addListenersToUpdateUI(metadataValue, textField.textProperty());
    return textField;
  }

  private TextArea createFormTextArea(MetadataValue metadataValue) {
    TextArea textArea = new TextArea((String) metadataValue.get("value"));
    HBox.setHgrow(textArea, Priority.ALWAYS);
    textArea.setUserData(metadataValue);
    textArea.textProperty().addListener((observable, oldValue, newValue) -> metadataValue.set("value", newValue));
    textArea.getStyleClass().add("form-text-area");
    addListenersToUpdateUI(metadataValue, textArea.textProperty());
    return textArea;
  }

  private ComboBox<String> createFormCombo(MetadataValue metadataValue) {
    ObservableList<String> comboList = FXCollections.observableArrayList();
    String input = (String) metadataValue.get("list");
    if (input != null) {
      JSONArray jsonArray = new JSONArray(input);
      jsonArray.forEach(o -> comboList.add((String) o));
    }
    ComboBox<String> comboBox = new ComboBox<>(comboList);
    HBox.setHgrow(comboBox, Priority.ALWAYS);
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.setUserData(metadataValue);
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> metadataValue.set("value", newValue));
    String currentValue = (String) metadataValue.get("value");
    if (currentValue != null) {
      comboBox.getSelectionModel().select(currentValue);
    }
    addListenersToUpdateUI(metadataValue, comboBox.valueProperty());

    return comboBox;
  }

  private void addListenersToUpdateUI(MetadataValue metadataValue, Property property) {
    if (metadataValue.getId().equals("title")) {
      paneTitle.textProperty().bind(property);
      if (currentSIPNode != null) {
        property.bindBidirectional(currentSIPNode.valueProperty());
      } else {
        if (currentSchema != null) {
          property.bindBidirectional(currentSchema.valueProperty());
        }
      }
    }
    if (metadataValue.getId().equals("level")) {
      property.addListener((observable, oldValue, newValue) -> {
        TreeItem<String> itemToForceUpdate = null;
        // Update the icons of the description level
        if (currentSIPNode != null) {
          if (newValue instanceof String) {
            currentSIPNode.updateDescriptionLevel((String) newValue);
            itemToForceUpdate = currentSIPNode;
          }
        } else if (currentSchema != null) {
          if (newValue instanceof String) {
            currentSchema.updateDescriptionLevel((String) newValue);
            itemToForceUpdate = currentSchema;
          }
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

  private DatePicker createFormDatePicker(MetadataValue metadataValue) {
    String pattern = "yyyy-MM-dd";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    LocalDateStringConverter ldsc = new LocalDateStringConverter(formatter, null);

    String currentValue = metadataValue.get("value") != null ? (String) metadataValue.get("value") : "";
    DatePicker datePicker = new DatePicker(ldsc.fromString(currentValue));
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

  private Set<MetadataValue> getMetadataValues() {
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

  private void saveMetadataPrivate(DescObjMetadata selectedDescObjMetadata) {
    String oldMetadata = null, newMetadata = null;
    if (currentDescOb != null) {
      newMetadata = metaText.getText();
      oldMetadata = selectedDescObjMetadata.getContentDecoded();
    }
    // only update if there's been modifications or there's no old
    // metadata and the new isn't empty
    boolean update = false;
    if (selectedDescObjMetadata.getCreatorOption() == MetadataOptions.TEMPLATE) {
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
    UIPair selectedObject = metadataCombo.getSelectionModel().getSelectedItem();
    DescObjMetadata selectedDescObjMetadata;
    if (selectedObject != null && selectedObject.getKey() instanceof DescObjMetadata)
      selectedDescObjMetadata = (DescObjMetadata) selectedObject.getKey();
    else
      return;
    if (metadata.getChildren().contains(metadataFormWrapper)) {
      if (currentDescOb != null) {
        currentDescOb.updatedMetadata(selectedDescObjMetadata);
        updateTextArea(currentDescOb.getMetadataWithReplaces(selectedDescObjMetadata));
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
    Label title = new Label(I18n.t("InspectionPane.help.title"));
    title.getStyleClass().add("helpTitle");
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
    top.setPadding(new Insets(4, 15, 3, 15));

    Label title = new Label(I18n.t("data").toUpperCase());
    title.setPadding(new Insets(5, 0, 0, 0));
    title.getStyleClass().add("title");
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
    docsRoot = new SipContentDirectory(new TreeNode(Paths.get("")), null);
    sipDocumentation.setRoot(docsRoot);
    toggleDocumentation = new ToggleButton();
    toggleDocumentation.setTooltip(new Tooltip(I18n.t("documentation")));
    Platform.runLater(() -> {
      Image selected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.OPEN_FOLDER, Color.WHITE);
      Image unselected = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.BOOK, Color.WHITE);
      ImageView toggleImage = new ImageView();
      toggleDocumentation.setGraphic(toggleImage);
      toggleImage.imageProperty()
        .bind(Bindings.when(toggleDocumentation.selectedProperty()).then(selected).otherwise(unselected));
    });
    title.textProperty().bind(Bindings.when(toggleDocumentation.selectedProperty())
      .then(I18n.t("documentation").toUpperCase()).otherwise(I18n.t("data").toUpperCase()));

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
        dataBox.getChildren().clear();
        dataBox.getChildren().add(sipFiles);
        content.setCenter(dataBox);
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
      LOGGER.error("Error reading loading GIF", e);
    }
  }

  private void createContentBottom() {
    contentBottom = new HBox(10);
    contentBottom.setPadding(new Insets(10, 10, 10, 10));
    contentBottom.setAlignment(Pos.CENTER);

    ignore = new Button(I18n.t("remove"));
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

    Button addRepresentation = new Button(I18n.t("InspectionPane.addRepresentation"));
    addRepresentation.setOnAction(event -> {
      int repCount = currentSIPNode.getSip().getRepresentations().size() + 1;
      SipRepresentation sipRep = new SipRepresentation("rep" + repCount);
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

  private void createMultipleSelectedBottom() {
    BorderPane help = new BorderPane();
    help.getStyleClass().add("inspectionPart");

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(5, 15, 10, 15));

    Label title = new Label(I18n.t("InspectionPane.multipleSelected.helpTitle").toUpperCase());
    title.setPadding(new Insets(5, 0, 0, 0));
    title.getStyleClass().add("title");
    top.getChildren().add(title);
    help.setTop(top);

    Label multSelectedHelp = new Label(I18n.t("InspectionPane.multipleSelected.help"));
    multSelectedHelp.setPadding(new Insets(10, 10, 10, 10));
    multSelectedHelp.setStyle("-fx-text-fill: black");
    multSelectedHelp.setWrapText(true);

    help.setCenter(multSelectedHelp);

    BorderPane confirm = new BorderPane();
    confirm.getStyleClass().add("inspectionPart");

    HBox confirmTop = new HBox();
    confirmTop.getStyleClass().add("hbox");
    confirmTop.setPadding(new Insets(5, 15, 10, 15));

    Label confirmTitle = new Label(I18n.t("apply").toUpperCase());
    confirmTitle.setAlignment(Pos.CENTER_LEFT);
    confirmTitle.setPadding(new Insets(5, 0, 0, 0));
    confirmTitle.getStyleClass().add("title");
    confirmTop.getChildren().add(confirmTitle);
    confirm.setTop(confirmTop);

    PopOver applyPopOver = new PopOver();
    applyPopOver.setDetachable(false);
    applyPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);

    HBox popOverContent = new HBox(10);
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label popOverTitle = new Label(I18n.t("InspectionPane.multipleSelected.appliedMessage"));
    popOverTitle.setStyle("-fx-font-size: 16px");
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.GREEN, 32));
      popOverContent.getChildren().addAll(popOverTitle, iv);
    });
    applyPopOver.setContentNode(popOverContent);

    HBox multSelectedSaveBox = new HBox(5);
    multSelectedSaveBox.setPadding(new Insets(10, 10, 10, 10));
    multSelectedSaveBox.getStyleClass();
    multSelectedSaveBox.setStyle("-fx-text-fill: black");
    Label confirmationLabel = new Label(I18n.t("InspectionPane.multipleSelected.confirm"));
    confirmationLabel.setStyle("-fx-text-fill: black;");
    Button save = new Button(I18n.t("apply"));
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
    List<DescObjMetadata> metadataList = currentDescOb.getMetadata();
    if (!metadataList.isEmpty()) {
      selectedItems.forEach(item -> {
        DescriptionObject itemDO = null;
        if (item instanceof SchemaNode)
          itemDO = ((SchemaNode) item).getDob();
        if (item instanceof SipPreviewNode)
          itemDO = ((SipPreviewNode) item).getSip();
        if (itemDO != null) {
          for (DescObjMetadata metadataObj : metadataList) {
            applyMetadataFileToDescriptionObject(metadataObj, itemDO, item);
          }
        }
      });
    }
  }

  private void applyMetadataFileToDescriptionObject(DescObjMetadata metadataObj, DescriptionObject descObj,
    TreeItem<String> treeItem) {
    if (metadataObj.getCreatorOption() != MetadataOptions.TEMPLATE) {
      // remove the metadata files with the same ID as the new one
      List<DescObjMetadata> toRemove = new ArrayList<>();
      descObj.getMetadata().forEach(descObjMetadata -> {
        if (descObjMetadata.getId().equals(metadataObj.getId()))
          toRemove.add(descObjMetadata);
      });
      descObj.getMetadata().removeAll(toRemove);
      // add a clone of the new metadata
      descObj.getMetadata().add(metadataObj.clone());
    } else {
      boolean merged = false;
      for (DescObjMetadata descObjMetadata : descObj.getMetadata()) {
        if (descObjMetadata.getId().equals(metadataObj.getId())) {
          merged = true;
          Set<MetadataValue> metadataObjValues = metadataObj.getValues();
          Set<MetadataValue> descObjMetadataValues = descObjMetadata.getValues();
          for (MetadataValue metadataObjValue : metadataObjValues) {
            for (MetadataValue descObjMetadataValue : descObjMetadataValues) {
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
    rules.getStyleClass().add("inspectionPart");
    VBox.setVgrow(rules, Priority.ALWAYS);
    rules.setMinHeight(200);

    HBox top = new HBox();
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(10, 15, 10, 15));

    Label title = new Label(I18n.t("InspectionPane.rules").toUpperCase());
    title.getStyleClass().add("title");
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
    metadataTask.setOnSucceeded(Void -> {
      if (metadataTask != null && metadataTask == thisMetadataTask) {
        Set<MetadataValue> values = currentDescOb.getMetadataValueMap(dom);
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
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);
    // we need to account for the size of the combo-box, otherwise the top box
    // is too tall
    topBox.setPadding(new Insets(11, 15, 11, 15));

    PopOver editPopOver = new PopOver();
    editPopOver.setDetachable(false);
    editPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add("inspectionPart");
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    Label sipTypeLabel = new Label(I18n.t("InspectionPane.sipTypeTooltip"));
    sipTypeLabel.setStyle("-fx-text-fill: black");
    popOverContent.getChildren().addAll(sipTypeLabel, createSIPTypeComboBox());
    editPopOver.setContentNode(popOverContent);

    Button editButton = new Button();
    Platform.runLater(() -> {
      ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.PENCIL, Color.WHITE, 16));
      editButton.setGraphic(iv);
    });
    editButton.setOnAction(event -> editPopOver.show(editButton));

    top.getChildren().addAll(space, editButton);

    topSubtitle.getChildren().addAll(space, top);

    updateMetadataCombo();

    /* Center */
    center.getChildren().clear();

    // content tree
    boolean documentation = toggleDocumentation.isSelected();
    createContent(sip, !documentation);
    createDocumentation(sip, documentation);

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
    selectedItems = null;
    currentSchema = node;
    if (contentTask != null && contentTask.isRunning()) {
      contentTask.cancel(true);
    }
    if (metadataTask != null && metadataTask.isRunning()) {
      metadataTask.cancel(true);
    }

    /* top */
    topBox.setPadding(new Insets(15, 15, 15, 15));
    ImageView iconView = new ImageView(node.getIconWhite());
    node.graphicProperty()
      .addListener((observable, oldValue, newValue) -> iconView.setImage(((ImageView) newValue).getImage()));
    createTopSubtitle(iconView, node.getValue());

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

  public void update(List<TreeItem<String>> selectedItems) {
    setTop(topBox);
    currentSchema = null;
    currentSIPNode = null;
    this.selectedItems = selectedItems;
    // create a temporary description object to hold the metadata
    currentDescOb = new DescriptionObject();
    currentDescOb.getMetadata().clear();

    Map<String, MetadataValue> commonMV = new HashMap<>();
    String commonTemplate = null, commonVersion = null, commonMetadataType = null;
    boolean common = true;
    for (TreeItem ti : selectedItems) {
      System.out.println("----------------------------\nSelected item: " + ti.getValue());
      System.out.println("Common: " + common);

      if (!common)
        continue;

      DescriptionObject dob;
      if (ti instanceof SipPreviewNode)
        dob = ((SipPreviewNode) ti).getSip();
      else if (ti instanceof SchemaNode)
        dob = ((SchemaNode) ti).getDob();
      else
        continue;

      for (DescObjMetadata dobm : dob.getMetadata()) {
        // Check if the creator option, template and version are the same as the
        // previously analysed items
        if (dobm.getCreatorOption() != MetadataOptions.TEMPLATE) {
          common = false;
          System.out.println("Not template: " + dobm.getCreatorOption());
          continue;
        }
        if (commonTemplate == null)
          commonTemplate = dobm.getTemplateType();
        if (commonVersion == null)
          commonVersion = dobm.getVersion();
        if (commonMetadataType == null) {
          commonMetadataType = dobm.getMetadataType();
        }

        System.out.println("CommonTemplate: " + commonTemplate);
        System.out.println("CommonVersion: " + commonVersion);
        System.out.println("CommonMetadataType: " + commonMetadataType);

        common =    commonTemplate != null       && commonTemplate.equals(dobm.getTemplateType())
                &&  commonVersion != null        && commonVersion.equals(dobm.getVersion())
                &&  commonMetadataType != null   && commonMetadataType.equals(dobm.getMetadataType());
        // Add the metadata values to the common set
        for (MetadataValue mv : dob.getMetadataValueMap(dobm)) {
          if (commonMV.containsKey(mv.getId())) {
            if (mv.get("value") == null && commonMV.get(mv.getId()).get("value") == null)
              continue;
            String mvValue = (String) mv.get("value");
            String commonMVvalue = (String) commonMV.get(mv.getId()).get("value");
            if (mvValue == null || !mvValue.equals(commonMVvalue)) {
              commonMV.get(mv.getId()).set("value", "{{mixed}}");
            }
          } else {
            commonMV.put(mv.getId(), mv);
          }
        }
      }
    }

    if (common) {
      DescObjMetadata dobm = new DescObjMetadata(MetadataOptions.TEMPLATE, commonTemplate, commonMetadataType,
        commonVersion);
      dobm.setValues(new TreeSet<>(commonMV.values()));
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
    createTopSubtitle(null, String.format("%d %s", selectedItems.size(), I18n.t("items")));

    /* center */
    center.getChildren().clear();
    metadata.getChildren().clear();
    metadata.getChildren().addAll(metadataTopBox);
    updateMetadataCombo();

    center.getChildren().addAll(metadata, multSelectedBottom);
    setCenter(center);
  }

  private HBox createSIPTypeComboBox() {
    SipPreview sip = currentSIPNode.getSip();
    HBox result = new HBox(10);
    result.setAlignment(Pos.CENTER_LEFT);

    // Text field for the OTHER content type
    TextField otherTextField = new TextField();
    otherTextField.textProperty().addListener((obs, old, newValue) -> sip.getContentType().setOtherType(newValue));
    // Content Type combo box
    ComboBox<UIPair> contentType = new ComboBox<>();
    List<UIPair> contTypeList = new ArrayList<>();

    result.getChildren().addAll(contentType);

    for (IPContentType.IPContentTypeEnum ct : IPContentType.IPContentTypeEnum.values()) {
      IPContentType ipCT = new IPContentType(ct);
      contTypeList.add(new UIPair(ipCT, ipCT.getType()));
    }
    // sort the list as strings
    Collections.sort(contTypeList, (o1, o2) -> o1.toString().compareTo(o2.toString()));
    contentType.setItems(FXCollections.observableList(contTypeList));
    contentType.valueProperty().addListener((obs, old, newValue) -> {
      sip.setContentType((IPContentType) newValue.getKey());
      if (((IPContentType) newValue.getKey()).getType() == IPContentType.IPContentTypeEnum.OTHER) {
        if (!result.getChildren().contains(otherTextField)) {
          result.getChildren().add(otherTextField);
          otherTextField.setText(sip.getContentType().getOtherType());
        }
      } else {
        if (result.getChildren().contains(otherTextField))
          result.getChildren().remove(otherTextField);
      }
    });
    contentType.getSelectionModel().select(new UIPair(sip.getContentType(), sip.getContentType().getType()));
    contentType.setMinWidth(85);
    contentType.setCellFactory(param -> new ComboBoxListCell<UIPair>() {
      @Override
      public void updateItem(UIPair item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && item.getKey() != null) {
          String translation = I18n.t("IPContentType." + item.getValue().toString());
          if (translation == null || "".equals(translation))
            translation = item.getValue().toString();
          setTooltip(new Tooltip(translation));
        }
      }
    });
    contentType.setTooltip(new Tooltip(I18n.t("InspectionPane.sipTypeTooltip")));
    return result;
  }

  private void createTopSubtitle(ImageView icon, String text) {
    paneTitle = new Label(text);
    paneTitle.setWrapText(true);
    paneTitle.getStyleClass().add("top-subtitle");
    topSubtitle.setAlignment(Pos.CENTER_LEFT);
    topSubtitle.getChildren().clear();
    if (icon != null)
      topSubtitle.getChildren().addAll(icon, paneTitle);
    else
      topSubtitle.getChildren().add(paneTitle);
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
    Platform.runLater(() -> {
      metaText.replaceText(content);
      metaText.setStyleSpans(0, XMLEditor.computeHighlighting(content));
      metaText.moveTo(0);
    });
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

  public void addDataToSIP(TreeItem target) {
    Set<Path> paths = new HashSet<>();
    Set<SourceTreeItem> items = RodaIn.getSourceSelectedItems();
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
      PathCollection.addPath(tn.getPath().toString(), SourceTreeItemState.MAPPED);
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

    ContentCreator dc = new ContentCreator(filters, paths);
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

  public List<InspectionTreeItem> getDocumentationSelectedItems() {
    return new ArrayList<>(sipDocumentation.getSelectionModel().getSelectedItems());
  }

  public List<InspectionTreeItem> getDataSelectedItems() {
    return new ArrayList<>(sipFiles.getSelectionModel().getSelectedItems());
  }

  public void updateMetadataList(DescriptionObject descriptionObject) {
    if (descriptionObject == currentDescOb) {
      updateMetadataCombo();
      RodaIn.getSchemePane().setModifiedPlan(true);
    }
  }

  public void showAddMetadataError(DescObjMetadata metadataToAdd) {
    String showContent = String.format(I18n.t("InspectionPane.addMetadataError.content"), metadataToAdd.getId());
    Alert dlg = new Alert(Alert.AlertType.INFORMATION);
    dlg.initStyle(StageStyle.UNDECORATED);
    dlg.setHeaderText(I18n.t("InspectionPane.addMetadataError.header"));
    dlg.setTitle(I18n.t("InspectionPane.addMetadataError.title"));
    dlg.setContentText(showContent);
    dlg.initModality(Modality.APPLICATION_MODAL);
    dlg.initOwner(stage);
    dlg.getDialogPane().setMinHeight(180);
    dlg.show();
  }
}
