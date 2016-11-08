package org.roda.rodain.inspection;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.rules.ui.HBoxCell;
import org.roda.rodain.rules.ui.RuleModalPane;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private ComboBox<UIPair> templateTypes;
  private Button chooseFile, btCancel, btContinue;
  private TextField emptyFileNameTxtField, emptyFileMetadataTypeTxtField, emptyFileMetadataTypeVersionTxtField;

  private HBoxCell cellSingleFile, cellEmptyFile;
  private Stage stage;
  private DescriptionObject descriptionObject;
  private Path selectedPath;

  private ComboBox<UIPair> comboTypesSingleFile;
  
  private DescObjMetadata metadataToAdd;
  
  

  public DescObjMetadata getMetadataToAdd() {
    return metadataToAdd;
  }

  public void setMetadataToAdd(DescObjMetadata metadataToAdd) {
    this.metadataToAdd = metadataToAdd;
  }

  public AddMetadataPane(Stage stage, DescriptionObject descriptionObject) {
    this.stage = stage;
    this.descriptionObject = descriptionObject;

    getStyleClass().add("modal");

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
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(10, 10, 10, 10));
    pane.getChildren().add(box);

    Label title = new Label(I18n.t("InspectionPane.addMetadata"));
    title.setId("title");

    box.getChildren().add(title);

    setTop(pane);
  }

  private void createCenterMetadata() {
    boxMetadata = new VBox();
    boxMetadata.setAlignment(Pos.TOP_LEFT);
    boxMetadata.setPadding(new Insets(0, 10, 0, 10));

    Label subtitle = new Label(I18n.t("RuleModalPane.metadataMethod"));
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
            if (oldValue != null)
              metaList.getSelectionModel().select(oldValue);
            else
              metaList.getSelectionModel().clearSelection();
          });
        }
      }
    });

    List<UIPair> typesList = new ArrayList<>();
    String metaTypesRaw = AppProperties.getConfig("metadata.types");
    if (metaTypesRaw != null) {
      String[] metaTypes = metaTypesRaw.split(",");
      for (String type : metaTypes) {
        String title = AppProperties.getConfig("metadata." + type + ".title");
        if (title == null || type == null)
          continue;
        typesList.add(new UIPair(type, title));
      }
    }

    String icon = AppProperties.getStyle("metadata.template.icon");
    String title = I18n.t("metadata.template.title");
    String description = I18n.t("metadata.template.description");
    HBoxCell cellTemplate = new HBoxCell("meta4", icon, title, description, optionsTemplate());
    cellTemplate.setUserData(OPTIONS.TEMPLATE);

    icon = AppProperties.getStyle("metadata.singleFile.icon");
    title = I18n.t("metadata.singleFile.title");
    description = I18n.t("metadata.singleFile.description");
    cellSingleFile = new HBoxCell("meta1", icon, title, description, optionsSingleFile(typesList));
    cellSingleFile.setUserData(OPTIONS.SINGLE_FILE);

    icon = AppProperties.getStyle("metadata.emptyFile.icon");
    title = I18n.t("metadata.emptyFile.title");
    description = I18n.t("metadata.emptyFile.description");
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

  private HBox optionsSingleFile(List<UIPair> metaTypeList) {
    HBox box = new HBox(5);
    box.setAlignment(Pos.CENTER_LEFT);

    chooseFile = new Button(I18n.t("RuleModalPane.chooseFile"));
    chooseFile.setOnAction(event -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellSingleFile);
      FileChooser chooser = new FileChooser();
      chooser.setTitle(I18n.t("filechooser.title"));
      File selectedFile = chooser.showOpenDialog(stage);
      if (selectedFile == null)
        return;
      selectedPath = selectedFile.toPath();
      chooseFile.setText(selectedPath.getFileName().toString());
      chooseFile.setUserData(selectedPath.toString());
    });

    Label typeLabel = new Label(I18n.t("type") + ":");
    comboTypesSingleFile = new ComboBox<>(FXCollections.observableList(metaTypeList));
    comboTypesSingleFile.getSelectionModel().selectFirst();
    comboTypesSingleFile.valueProperty().addListener(observable -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellSingleFile);
    });
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    box.getChildren().addAll(chooseFile, space, typeLabel, comboTypesSingleFile);
    return box;
  }

  private HBox optionsEmptyFile(List<UIPair> metaTypeList) {
    HBox box = new HBox(5);
    box.setAlignment(Pos.CENTER_LEFT);

    Label lab = new Label(I18n.t("name"));
    emptyFileNameTxtField = new TextField("metadata.xml");

    Label typeLabel = new Label(I18n.t("type") + ":");
    emptyFileMetadataTypeTxtField = new TextField(I18n.t("type"));
    Label versionlabel = new Label(I18n.t("version") + ":");
    emptyFileMetadataTypeVersionTxtField = new TextField(I18n.t("version"));

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    box.getChildren().addAll(lab, emptyFileNameTxtField, space, typeLabel, emptyFileMetadataTypeTxtField,versionlabel,emptyFileMetadataTypeVersionTxtField );
    return box;
  }

  private HBox optionsTemplate() {
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);

    templateTypes = new ComboBox<>();
    String templatesRaw = AppProperties.getConfig("metadata.templates");
    String[] templates = templatesRaw.split(",");
    for (String templ : templates) {
      String trimmed = templ.trim();
      
      String title = AppProperties.getConfig("metadata." + trimmed + ".title");
      String type = AppProperties.getConfig("metadata." + trimmed + ".type");
      String version = AppProperties.getConfig("metadata." + trimmed + ".version");
      if (title == null)
        continue;
      String key = trimmed;
      if (type != null) {
        key += "!###!" + type;
      }
      if (version != null) {
        key += "!###!" + version;
      }
      String value = title;
      UIPair newPair = new UIPair(key, value);
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
    btContinue = new Button(I18n.t("continue"));
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
      if (selected == null)
        return;
      if (selected.getUserData() instanceof OPTIONS) {
        OPTIONS option = (OPTIONS) selected.getUserData();
        metadataToAdd = null;
        switch (option) {
          case TEMPLATE:
            String rawTemplateType = (String) templateTypes.getSelectionModel().getSelectedItem().getKey();
            String[] splitted = rawTemplateType.split("!###!");
            String templateType = splitted[0], metadataType = splitted[1], metadataVersion = splitted.length==3?splitted[2]:null;
            metadataToAdd = new DescObjMetadata(MetadataOptions.TEMPLATE, templateType, metadataType, metadataVersion);
            break;
          case SINGLE_FILE:
            if (selectedPath == null)
              return;
            metadataToAdd = new DescObjMetadata(MetadataOptions.SINGLE_FILE, selectedPath, "", "");
            UIPair metaType = comboTypesSingleFile.getSelectionModel().getSelectedItem();
            addTypeAndVersionToMetadata(metaType, metadataToAdd);
            break;
          case EMPTY_FILE:
            String name = emptyFileNameTxtField.getText();
            if (name == null || "".equals(name))
              return;
            metadataToAdd = new DescObjMetadata();
            metadataToAdd.setId(name);
            metadataToAdd.setContentDecoded("");
            if(emptyFileMetadataTypeTxtField.getText()!=null){
              metadataToAdd.setMetadataType(emptyFileMetadataTypeTxtField.getText());
            }
            if(emptyFileMetadataTypeVersionTxtField.getText()!=null){
              metadataToAdd.setMetadataVersion(emptyFileMetadataTypeVersionTxtField.getText());
            }
            metadataToAdd = Utils.updateTemplate(metadataToAdd);
            break;
        }

        if (metadataToAdd == null)
          return;
        boolean add = true;
        if(descriptionObject!=null){
          for (DescObjMetadata dom : descriptionObject.getMetadata()) {
            if (dom.getId().equals(metadataToAdd.getId()))
              add = false;
          }
          if (add) {
            descriptionObject.getMetadata().add(metadataToAdd);
            RodaIn.getInspectionPane().updateMetadataList(descriptionObject);
          } else {
            RodaIn.getInspectionPane().showAddMetadataError(metadataToAdd);
          }
        }
        stage.close();
      }
    });
  }

  private void addTypeAndVersionToMetadata(UIPair metaType, DescObjMetadata metadataToAdd) {
    if (metaType != null) {
      String templateVersion = (String) metaType.getKey();
      String metadataVersion = AppProperties.getConfig("metadata." + templateVersion + ".version");
      String metadataType = AppProperties.getConfig("metadata." + templateVersion + ".type");
      metadataToAdd.setMetadataType(metadataType);
      metadataToAdd.setMetadataVersion(metadataVersion);
      metadataToAdd.setTemplateType(templateVersion);
    }
  }

  private void createCancelButton() {
    btCancel = new Button(I18n.t("cancel"));
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
