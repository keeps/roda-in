package org.roda.rodain.rules.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.UIPair;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class RuleModalPane extends BorderPane {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(RuleModalPane.class.getName());
  private final int LIST_HEIGHT = 440;

  private enum States {
    ASSOCIATION, METADATA
  }

  private Stage stage;
  private SchemaNode schema;
  private Set<SourceTreeItem> sourceSet;
  // Association
  private VBox boxAssociation;
  private ListView<HBoxCell> assocList;
  private ComboBox<Integer> level;
  // Metadata
  private VBox boxMetadata;
  private ListView<HBoxCell> metaList;
  private HBoxCell cellSingleFile;
  private HBoxCell cellSameFolder;
  private HBoxCell cellDiffFolder;
  private Button chooseDir, chooseFile;
  private ComboBox<UIPair> templateTypes;

  private Button btContinue, btCancel, btBack;
  private HBox space, buttons;
  private States currentState;
  private String fromFile, diffDir;
  private TextField sameFolderTxtField;

  private int folderCount;

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
    box.setPadding(new Insets(10, 10, 10, 10));
    pane.getChildren().add(box);

    Label title = new Label(
      AppProperties.getLocalizedString("LoadingPane.createAssociation") + " \"" + schema.getDob().getTitle() + "\"");
    title.setId("title");

    ArrayList<String> dirs = new ArrayList<>();
    for (SourceTreeItem it : sourceSet) {
      if (it instanceof SourceTreeDirectory)
        dirs.add(it.getValue());
    }
    folderCount = dirs.size();

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
    boxAssociation.setPadding(new Insets(0, 10, 0, 10));
    boxAssociation.setAlignment(Pos.TOP_LEFT);

    Label subtitle = new Label(AppProperties.getLocalizedString("RuleModalPane.associationMethod"));
    subtitle.setPadding(new Insets(0, 0, 10, 0));
    subtitle.setId("sub-title");

    assocList = new ListView<>();
    assocList.setMinHeight(LIST_HEIGHT);
    assocList.setMaxHeight(LIST_HEIGHT);
    assocList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBoxCell>() {
      @Override
      public void changed(ObservableValue<? extends HBoxCell> observable, final HBoxCell oldValue, HBoxCell newValue) {
        if (newValue != null && newValue.isDisabled()) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              if (oldValue != null)
                assocList.getSelectionModel().select(oldValue);
              else
                assocList.getSelectionModel().clearSelection();
            }
          });
        }
      }
    });

    String icon = AppProperties.getStyle("association.sipSelection.icon");
    String title = AppProperties.getLocalizedString("association.sipSelection.title");
    String description = AppProperties.getLocalizedString("association.sipSelection.description");
    HBoxCell cellSelected = new HBoxCell("assoc2", icon, title, description, new HBox());
    cellSelected.setUserData(RuleTypes.SIP_PER_SELECTION);

    icon = AppProperties.getStyle("association.singleSip.icon");
    title = AppProperties.getLocalizedString("association.singleSip.title");
    description = AppProperties.getLocalizedString("association.singleSip.description");
    HBoxCell cellSingleSip = new HBoxCell("assoc1", icon, title, description, new HBox());
    cellSingleSip.setUserData(RuleTypes.SINGLE_SIP);

    icon = AppProperties.getStyle("association.sipPerFile.icon");
    title = AppProperties.getLocalizedString("association.sipPerFile.title");
    description = AppProperties.getLocalizedString("association.sipPerFile.description");
    HBoxCell cellSipPerFile = new HBoxCell("assoc3", icon, title, description, new HBox());
    cellSipPerFile.setUserData(RuleTypes.SIP_PER_FILE);

    icon = AppProperties.getStyle("association.sipWithStructure.icon");
    title = AppProperties.getLocalizedString("association.sipWithStructure.title");
    description = AppProperties.getLocalizedString("association.sipWithStructure.description");
    HBoxCell cellStructure = new HBoxCell("assoc4", icon, title, description, new HBox());
    cellStructure.setUserData(RuleTypes.SIP_WITH_STRUCTURE);

    ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
    hboxList.addAll(cellSelected, cellSingleSip, cellSipPerFile, cellStructure);
    assocList.setItems(hboxList);
    assocList.getSelectionModel().selectFirst();

    if (sourceSet.size() == 1) {
      cellSingleSip.setDisable(true);
    }

    boxAssociation.getChildren().addAll(subtitle, assocList);
  }

  private void createCenterMetadata() {
    boxMetadata = new VBox();
    boxMetadata.setAlignment(Pos.TOP_LEFT);
    boxMetadata.setPadding(new Insets(0, 10, 0, 10));

    Label subtitle = new Label(AppProperties.getLocalizedString("RuleModalPane.metadataMethod"));
    subtitle.setId("sub-title");
    subtitle.setPadding(new Insets(0, 0, 10, 0));

    metaList = new ListView<>();
    metaList.setMinHeight(LIST_HEIGHT);
    metaList.setMaxHeight(LIST_HEIGHT);
    metaList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBoxCell>() {
      @Override
      public void changed(ObservableValue<? extends HBoxCell> observable, final HBoxCell oldValue, HBoxCell newValue) {
        if (newValue != null && newValue.isDisabled()) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              if (oldValue != null)
                metaList.getSelectionModel().select(oldValue);
              else
                metaList.getSelectionModel().clearSelection();
            }
          });
        }
      }
    });

    String icon = AppProperties.getStyle("metadata.template.icon");
    String title = AppProperties.getLocalizedString("metadata.template.title");
    String description = AppProperties.getLocalizedString("metadata.template.description");
    HBoxCell cellTemplate = new HBoxCell("meta4", icon, title, description, optionsTemplate());
    cellTemplate.setUserData(MetadataTypes.TEMPLATE);

    icon = AppProperties.getStyle("metadata.singleFile.icon");
    title = AppProperties.getLocalizedString("metadata.singleFile.title");
    description = AppProperties.getLocalizedString("metadata.singleFile.description");
    cellSingleFile = new HBoxCell("meta1", icon, title, description, optionsSingleFile());
    cellSingleFile.setUserData(MetadataTypes.SINGLE_FILE);

    icon = AppProperties.getStyle("metadata.sameFolder.icon");
    title = AppProperties.getLocalizedString("metadata.sameFolder.title");
    description = AppProperties.getLocalizedString("metadata.sameFolder.description");
    cellSameFolder = new HBoxCell("meta2", icon, title, description, optionsSameFolder());
    cellSameFolder.setUserData(MetadataTypes.SAME_DIRECTORY);

    icon = AppProperties.getStyle("metadata.diffFolder.icon");
    title = AppProperties.getLocalizedString("metadata.diffFolder.title");
    description = AppProperties.getLocalizedString("metadata.diffFolder.description");
    cellDiffFolder = new HBoxCell("meta3", icon, title, description, optionsDiffFolder());
    cellDiffFolder.setUserData(MetadataTypes.DIFF_DIRECTORY);

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

  private HBox optionsSingleFile() {
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);

    chooseFile = new Button(AppProperties.getLocalizedString("RuleModalPane.chooseFile"));
    chooseFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        metaList.getSelectionModel().clearSelection();
        metaList.getSelectionModel().select(cellSingleFile);
        FileChooser chooser = new FileChooser();
        chooser.setTitle(AppProperties.getLocalizedString("filechooser.title"));
        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile == null)
          return;
        fromFile = selectedFile.toPath().toString();
        chooseFile.setText(selectedFile.toPath().getFileName().toString());
        chooseFile.setUserData(fromFile);
      }
    });

    box.getChildren().add(chooseFile);
    return box;
  }

  private HBox optionsSameFolder() {
    HBox box = new HBox(5);
    box.setAlignment(Pos.CENTER_LEFT);

    Label lab = new Label(AppProperties.getLocalizedString("RuleModalPane.metadataPattern"));
    sameFolderTxtField = new TextField("metadata.xml");

    box.getChildren().addAll(lab, sameFolderTxtField);
    return box;
  }

  private HBox optionsDiffFolder() {
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);

    chooseDir = new Button(AppProperties.getLocalizedString("RuleModalPane.chooseDirectory"));
    chooseDir.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        metaList.getSelectionModel().clearSelection();
        metaList.getSelectionModel().select(cellDiffFolder);
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(AppProperties.getLocalizedString("directorychooser.title"));
        File selectedDirectory = chooser.showDialog(stage);
        if (selectedDirectory == null)
          return;
        diffDir = selectedDirectory.toPath().toString();
        chooseDir.setText(selectedDirectory.toPath().getFileName().toString());
        chooseDir.setUserData(diffDir);
      }
    });

    box.getChildren().addAll(chooseDir);
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
      String version = AppProperties.getConfig("metadata.template." + trimmed + ".version");
      if (title == null)
        continue;
      String key = trimmed;
      String value = title;
      if (version != null) {
        value += " (" + version + ")";
        key += "!###!" + version;
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
    btContinue = new Button(AppProperties.getLocalizedString("continue"));
    btContinue.setId("btConfirm");
    btContinue.setMaxWidth(120);
    btContinue.setMinWidth(120);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Image im = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.chevron_right, Color.WHITE);
        ImageView imv = new ImageView(im);
        btContinue.setGraphic(imv);
      }
    });
    btContinue.setGraphicTextGap(10);
    btContinue.setContentDisplay(ContentDisplay.RIGHT);

    btContinue.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        if (currentState == States.ASSOCIATION) {
          if (assocList.getSelectionModel().getSelectedIndex() != -1) {
            setCenter(boxMetadata);
            currentState = States.METADATA;
            enableMetaOptions();
            buttons.getChildren().clear();
            buttons.getChildren().addAll(btCancel, space, btBack, btContinue);
            btContinue.setText(AppProperties.getLocalizedString("confirm"));
            btContinue.setGraphicTextGap(16);
          }
        } else if (currentState == States.METADATA && metadataCheckContinue()) {
          RuleModalController.confirm();
        }
      }
    });
  }

  private void createCancelButton() {
    btCancel = new Button(AppProperties.getLocalizedString("cancel"));
    btCancel.setMaxWidth(120);
    btCancel.setMinWidth(120);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Image im = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.times, Color.WHITE);
        ImageView imv = new ImageView(im);
        btCancel.setGraphic(imv);
      }
    });
    btCancel.setGraphicTextGap(20);
    btCancel.setContentDisplay(ContentDisplay.RIGHT);

    btCancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        RuleModalController.cancel();
      }
    });
  }

  private void createBackButton() {
    btBack = new Button(AppProperties.getLocalizedString("back"));
    btBack.setMaxWidth(120);
    btBack.setMinWidth(120);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Image im = FontAwesomeImageCreator.generate(FontAwesomeImageCreator.chevron_left, Color.WHITE);
        ImageView imv = new ImageView(im);
        btBack.setGraphic(imv);
      }
    });
    btBack.setGraphicTextGap(30);

    btBack.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        if (currentState == States.ASSOCIATION) {
          setCenter(boxMetadata);
          currentState = States.METADATA;
          enableMetaOptions();
        } else if (currentState == States.METADATA) {
          setCenter(boxAssociation);
          currentState = States.ASSOCIATION;
          buttons.getChildren().clear();
          buttons.getChildren().addAll(btCancel, space, btContinue);
          btContinue.setText(AppProperties.getLocalizedString("continue"));
          btContinue.setGraphicTextGap(10);
        }
      }
    });
  }

  private boolean metadataCheckContinue() {
    try {
      MetadataTypes metaType = getMetadataType();
      if (metaType == null)
        return false;
      if (metaType == MetadataTypes.SINGLE_FILE)
        return chooseFile.getUserData() != null;
      if (metaType == MetadataTypes.DIFF_DIRECTORY)
        return chooseDir.getUserData() != null;
    } catch (Exception e) {
      log.error("Error getting metadata type", e);
    }
    return true;
  }

  private void enableMetaOptions() {
    try {
      RuleTypes assocType = getAssociationType();
      switch (assocType) {
        case SIP_PER_FILE:
          cellSameFolder.setDisable(false);
          cellDiffFolder.setDisable(false);
          chooseDir.setDisable(false);
          break;
        case SIP_PER_SELECTION:
          cellSameFolder.setDisable(false);
          cellDiffFolder.setDisable(true);
          chooseDir.setDisable(true);
          break;
        default:
          cellSameFolder.setDisable(true);
          cellDiffFolder.setDisable(true);
          chooseDir.setDisable(true);
          break;
      }
    } catch (Exception e) {
      log.error("Error getting association type", e);
    }
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
   * @return The value of the combo box of the SipPerFolder association option.
   */
  public int getLevel() {
    return level.getValue();
  }

  /**
   * @return The metadata type of the item the user selected or null if there
   *         was no selection.
   * @throws UnexpectedDataTypeException
   */
  public MetadataTypes getMetadataType() throws UnexpectedDataTypeException {
    HBoxCell selected = metaList.getSelectionModel().getSelectedItem();
    if (selected == null)
      return null;
    if (selected.getUserData() instanceof MetadataTypes)
      return (MetadataTypes) selected.getUserData();
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
}
