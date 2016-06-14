package org.roda.rodain.rules.ui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.UIPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class RuleModalPane extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(RuleModalPane.class.getName());
  private static final int LIST_HEIGHT = 440;

  private enum States {
    ASSOCIATION, METADATA
  }

  private Stage stage;
  private SchemaNode schema;
  private Set<SourceTreeItem> sourceSet;
  // Association
  private VBox boxAssociation;
  private ListView<HBoxCell> assocList;
  /* Metadata */
  private VBox boxMetadata;
  private ListView<HBoxCell> metaList;
  // Templates
  private ComboBox<UIPair> templateTypes;
  // Single file
  private HBoxCell cellSingleFile;
  private Button chooseFile;
  private ComboBox<UIPair> comboTypesSingleFile;
  // Same directory
  private HBoxCell cellSameFolder;
  private TextField sameFolderTxtField;
  private ComboBox<UIPair> comboTypesSameDir;
  // Different directory
  private HBoxCell cellDiffFolder;
  private Button chooseDir;
  private ComboBox<UIPair> comboTypesDiffFolder;

  private Button btContinue, btCancel, btBack;
  private HBox space, buttons;
  private States currentState;
  private String fromFile, diffDir;

  /**
   * Creates a new RuleModalPane, used to create a new Rule.
   *
   * @param stage
   *          The stage of the pane
   * @param sourceSet
   *          The set of selected SourceTreeItems
   * @param schemaNode
   *          The destination SchemaNode, where the SIPs will be created
   */
  public RuleModalPane(Stage stage, Set<SourceTreeItem> sourceSet, SchemaNode schemaNode) {
    super();
    schema = schemaNode;
    this.sourceSet = sourceSet;
    this.stage = stage;
    getStyleClass().add("modal");

    createTop();
    createCenter();
    createBottom();

    currentState = States.ASSOCIATION;
  }

  private void createTop() {
    StackPane pane = new StackPane();
    pane.setPadding(new Insets(0, 0, 10, 0));

    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER_LEFT);
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(15, 15, 15, 15));
    pane.getChildren().add(box);

    Label title = new Label(
      I18n.t("LoadingPane.createAssociation").toUpperCase() + " \"" + schema.getDob().getTitle() + "\"");
    title.setId("title");

    box.getChildren().add(title);

    setTop(pane);
  }

  private void createCenter() {
    createCenterAssociation();
    createCenterMetadata();

    setCenter(boxAssociation);
  }

  private void createCenterAssociation() {
    boxAssociation = new VBox();
    boxAssociation.setPadding(new Insets(0, 15, 0, 15));
    boxAssociation.setAlignment(Pos.TOP_LEFT);

    Label subtitle = new Label(I18n.t("RuleModalPane.associationMethod").toUpperCase());
    subtitle.setPadding(new Insets(0, 0, 10, 0));
    subtitle.setId("sub-title");

    assocList = new ListView<>();
    assocList.setMinHeight(LIST_HEIGHT);
    assocList.setMaxHeight(LIST_HEIGHT);
    assocList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBoxCell>() {
      @Override
      public void changed(ObservableValue<? extends HBoxCell> observable, final HBoxCell oldValue, HBoxCell newValue) {
        if (newValue != null && newValue.isDisabled()) {
          Platform.runLater(() -> {
            if (oldValue != null)
              assocList.getSelectionModel().select(oldValue);
            else
              assocList.getSelectionModel().clearSelection();
          });
        }
      }
    });

    String icon = AppProperties.getStyle("association.sipSelection.icon");
    String title = I18n.t("association.sipSelection.title");
    String description = I18n.t("association.sipSelection.description");
    HBoxCell cellSelected = new HBoxCell("assoc2", icon, title, description, new HBox());
    cellSelected.setUserData(RuleTypes.SIP_PER_SELECTION);

    icon = AppProperties.getStyle("association.singleSip.icon");
    title = I18n.t("association.singleSip.title");
    description = I18n.t("association.singleSip.description");
    HBoxCell cellSingleSip = new HBoxCell("assoc1", icon, title, description, new HBox());
    cellSingleSip.setUserData(RuleTypes.SINGLE_SIP);

    icon = AppProperties.getStyle("association.sipPerFile.icon");
    title = I18n.t("association.sipPerFile.title");
    description = I18n.t("association.sipPerFile.description");
    HBoxCell cellSipPerFile = new HBoxCell("assoc3", icon, title, description, new HBox());
    cellSipPerFile.setUserData(RuleTypes.SIP_PER_FILE);

    icon = AppProperties.getStyle("association.sipWithStructure.icon");
    title = I18n.t("association.sipWithStructure.title");
    description = I18n.t("association.sipWithStructure.description");
    HBoxCell cellStructure = new HBoxCell("assoc4", icon, title, description, new HBox());
    cellStructure.setUserData(RuleTypes.SIP_WITH_STRUCTURE);

    ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
    hboxList.addAll(cellSelected, cellSingleSip, cellSipPerFile, cellStructure);
    assocList.setItems(hboxList);
    assocList.getSelectionModel().selectFirst();

    int fileCount = 0;
    for (SourceTreeItem sti : sourceSet) {
      if (sti instanceof SourceTreeFile) {
        fileCount++;
      }
    }
    if (sourceSet.size() == 1) {
      cellSingleSip.setDisable(true);
      if (fileCount == 1) {
        cellSipPerFile.setDisable(true);
      }
    }
    if (fileCount != 0) {
      cellStructure.setDisable(true);
    }

    boxAssociation.getChildren().addAll(subtitle, assocList);
  }

  private void createCenterMetadata() {
    boxMetadata = new VBox();
    boxMetadata.setAlignment(Pos.TOP_LEFT);
    boxMetadata.setPadding(new Insets(0, 15, 0, 15));

    Label subtitle = new Label(I18n.t("RuleModalPane.metadataMethod").toUpperCase());
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
        String title = AppProperties.getConfig("metadata.type." + type + ".title");
        if (title == null || type == null)
          continue;
        typesList.add(new UIPair(type, title));
      }
    }

    String icon = AppProperties.getStyle("metadata.template.icon");
    String title = I18n.t("metadata.template.title");
    String description = I18n.t("metadata.template.description");
    HBoxCell cellTemplate = new HBoxCell("meta4", icon, title, description, optionsTemplate());
    cellTemplate.setUserData(MetadataOptions.TEMPLATE);

    icon = AppProperties.getStyle("metadata.singleFile.icon");
    title = I18n.t("metadata.singleFile.title");
    description = I18n.t("metadata.singleFile.description");
    cellSingleFile = new HBoxCell("meta1", icon, title, description, optionsSingleFile(typesList));
    cellSingleFile.setUserData(MetadataOptions.SINGLE_FILE);

    icon = AppProperties.getStyle("metadata.sameFolder.icon");
    title = I18n.t("metadata.sameFolder.title");
    description = I18n.t("metadata.sameFolder.description");
    cellSameFolder = new HBoxCell("meta2", icon, title, description, optionsSameFolder(typesList));
    cellSameFolder.setUserData(MetadataOptions.SAME_DIRECTORY);

    icon = AppProperties.getStyle("metadata.diffFolder.icon");
    title = I18n.t("metadata.diffFolder.title");
    description = I18n.t("metadata.diffFolder.description");
    cellDiffFolder = new HBoxCell("meta3", icon, title, description, optionsDiffFolder(typesList));
    cellDiffFolder.setUserData(MetadataOptions.DIFF_DIRECTORY);

    ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
    hboxList.addAll(cellTemplate, cellSingleFile, cellSameFolder, cellDiffFolder);
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
      fromFile = selectedFile.toPath().toString();
      chooseFile.setText(selectedFile.toPath().getFileName().toString());
      chooseFile.setUserData(fromFile);
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

  private HBox optionsSameFolder(List<UIPair> metaTypeList) {
    HBox box = new HBox(5);
    box.setAlignment(Pos.CENTER_LEFT);

    Label lab = new Label(I18n.t("RuleModalPane.metadataPattern"));
    sameFolderTxtField = new TextField("metadata.xml");
    sameFolderTxtField.textProperty().addListener(observable -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellSameFolder);
    });

    Label typeLabel = new Label(I18n.t("type") + ":");
    comboTypesSameDir = new ComboBox<>(FXCollections.observableList(metaTypeList));
    comboTypesSameDir.getSelectionModel().selectFirst();
    comboTypesSameDir.valueProperty().addListener(observable -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellSameFolder);
    });
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    box.getChildren().addAll(lab, sameFolderTxtField, space, typeLabel, comboTypesSameDir);
    return box;
  }

  private HBox optionsDiffFolder(List<UIPair> metaTypeList) {
    HBox box = new HBox(5);
    box.setAlignment(Pos.CENTER_LEFT);

    chooseDir = new Button(I18n.t("RuleModalPane.chooseDirectory"));
    chooseDir.setOnAction(event -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellDiffFolder);
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle(I18n.t("directorychooser.title"));
      File selectedDirectory = chooser.showDialog(stage);
      if (selectedDirectory == null)
        return;
      diffDir = selectedDirectory.toPath().toString();
      chooseDir.setText(selectedDirectory.toPath().getFileName().toString());
      chooseDir.setUserData(diffDir);
    });

    Label typeLabel = new Label(I18n.t("type") + ":");
    comboTypesDiffFolder = new ComboBox<>(FXCollections.observableList(metaTypeList));
    comboTypesDiffFolder.getSelectionModel().selectFirst();
    comboTypesDiffFolder.valueProperty().addListener(observable -> {
      metaList.getSelectionModel().clearSelection();
      metaList.getSelectionModel().select(cellDiffFolder);
    });
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    box.getChildren().addAll(chooseDir, space, typeLabel, comboTypesDiffFolder);
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
      String title = AppProperties.getConfig("metadata.template." + trimmed + ".title");
      String type = AppProperties.getConfig("metadata.template." + trimmed + ".type");
      if (title == null)
        continue;
      String key = trimmed;
      String value = title;
      if (type != null) {
        key += "!###!" + type;
      }
      UIPair newPair = new UIPair(key, value);
      templateTypes.getItems().add(newPair);
    }
    templateTypes.getSelectionModel().selectFirst();

    box.getChildren().add(templateTypes);

    return box;
  }

  private void createBottom() {
    createContinueButton();
    createBackButton();
    createCancelButton();

    space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    VBox bottom = new VBox();
    VBox.setVgrow(bottom, Priority.ALWAYS);
    Separator separator = new Separator();

    buttons = new HBox(10);
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
      if (currentState == States.ASSOCIATION) {
        if (assocList.getSelectionModel().getSelectedIndex() != -1) {
          setCenter(boxMetadata);
          currentState = States.METADATA;
          // enableMetaOptions();
          buttons.getChildren().clear();
          buttons.getChildren().addAll(btCancel, space, btBack, btContinue);
          btContinue.setText(I18n.t("confirm"));
          btContinue.setGraphicTextGap(16);
        }
      } else if (currentState == States.METADATA && metadataCheckContinue()) {
        RuleModalController.confirm();
      }
    });
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

    btCancel.setOnAction(event -> RuleModalController.cancel());
  }

  private void createBackButton() {
    btBack = new Button(I18n.t("back"));
    btBack.setMaxWidth(120);
    btBack.setMinWidth(120);
    Platform.runLater(() -> {
      Image im = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHEVRON_LEFT, Color.WHITE);
      ImageView imv = new ImageView(im);
      btBack.setGraphic(imv);
    });
    btBack.setGraphicTextGap(30);

    btBack.setOnAction(event -> {
      if (currentState == States.ASSOCIATION) {
        setCenter(boxMetadata);
        currentState = States.METADATA;
        // enableMetaOptions();
      } else if (currentState == States.METADATA) {
        setCenter(boxAssociation);
        currentState = States.ASSOCIATION;
        buttons.getChildren().clear();
        buttons.getChildren().addAll(btCancel, space, btContinue);
        btContinue.setText(I18n.t("continue"));
        btContinue.setGraphicTextGap(10);
      }
    });
  }

  private boolean metadataCheckContinue() {
    try {
      MetadataOptions metaType = getMetadataOption();
      if (metaType == null)
        return false;
      if (metaType == MetadataOptions.SINGLE_FILE)
        return chooseFile.getUserData() != null;
      if (metaType == MetadataOptions.DIFF_DIRECTORY)
        return chooseDir.getUserData() != null;
    } catch (Exception e) {
      LOGGER.error("Error getting metadata type", e);
    }
    return true;
  }

  /**
   * @return The association type of the item the user selected or null if there
   *         was no selection.
   * @throws UnexpectedDataTypeException
   */
  public RuleTypes getAssociationType() throws UnexpectedDataTypeException {
    HBoxCell cell = assocList.getSelectionModel().getSelectedItem();
    if (cell == null)
      return null;
    if (cell.getUserData() instanceof RuleTypes)
      return (RuleTypes) cell.getUserData();
    else
      throw new UnexpectedDataTypeException();
  }

  /**
   * @return The metadata type of the item the user selected or null if there
   *         was no selection.
   * @throws UnexpectedDataTypeException
   */
  public MetadataOptions getMetadataOption() throws UnexpectedDataTypeException {
    HBoxCell selected = metaList.getSelectionModel().getSelectedItem();
    if (selected == null)
      return null;
    if (selected.getUserData() instanceof MetadataOptions)
      return (MetadataOptions) selected.getUserData();
    else
      throw new UnexpectedDataTypeException();
  }

  /**
   * @return The path of the file selected by the user in the metadata option
   *         SINGLE_FILE
   */
  public Path getFromFile() {
    return Paths.get(fromFile);
  }

  /**
   * @return The path of the directory selected by the user in the metadata
   *         option DIFF_DIRECTORY
   */
  public Path getDiffDir() {
    return Paths.get(diffDir);
  }

  /**
   * @return The text pattern the user input
   */
  public String getSameFolderPattern() {
    return sameFolderTxtField.getText();
  }

  /**
   * @return The template from the metadata option TEMPLATE
   */
  public String getTemplate() {
    UIPair selected = templateTypes.getSelectionModel().getSelectedItem();
    if (selected == null) {
      btContinue.setDisable(true);
    }
    return (String) selected.getKey();
  }

  public String getMetadataTypeSingleFile() {
    String result = null;
    UIPair selected = comboTypesSingleFile.getSelectionModel().getSelectedItem();
    if (selected != null) {
      result = (String) selected.getKey();
    }
    return result;
  }

  public String getMetadataTypeSameFolder() {
    String result = null;
    UIPair selected = comboTypesSameDir.getSelectionModel().getSelectedItem();
    if (selected != null) {
      result = (String) selected.getKey();
    }
    return result;
  }

  public String getMetadataTypeDiffFolder() {
    String result = null;
    UIPair selected = comboTypesDiffFolder.getSelectionModel().getSelectedItem();
    if (selected != null) {
      result = (String) selected.getKey();
    }
    return result;
  }
}
