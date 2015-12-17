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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.rules.sip.TemplateType;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

  private static Properties properties;

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
  private ComboBox<String> templateTypes;

  private Button btContinue, btCancel, btBack;
  private HBox space, buttons;
  private States currentState;
  private String fromFile, diffDir, sameDir;

  private int folderCount;


  /**
   * Creates a new RuleModalPane, used to create a new Rule.
   *
   * @param stage      The stage of the pane
   * @param sourceSet  The set of selected SourceTreeItems
   * @param schemaNode The destination SchemaNode, where the SIPs will be created
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

  /**
   * Sets the properties object for this class.
   *
   * @param prop The properties object.
   */
  public static void setProperties(Properties prop) {
    properties = prop;
  }

  private void createTop() {
    StackPane pane = new StackPane();
    pane.setPadding(new Insets(0, 0, 10, 0));

    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER_LEFT);
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(10, 10, 10, 10));
    pane.getChildren().add(box);

    Label title = new Label("Create association to \"" + schema.getDob().getTitle() + "\"");
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

    Label subtitle = new Label("Choose the association method");
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

    String icon = properties.getProperty("association.singleSip.icon");
    String title = properties.getProperty("association.singleSip.title");
    String description = properties.getProperty("association.singleSip.description");
    HBoxCell cellSingleSip = new HBoxCell("assoc1", icon, title, description, new HBox());
    cellSingleSip.setUserData(RuleTypes.SINGLE_SIP);

    icon = properties.getProperty("association.sipSelection.icon");
    title = properties.getProperty("association.sipSelection.title");
    description = properties.getProperty("association.sipSelection.description");
    HBoxCell cellSelected = new HBoxCell("assoc2", icon, title, description, new HBox());
    cellSelected.setUserData(RuleTypes.SIP_PER_SELECTION);

    icon = properties.getProperty("association.sipPerFile.icon");
    title = properties.getProperty("association.sipPerFile.title");
    description = properties.getProperty("association.sipPerFile.description");
    HBoxCell cellSipPerFile = new HBoxCell("assoc3", icon, title, description, new HBox());
    cellSipPerFile.setUserData(RuleTypes.SIP_PER_FILE);

    icon = properties.getProperty("association.sipPerFolder.icon");
    title = properties.getProperty("association.sipPerFolder.title");
    description = properties.getProperty("association.sipPerFolder.description");
    HBox options = createPerFolderOptions();
    HBoxCell cellSipPerFolder = new HBoxCell("assoc4", icon, title, description, options);
    cellSipPerFolder.setUserData(RuleTypes.SIP_PER_FOLDER);

    ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
    hboxList.addAll(cellSingleSip, cellSelected, cellSipPerFile, cellSipPerFolder);
    assocList.setItems(hboxList);

    if (folderCount == 0 || level.getItems().isEmpty()) {
      cellSipPerFolder.setDisable(true);
    }

    boxAssociation.getChildren().addAll(subtitle, assocList);
  }

  private HBox createPerFolderOptions() {
    HBox resultBox = new HBox(10);
    resultBox.setAlignment(Pos.CENTER_LEFT);

    int depth = 0;
    if (folderCount == sourceSet.size()) { // check if the selected items are
      // all directories
      // we only need to compute the depth if we'll be going to activate the
      // radio button
      for (SourceTreeItem std : sourceSet) {
        Path startPath = Paths.get(std.getPath());
        int depthAux = Utils.getRelativeMaxDepth(startPath);
        if (depthAux > depth)
          depth = depthAux;
      }
    }

    ArrayList<Integer> levels = new ArrayList<>();
    for (int i = 1; i <= depth; i++)
      levels.add(i);

    ObservableList<Integer> options = FXCollections.observableArrayList(levels);
    level = new ComboBox<>(options);
    level.setValue((int) Math.ceil(depth / 2.0));

    Label lLevel = new Label("Max depth");
    resultBox.getChildren().addAll(lLevel, level);

    return resultBox;
  }

  private void createCenterMetadata() {
    boxMetadata = new VBox();
    boxMetadata.setAlignment(Pos.TOP_LEFT);
    boxMetadata.setPadding(new Insets(0, 10, 0, 10));

    Label subtitle = new Label("Choose the metadata method");
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

    String icon = properties.getProperty("metadata.singleFile.icon");
    String title = properties.getProperty("metadata.singleFile.title");
    String description = properties.getProperty("metadata.singleFile.description");
    cellSingleFile = new HBoxCell("meta1", icon, title, description, optionsSingleFile());
    cellSingleFile.setUserData(MetadataTypes.SINGLE_FILE);

    icon = properties.getProperty("metadata.sameFolder.icon");
    String tempTitle = properties.getProperty("metadata.sameFolder.title");
    title = String.format("%s \"%s\"", tempTitle, pathSameFolder());
    description = properties.getProperty("metadata.sameFolder.description");
    cellSameFolder = new HBoxCell("meta2", icon, title, description, new HBox());
    cellSameFolder.setUserData(MetadataTypes.SAME_DIRECTORY);

    icon = properties.getProperty("metadata.diffFolder.icon");
    title = properties.getProperty("metadata.diffFolder.title");
    description = properties.getProperty("metadata.diffFolder.description");
    cellDiffFolder = new HBoxCell("meta3", icon, title, description, optionsDiffFolder());
    cellDiffFolder.setUserData(MetadataTypes.DIFF_DIRECTORY);

    icon = properties.getProperty("metadata.template.icon");
    title = properties.getProperty("metadata.template.title");
    description = properties.getProperty("metadata.template.description");
    HBoxCell cellTemplate = new HBoxCell("meta4", icon, title, description, optionsTemplate());
    cellTemplate.setUserData(MetadataTypes.TEMPLATE);

    ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
    hboxList.addAll(cellSingleFile, cellSameFolder, cellDiffFolder, cellTemplate);
    metaList.setItems(hboxList);

    boxMetadata.getChildren().addAll(subtitle, metaList);
  }

  private HBox optionsSingleFile() {
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);

    chooseFile = new Button("Choose File...");
    chooseFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        metaList.getSelectionModel().clearSelection();
        metaList.getSelectionModel().select(cellSingleFile);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Please choose a file");
        chooser.setInitialDirectory(new File(sameDir));
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

  private String pathSameFolder() {
    // fill list with the directories in the source set
    List<String> directories = new ArrayList<>();
    for (SourceTreeItem sti : sourceSet) {
      if (sti instanceof SourceTreeDirectory)
        directories.add(sti.getPath());
      else { // if the item isn't a directory, get its parent
        Path path = Paths.get(sti.getPath());
        directories.add(path.getParent().toString());
      }
    }
    // get the common prefix of the directories
    sameDir = Utils.longestCommonPrefix(directories);
    return sameDir;
  }

  private HBox optionsDiffFolder() {
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);

    chooseDir = new Button("Choose Directory...");
    chooseDir.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        metaList.getSelectionModel().clearSelection();
        metaList.getSelectionModel().select(cellDiffFolder);
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Please choose a folder");
        chooser.setInitialDirectory(new File(sameDir));
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
    templateTypes.getItems().addAll("Dublin Core", "EAD");
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
    btContinue = new Button("Continue");
    btContinue.setMaxWidth(100);
    btContinue.setMinWidth(100);
    btContinue.setGraphic(new ImageView(FontAwesomeImageCreator.im_w_chevron_right));
    btContinue.setGraphicTextGap(10);
    btContinue.setContentDisplay(ContentDisplay.RIGHT);

    btContinue.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        if (currentState == States.ASSOCIATION) {
          if (assocList.getSelectionModel().getSelectedIndex() != -1) {
            setCenter(boxMetadata);
            currentState = States.METADATA;
            enableMetaRadioButtons();
            buttons.getChildren().clear();
            buttons.getChildren().addAll(btCancel, space, btBack, btContinue);
            btContinue.setText("Confirm");
            btContinue.setGraphicTextGap(16);
          }
        } else if (currentState == States.METADATA && metadataCheckContinue())
          RuleModalController.confirm();
      }
    });
  }

  private void createCancelButton() {
    btCancel = new Button("Cancel");
    btCancel.setMaxWidth(100);
    btCancel.setMinWidth(100);
    btCancel.setGraphic(new ImageView(FontAwesomeImageCreator.im_w_times));
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
    btBack = new Button("Back");
    btBack.setMaxWidth(100);
    btBack.setMinWidth(100);
    btBack.setGraphic(new ImageView(FontAwesomeImageCreator.im_w_chevron_left));
    btBack.setGraphicTextGap(30);

    btBack.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        if (currentState == States.ASSOCIATION) {
          setCenter(boxMetadata);
          currentState = States.METADATA;
          enableMetaRadioButtons();
        } else if (currentState == States.METADATA) {
          setCenter(boxAssociation);
          currentState = States.ASSOCIATION;
          buttons.getChildren().clear();
          buttons.getChildren().addAll(btCancel, space, btContinue);
          btContinue.setText("Continue");
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

  private void enableMetaRadioButtons() {
    try {
      RuleTypes assocType = getAssociationType();
      if (assocType == null)
        return;
      if (assocType == RuleTypes.SIP_PER_FILE) {
        cellSameFolder.setDisable(false);
        cellDiffFolder.setDisable(false);
        chooseDir.setDisable(false);
      } else {
        cellSameFolder.setDisable(true);
        cellDiffFolder.setDisable(true);
        chooseDir.setDisable(true);
      }
    } catch (Exception e) {
      log.error("Error getting association type", e);
    }
  }

  /**
   * @return The association type of the item the user selected or null if there
   * was no selection.
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
   * was no selection.
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
   * SINGLE_FILE
   */
  public Path getFromFile() {
    return Paths.get(fromFile);
  }

  /**
   * @return The path of the directory selected by the user in the metadata
   * option DIFF_DIRECTORY
   */
  public Path getDiffDir() {
    return Paths.get(diffDir);
  }

  /**
   * @return The path of the directory that is an ancestor to all the selected
   * files
   */
  public Path getSameDir() {
    return Paths.get(sameDir);
  }

  /**
   * @return The template from the metadata option TEMPLATE
   */
  public TemplateType getTemplate() {
    String selected = templateTypes.getSelectionModel().getSelectedItem();
    TemplateType result;
    if (selected.startsWith("Dublin")) {
      result = TemplateType.DUBLIN_CORE;
    } else
      result = TemplateType.EAD;

    return result;
  }
}
