package org.roda.rodain.ui.inspection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.rules.ui.HBoxCell;
import org.roda.rodain.ui.utils.FontAwesomeImageCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 23-03-2016.
 */
public class AddMetadataPane extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(AddMetadataPane.class.getName());

  private enum OPTIONS {
    TEMPLATE, SINGLE_FILE, EMPTY_FILE
  }

  private static final int LIST_HEIGHT = 440;
  private VBox boxMetadata;
  private ListView<HBoxCell> metaList;
  private ComboBox<Pair> templateTypes;
  private Button chooseFile, btCancel, btContinue;
  private TextField emptyFileNameTxtField, emptyFileMetadataTypeTxtField, emptyFileMetadataTypeVersionTxtField;

  private HBoxCell cellSingleFile, cellEmptyFile;
  private Stage stage;
  private Sip descriptionObject;
  private Path selectedPath;

  private ComboBox<Pair> comboTypesSingleFile;

  private Label error;

  private DescriptiveMetadata metadataToAdd;

  public DescriptiveMetadata getMetadataToAdd() {
    return metadataToAdd;
  }

  public void setMetadataToAdd(DescriptiveMetadata metadataToAdd) {
    this.metadataToAdd = metadataToAdd;
  }

  public AddMetadataPane(Stage stage, Sip descriptionObject) {
    this.stage = stage;
    this.descriptionObject = descriptionObject;

    getStyleClass().add(Constants.CSS_MODAL);

    createTop();
    createCenterMetadata();
    createBottom();
    this.setCenter(boxMetadata);
  }

  private void createTop() {
    StackPane pane = new StackPane();
    pane.setPadding(new Insets(0, 0, 10, 0));

    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER_LEFT);
    box.getStyleClass().add(Constants.CSS_HBOX);
    box.setPadding(new Insets(10, 10, 10, 10));
    pane.getChildren().add(box);

    Label title = new Label(I18n.t(Constants.I18N_INSPECTIONPANE_ADDMETADATA));
    title.setId("title");

    box.getChildren().add(title);

    setTop(pane);
  }

  private void createCenterMetadata() {
    boxMetadata = new VBox();
    boxMetadata.setAlignment(Pos.TOP_LEFT);
    boxMetadata.setPadding(new Insets(0, 10, 0, 10));

    Label subtitle = new Label(I18n.t(Constants.I18N_RULEMODALPANE_METADATA_METHOD));
    subtitle.setId("sub-title");
    subtitle.setPadding(new Insets(0, 0, 10, 0));

    metaList = new ListView<>();
    metaList.setMinHeight(LIST_HEIGHT);
    metaList.setMaxHeight(LIST_HEIGHT);
    metaList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBoxCell>() {
      @Override
      public void changed(ObservableValue<? extends HBoxCell> observable, final HBoxCell oldValue, HBoxCell newValue) {
        if (newValue != null && newValue.isDisabled()) {
          Platform.runLater(() -> {
            if (oldValue != null) {
              metaList.getSelectionModel().select(oldValue);
            } else {
              metaList.getSelectionModel().clearSelection();
            }
          });
        }
      }
    });

    List<Pair> typesList = new ArrayList<>();
    String metaTypesRaw = ConfigurationManager.getConfig(Constants.CONF_K_METADATA_TYPES);
    if (metaTypesRaw != null) {
      String[] metaTypes = metaTypesRaw.split(Constants.MISC_COMMA);
      for (String type : metaTypes) {
        String title = ConfigurationManager.getMetadataConfig(type + Constants.CONF_K_SUFFIX_TITLE);
        if (title == null || type == null) {
          continue;
        }
        typesList.add(new Pair(type, title));
      }
    }

    String icon = ConfigurationManager.getStyle("metadata.template.icon");
    String title = I18n.t(Constants.I18N_METADATA_TEMPLATE_TITLE);
    String description = I18n.t(Constants.I18N_METADATA_TEMPLATE_DESCRIPTION);
    HBoxCell cellTemplate = new HBoxCell("meta4", icon, title, description, optionsTemplate());
    cellTemplate.setUserData(OPTIONS.TEMPLATE);

    icon = ConfigurationManager.getStyle("metadata.singleFile.icon");
    title = I18n.t(Constants.I18N_METADATA_SINGLE_FILE_TITLE);
    description = I18n.t(Constants.I18N_METADATA_SINGLE_FILE_DESCRIPTION);
    cellSingleFile = new HBoxCell("meta1", icon, title, description, optionsSingleFile(typesList));
    cellSingleFile.setUserData(OPTIONS.SINGLE_FILE);

    icon = ConfigurationManager.getStyle("metadata.emptyFile.icon");
    title = I18n.t(Constants.I18N_METADATA_EMPTY_FILE_TITLE);
    description = I18n.t(Constants.I18N_METADATA_EMPTY_FILE_DESCRIPTION);
    cellEmptyFile = new HBoxCell("meta2", icon, title, description, optionsEmptyFile(typesList));
    cellEmptyFile.setUserData(OPTIONS.EMPTY_FILE);

    ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
    hboxList.addAll(cellTemplate, cellSingleFile, cellEmptyFile);
    metaList.setItems(hboxList);

    metaList.getSelectionModel().selectFirst();

    if (templateTypes.getItems().isEmpty()) {
      cellTemplate.setDisable(true);
      metaList.getSelectionModel().select(1);
    }

    boxMetadata.getChildren().addAll(subtitle, metaList);
  }

  private HBox optionsSingleFile(List<Pair> metaTypeList) {
    HBox box = new HBox(5);
    error = new Label("");
    box.setAlignment(Pos.CENTER_LEFT);

    chooseFile = new Button(I18n.t(Constants.I18N_RULEMODALPANE_CHOOSE_FILE));
    chooseFile.setOnAction(event -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellSingleFile);
      FileChooser chooser = new FileChooser();
      chooser.setTitle(I18n.t(Constants.I18N_FILE_CHOOSER_TITLE));
      File selectedFile = chooser.showOpenDialog(stage);
      if (selectedFile == null) {
        return;
      }
      selectedPath = selectedFile.toPath();
      chooseFile.setText(selectedPath.getFileName().toString());
      chooseFile.setUserData(selectedPath.toString());
    });

    Label typeLabel = new Label(I18n.t(Constants.I18N_TYPE) + Constants.MISC_COLON);
    comboTypesSingleFile = new ComboBox<>(FXCollections.observableList(metaTypeList));
    comboTypesSingleFile.getSelectionModel().selectFirst();
    comboTypesSingleFile.valueProperty().addListener(observable -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellSingleFile);
    });
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    box.getChildren().addAll(chooseFile, error, space, typeLabel, comboTypesSingleFile);
    return box;
  }

  private HBox optionsEmptyFile(List<Pair> metaTypeList) {
    HBox box = new HBox(5);
    box.setAlignment(Pos.CENTER_LEFT);

    Label lab = new Label(I18n.t(Constants.I18N_NAME));
    emptyFileNameTxtField = new TextField("metadata.xml");

    Label typeLabel = new Label(I18n.t(Constants.I18N_TYPE) + Constants.MISC_COLON);
    emptyFileMetadataTypeTxtField = new TextField(I18n.t(Constants.I18N_TYPE));
    Label versionlabel = new Label(I18n.t(Constants.I18N_VERSION) + Constants.MISC_COLON);
    emptyFileMetadataTypeVersionTxtField = new TextField(I18n.t(Constants.I18N_VERSION));

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    box.getChildren().addAll(lab, emptyFileNameTxtField, space, typeLabel, emptyFileMetadataTypeTxtField, versionlabel,
      emptyFileMetadataTypeVersionTxtField);
    return box;
  }

  private HBox optionsTemplate() {
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);

    templateTypes = new ComboBox<>();
    String templatesRaw = ConfigurationManager.getConfig(Constants.CONF_K_METADATA_TEMPLATES);
    String[] templates = templatesRaw.split(Constants.MISC_COMMA);
    for (String templ : templates) {
      String trimmed = templ.trim();

      String title = ConfigurationManager.getMetadataConfig(trimmed + Constants.CONF_K_SUFFIX_TITLE);
      String type = ConfigurationManager.getMetadataConfig(trimmed + Constants.CONF_K_SUFFIX_TYPE);
      String version = ConfigurationManager.getMetadataConfig(trimmed + Constants.CONF_K_SUFFIX_VERSION);
      if (title == null) {
        continue;
      }
      String key = trimmed;
      if (type != null) {
        key += Constants.MISC_METADATA_SEP + type;
      }
      if (version != null) {
        key += Constants.MISC_METADATA_SEP + version;
      }
      String value = title;
      Pair newPair = new Pair(key, value);
      templateTypes.getItems().add(newPair);
    }
    templateTypes.getSelectionModel().selectFirst();

    box.getChildren().add(templateTypes);

    return box;
  }

  private void createBottom() {
    createContinueButton();
    createCancelButton();

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    VBox bottom = new VBox();
    VBox.setVgrow(bottom, Priority.ALWAYS);
    Separator separator = new Separator();

    HBox buttons = new HBox(10);
    buttons.setPadding(new Insets(10, 10, 10, 10));
    buttons.setAlignment(Pos.CENTER);
    buttons.getChildren().addAll(btCancel, space, btContinue);

    bottom.getChildren().addAll(separator, buttons);

    setBottom(bottom);
  }

  private void createContinueButton() {
    btContinue = new Button(I18n.t(Constants.I18N_CONTINUE));
    btContinue.setId("btConfirm");
    btContinue.setMaxWidth(120);
    btContinue.setMinWidth(120);
    Platform.runLater(() -> {
      Image im = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHEVRON_RIGHT, Color.WHITE);
      ImageView imv = new ImageView(im);
      btContinue.setGraphic(imv);
    });
    btContinue.setGraphicTextGap(10);
    btContinue.setContentDisplay(ContentDisplay.RIGHT);

    btContinue.setOnAction(event -> {
      HBoxCell selected = metaList.getSelectionModel().getSelectedItem();
      if (selected == null) {
        return;
      }
      if (selected.getUserData() instanceof OPTIONS) {
        OPTIONS option = (OPTIONS) selected.getUserData();
        metadataToAdd = null;
        switch (option) {
          case TEMPLATE:
            String rawTemplateType = (String) templateTypes.getSelectionModel().getSelectedItem().getKey();
            String[] splitted = rawTemplateType.split(Constants.MISC_METADATA_SEP);
            String templateType = splitted[0], metadataType = splitted[1],
              metadataVersion = splitted.length == 3 ? splitted[2] : null;
            metadataToAdd = new DescriptiveMetadata(MetadataOption.TEMPLATE, templateType, metadataType,
              metadataVersion);
            addRelatedTags(templateType, metadataToAdd);
            break;
          case SINGLE_FILE:
            if (selectedPath == null) {
              return;
            }
            metadataToAdd = new DescriptiveMetadata(MetadataOption.SINGLE_FILE, selectedPath, "", "", null);

            Pair metaType = comboTypesSingleFile.getSelectionModel().getSelectedItem();
            addTypeAndVersionToMetadata(metaType, metadataToAdd);
            try {
              if (!Controller.validateSchema(selectedPath, metadataToAdd.getSchema())) {
                metadataToAdd = null;
              }
            } catch (IOException | SAXException e) {
              metadataToAdd = null;
            }
            if (metadataToAdd == null) {
              error.setText(I18n.t(Constants.I18N_ERROR_VALIDATING_METADATA));
              error.getStyleClass().add(Constants.CSS_ERROR);
            }
            break;
          case EMPTY_FILE:
            String name = emptyFileNameTxtField.getText();
            if (name == null || "".equals(name)) {
              return;
            }
            metadataToAdd = new DescriptiveMetadata();
            metadataToAdd.setId(name);
            metadataToAdd.setContentDecoded("");
            if (emptyFileMetadataTypeTxtField.getText() != null) {
              metadataToAdd.setMetadataType(emptyFileMetadataTypeTxtField.getText());
            }
            if (emptyFileMetadataTypeVersionTxtField.getText() != null) {
              metadataToAdd.setMetadataVersion(emptyFileMetadataTypeVersionTxtField.getText());
            }
            metadataToAdd = Controller.updateTemplate(metadataToAdd);
            break;
        }

        if (metadataToAdd == null) {
          return;
        }
        boolean add = true;
        if (descriptionObject != null) {
          for (DescriptiveMetadata dom : descriptionObject.getMetadata()) {
            if (dom.getId().equals(metadataToAdd.getId())) {
              add = false;
            }
          }
          if (add) {
            descriptionObject.getMetadata().add(metadataToAdd);
            RodaInApplication.getInspectionPane().updateMetadataList(descriptionObject);
          } else {
            RodaInApplication.getInspectionPane().showAddMetadataError(metadataToAdd);
          }
        }
        stage.close();
      }
    });
  }

  private void addTypeAndVersionToMetadata(Pair metaType, DescriptiveMetadata metadataToAdd) {
    if (metaType != null) {
      String templateVersion = (String) metaType.getKey();
      String metadataVersion = ConfigurationManager
        .getMetadataConfig(templateVersion + Constants.CONF_K_SUFFIX_VERSION);
      String metadataType = ConfigurationManager.getMetadataConfig(templateVersion + Constants.CONF_K_SUFFIX_TYPE);
      metadataToAdd.setMetadataType(metadataType);
      metadataToAdd.setMetadataVersion(metadataVersion);
      metadataToAdd.setTemplateType(templateVersion);
      addRelatedTags(templateVersion, metadataToAdd);
    }
  }

  private void addRelatedTags(String templateVersion, DescriptiveMetadata metadataToAdd) {
    String metadataTags = ConfigurationManager.getMetadataConfig(templateVersion + Constants.CONF_K_SUFFIX_TAGS);
    if (StringUtils.isNotBlank(metadataTags)) {
      List<String> tagList = Arrays.asList(metadataTags.split(","));
      metadataToAdd.setRelatedTags(tagList);
    }
  }

  private void createCancelButton() {
    btCancel = new Button(I18n.t(Constants.I18N_CANCEL));
    btCancel.setMaxWidth(120);
    btCancel.setMinWidth(120);
    Platform.runLater(() -> {
      Image im = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.TIMES, Color.WHITE);
      ImageView imv = new ImageView(im);
      btCancel.setGraphic(imv);
    });
    btCancel.setGraphicTextGap(20);
    btCancel.setContentDisplay(ContentDisplay.RIGHT);

    btCancel.setOnAction(event -> stage.close());
  }
}
