package org.roda.rodain.ui.creation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.ToggleSwitch;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Constants.SipType;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.core.sip.naming.SIPNameBuilderBagit;
import org.roda.rodain.core.sip.naming.SIPNameBuilderEARK;
import org.roda.rodain.core.sip.naming.SIPNameBuilderEARK2;
import org.roda.rodain.core.sip.naming.SIPNameBuilderHungarian;
import org.roda.rodain.core.sip.naming.SIPNameBuilderSIPS;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.creation.METSHeaderComponents.METSHeaderUtils;
import org.roda_project.commons_ip.model.IPHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreationModalPreparation extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreationModalPreparation.class.getName());
  private static final List<Pair> SIP_TYPES = new ArrayList<>();
  private static final ObservableList<Pair> SIP_NAME_STRATEGY_COMBO_BOX_ITEMS = FXCollections.observableArrayList();
  private static Path outputFolder = null;
  private static Button start;
  private static boolean isDisableOutputFolder = true;
  private static boolean isDisableAgentName = true;
  private static boolean isDisableAgentID = true;
  private static boolean isDisableStart = true;
  private static HBox sipAgentNameField;
  private static HBox sipAgentNoteField;

  static {
    for (SipType type : SipType.getFilteredValues()) {
      SIP_TYPES.add(new Pair(type, type.toString()));
    }
  }

  static {
    for (SipNameStrategy sipNameStrategy : SipNameStrategy.values()) {
      SIP_NAME_STRATEGY_COMBO_BOX_ITEMS.add(new Pair(sipNameStrategy, I18n.t("sipNameStrategy." + sipNameStrategy)));
    }
  }

  private final int DEFAULT_WIDTH = 120;
  private CreationModalStage stage;
  private ComboBox<Pair> sipTypes;
  private SIPNameStrategyComboBox sipNameStrategyComboBox;
  private long selectedSIP, selectedItems, allSIP, allItems;
  private ToggleSwitch sipExportSwitch, itemExportSwitch, reportCreationSwitch;
  private TextField sipNameStrategyPrefix;
  private TextField sipNameStrategyTransferring;
  private TextField sipNameStrategySerial;
  private TextField sipAgentName;
  private TextField sipAgentNote;
  private String sSelectedSIP, sSelectedItems, sZeroItems, sAllSIP, sAllItems;

  /**
   * Creates a modal to prepare for the SIP exportation.
   * <p/>
   * <p>
   * This class creates a pane with a field to choose what the output directory
   * for the SIP exportation should be and the format of the SIPs.
   * </p>
   *
   * @param stage
   *          The stage of the pane
   */
  public CreationModalPreparation(CreationModalStage stage) {
    this.stage = stage;

    getStyleClass().add(Constants.CSS_SIPCREATOR);

    createTop();
    createCenter();
    createBottom();

    stage.sizeToScene();

  }

  /**
   * Sets the output folder of the SIPs. Used for testing.
   *
   * @param out
   *          The path of the output folder
   */
  public static void setOutputFolder(String out) {
    if (out != null && !"".equals(out)) {
      outputFolder = Paths.get(out);
      setIsDisableOutputFolder(false);
      setIsDisableStart();
    }
  }

  public static void setIsDisableOutputFolder(boolean isDisable) {
    isDisableOutputFolder = isDisable;
    setIsDisableStart();
  }

  public static void setIsDisableAgentName(boolean isDisable) {
    isDisableAgentName = isDisable;
    setIsDisableStart();
  }

  public static void setIsDisableAgentID(boolean isDisable) {
    isDisableAgentID = isDisable;
    setIsDisableStart();
  }

  public static void setIsDisableStart() {
    if (start != null) {
      if (sipAgentNameField.isVisible() && sipAgentNoteField.isVisible()) {
        start.setDisable(isDisableOutputFolder || isDisableAgentName || isDisableAgentID);
      } else {
        start.setDisable(isDisableOutputFolder);
      }
    }
  }

  private static Pair getLastOrDefaultSipType() {
    String lastSipType = ConfigurationManager.getAppConfig(Constants.CONF_K_LAST_SIP_TYPE);
    for (Pair sipType : SIP_TYPES) {
      String name = (String) sipType.getValue();
      if (name.equals(lastSipType)) {
        return sipType;
      }
    }

    String defaultSipType = ConfigurationManager.getConfig(Constants.CONF_K_DEFAULT_SIP_TYPE);
    for (Pair sipType : SIP_TYPES) {
      String name = (String) sipType.getValue();
      if (name.equals(defaultSipType)) {
        return sipType;
      }
    }
    return null;
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.setAlignment(Pos.CENTER);

    Label title = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_CREATING_SIPS));
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox(5);
    center.setAlignment(Pos.CENTER_LEFT);
    center.setPadding(new Insets(10, 10, 10, 10));
    VBox countBox = createCountBox();
    VBox reportBox = createReportBox();
    HBox outputFolderBox = createOutputFolder();
    HBox sipTypesBox = createSipTypes();
    sipAgentNameField = createAgentName();
    sipAgentNoteField = createAgentNote();
    HBox prefixBox = createSipNameStrategyBoxAndDropdown();

    sipTypes.getSelectionModel().clearSelection();
    Pair lastOrDefaultSipType = getLastOrDefaultSipType();
    if (lastOrDefaultSipType != null) {
      sipTypes.getSelectionModel().select(lastOrDefaultSipType);
    } else {
      sipTypes.getSelectionModel().select(1);
    }
    SipType type = (SipType) sipTypes.getSelectionModel().getSelectedItem().getKey();
    center.getChildren().addAll(countBox, reportBox, outputFolderBox, sipTypesBox, sipAgentNameField, sipAgentNoteField,
      prefixBox);
    if (!SipType.EARK2.equals(type) && !SipType.SIPS.equals(type)) {
      sipAgentNameField.setVisible(false);
      sipAgentNoteField.setVisible(false);
    }
    setCenter(center);
  }

  private VBox createCountBox() {
    VBox countBox = new VBox(10);
    countBox.setAlignment(Pos.CENTER);
    Set<Sip> selectedSet = RodaInApplication.getSelectedDescriptionObjects().keySet();
    Set<Sip> allSet = RodaInApplication.getAllDescriptionObjects().keySet();
    selectedSIP = selectedSet.stream().filter(p -> p instanceof SipPreview).count();
    selectedItems = selectedSet.size() - selectedSIP;
    allSIP = allSet.stream().filter(p -> p instanceof SipPreview).count();
    allItems = allSet.size() - allSIP;

    sSelectedSIP = String.format("%s %d/%d SIP", I18n.t(Constants.I18N_SELECTED), this.selectedSIP, this.allSIP);
    sSelectedItems = String.format("%d/%d %s", this.selectedItems, this.allItems, I18n.t(Constants.I18N_ITEMS));
    sZeroItems = String.format("%d/%d %s", 0, this.allItems, I18n.t(Constants.I18N_ITEMS));
    sAllSIP = String.format("%s %d/%d SIP", I18n.t(Constants.I18N_SELECTED), this.allSIP, this.allSIP);
    sAllItems = String.format("%d/%d %s", this.allItems, this.allItems, I18n.t(Constants.I18N_ITEMS));

    String startingLabel = sSelectedSIP;
    if (allItems != 0) {
      startingLabel = String.format("%s %s %s", sSelectedSIP, I18n.t(Constants.I18N_AND), sZeroItems);
    }

    Label countLabel = new Label(startingLabel);
    countLabel.getStyleClass().add(Constants.CSS_PREPARECREATIONSUBTITLE);
    sipExportSwitch = new ToggleSwitch(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_EXPORT_ALL));
    sipExportSwitch.selectedProperty().addListener((o, old, newValue) -> setSelectedLabel(countLabel));

    if (this.selectedSIP == 0 || this.selectedSIP == this.allSIP)
      sipExportSwitch.setSelected(true);

    itemExportSwitch = new ToggleSwitch(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_INCLUDE_HIERARCHY));
    itemExportSwitch.selectedProperty().addListener((o, old, newValue) -> setSelectedLabel(countLabel));

    String savedState = ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_ITEM_EXPORT_SWITCH);
    if (StringUtils.isNotBlank(savedState)) {
      itemExportSwitch.setSelected(Boolean.valueOf(savedState));
    }
    VBox switchBox = new VBox(10);
    switchBox.setAlignment(Pos.CENTER_LEFT);
    switchBox.getChildren().addAll(sipExportSwitch, itemExportSwitch);
    countBox.getChildren().addAll(countLabel, switchBox);
    return countBox;
  }

  private VBox createReportBox() {
    VBox reportBox = new VBox(10);
    reportBox.setAlignment(Pos.CENTER_LEFT);
    reportCreationSwitch = new ToggleSwitch(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_CREATE_REPORT));
    reportCreationSwitch.setSelected(true);

    String savedState = ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_REPORT_CREATION_SWITCH);
    if (StringUtils.isNotBlank(savedState)) {
      reportCreationSwitch.setSelected(Boolean.valueOf(savedState));
    }

    reportBox.getChildren().addAll(reportCreationSwitch);
    return reportBox;

  }

  private void setSelectedLabel(Label label) {
    boolean sipAll = false, exportItems = false;
    String newLabel, format = "%s %s %s";
    if (sipExportSwitch != null) {
      sipAll = sipExportSwitch.isSelected();
    }
    if (itemExportSwitch != null) {
      exportItems = itemExportSwitch.isSelected();
    }
    if (sipAll) {
      newLabel = sAllSIP;
      if (allItems != 0) {
        if (exportItems) {
          newLabel = String.format(format, sAllSIP, I18n.t(Constants.I18N_AND), sAllItems);
        } else {
          newLabel = String.format(format, sAllSIP, I18n.t(Constants.I18N_AND), sZeroItems);
        }
      }
    } else {
      newLabel = sSelectedSIP;
      if (allItems != 0) {
        if (exportItems) {
          newLabel = String.format(format, sSelectedSIP, I18n.t(Constants.I18N_AND), sSelectedItems);
        } else {
          newLabel = String.format(format, sSelectedSIP, I18n.t(Constants.I18N_AND), sZeroItems);
        }
      }
    }
    label.setText(newLabel);
  }

  private HBox createOutputFolder() {
    HBox outputFolderBox = new HBox(5);
    outputFolderBox.setAlignment(Pos.CENTER_LEFT);

    Label outputFolderLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_OUTPUT_DIRECTORY));
    Button chooseFile = new Button(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_CHOOSE));
    chooseFile.setMnemonicParsing(false);
    chooseFile.setMinWidth(DEFAULT_WIDTH);
    chooseFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(I18n.t(Constants.I18N_DIRECTORY_CHOOSER_TITLE));
        File selectedFile = chooser.showDialog(stage);
        setOutputFolder(selectedFile, chooseFile);
      }
    });

    String savedOutputFolder = ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_SIP_OUTPUT_FOLDER);
    if (StringUtils.isNotBlank(savedOutputFolder)) {
      // 20170411 bferreira: using File instead of NIO because in the action
      // handling 'File' is used and Path::exists is (according to a Java bug
      // report) performing poorly when the file/folder does not exist.
      File selectedFile = Paths.get(savedOutputFolder).toFile();
      if (selectedFile.isDirectory()) {
        setOutputFolder(selectedFile, chooseFile);
      }
    }

    outputFolderBox.getChildren().addAll(outputFolderLabel, HorizontalSpace.create(), chooseFile);
    return outputFolderBox;
  }

  private void setOutputFolder(File selectedFile, Button chooseFile) {
    if (selectedFile != null) {
      outputFolder = selectedFile.toPath();
      // 20170524 hsilva: the following is required to be able to use the root
      // of the filesystem as output folder (otherwise NPE occurs)
      String folderName = outputFolder.getFileName() != null ? outputFolder.getFileName().toString()
        : outputFolder.toString();
      chooseFile.setText(folderName);
      setIsDisableOutputFolder(false);
    }
  }

  private HBox createSipNameStrategyBoxAndDropdown() {
    HBox prefixBox = new HBox(5);
    prefixBox.setAlignment(Pos.TOP_LEFT);

    Map<SipNameStrategy, Pane> sipNameStrategyPanes = new HashMap<>();
    HBox prefixOnlySipNameStrategyBox = createPrefixOnlySipNameStrategyBox();
    VBox hungarianSipNameStrategyBox = createHungarianSipNameStrategyBox();
    hungarianSipNameStrategyBox.setVisible(false);
    sipNameStrategyPanes.put(SipNameStrategy.ID, prefixOnlySipNameStrategyBox);
    sipNameStrategyPanes.put(SipNameStrategy.TITLE_DATE, prefixOnlySipNameStrategyBox);
    sipNameStrategyPanes.put(SipNameStrategy.TITLE_ID, prefixOnlySipNameStrategyBox);
    sipNameStrategyPanes.put(SipNameStrategy.DATE_TRANSFERRING_SERIALNUMBER, hungarianSipNameStrategyBox);

    Set<SipNameStrategy> preEnabledItems = Collections.emptySet();
    if (sipTypes.getValue() != null) {
      SipType selectedSipType = (SipType) sipTypes.getValue().getKey();
      preEnabledItems = selectedSipType.getSipNameStrategies();
    }
    sipNameStrategyComboBox = new SIPNameStrategyComboBox(preEnabledItems);
    sipNameStrategyComboBox.setMinWidth(DEFAULT_WIDTH);

    sipNameStrategyComboBox.setItems(SIP_NAME_STRATEGY_COMBO_BOX_ITEMS);

    sipNameStrategyComboBox.getSelectionModel().selectFirst();

    sipNameStrategyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        if (newValue.getKey().equals(SipNameStrategy.DATE_TRANSFERRING_SERIALNUMBER)) {
          hungarianSipNameStrategyBox.setVisible(true);
          prefixOnlySipNameStrategyBox.setVisible(false);
        } else {
          hungarianSipNameStrategyBox.setVisible(false);
          prefixOnlySipNameStrategyBox.setVisible(true);
        }
        stage.sizeToScene();
      }
    });

    prefixBox.getChildren().addAll(prefixOnlySipNameStrategyBox, hungarianSipNameStrategyBox, HorizontalSpace.create(),
      sipNameStrategyComboBox);
    return prefixBox;
  }

  private HBox createAgentName() {
    HBox labelAndField = new HBox(5);
    labelAndField.setAlignment(Pos.CENTER_LEFT);
    Label prefixLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_AGENT_NAME));
    sipAgentName = new TextField();
    sipAgentName.setMinWidth(300);
    sipAgentName.setMaxWidth(450);
    sipAgentName.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
        setIsDisableAgentName(t1.isEmpty());
      }
    });
    labelAndField.getChildren().addAll(prefixLabel, HorizontalSpace.create(), sipAgentName);
    labelAndField.managedProperty().bind(labelAndField.visibleProperty());
    return labelAndField;
  }

  private HBox createAgentNote() {
    HBox labelAndField = new HBox(5);
    labelAndField.setAlignment(Pos.CENTER_LEFT);
    Label prefixLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_AGENT_ID));
    sipAgentNote = new TextField();
    sipAgentNote.setMinWidth(300);
    sipAgentNote.setMaxWidth(450);
    sipAgentNote.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
        setIsDisableAgentID(t1.isEmpty());
      }
    });
    labelAndField.getChildren().addAll(prefixLabel, HorizontalSpace.create(), sipAgentNote);

    labelAndField.managedProperty().bind(labelAndField.visibleProperty());
    return labelAndField;
  }

  private HBox createPrefixOnlySipNameStrategyBox() {
    HBox labelAndField = new HBox(5);
    labelAndField.setAlignment(Pos.CENTER_LEFT);
    Label prefixLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_PREFIX));
    sipNameStrategyPrefix = new TextField();
    sipNameStrategyPrefix.setMinWidth(DEFAULT_WIDTH);
    sipNameStrategyPrefix.setMaxWidth(DEFAULT_WIDTH);
    sipNameStrategyPrefix.setText(ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_PREFIX));

    labelAndField.getChildren().addAll(prefixLabel, HorizontalSpace.create(), sipNameStrategyPrefix);
    labelAndField.managedProperty().bind(labelAndField.visibleProperty());
    return labelAndField;
  }

  private VBox createHungarianSipNameStrategyBox() {
    VBox form = new VBox(5);
    form.setAlignment(Pos.TOP_LEFT);

    // transferring
    HBox transferringLabelAndField = new HBox(5);
    transferringLabelAndField.setAlignment(Pos.CENTER_LEFT);
    Label transferringLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_TRANSFERRING));
    sipNameStrategyTransferring = new TextField();
    sipNameStrategyTransferring.setMinWidth(DEFAULT_WIDTH);
    sipNameStrategyTransferring.setMaxWidth(DEFAULT_WIDTH);
    sipNameStrategyTransferring.setText(ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_TRANSFERRING));
    transferringLabelAndField.getChildren().addAll(transferringLabel, HorizontalSpace.create(),
      sipNameStrategyTransferring);

    // serial number
    HBox serialLabelAndField = new HBox(5);
    serialLabelAndField.setAlignment(Pos.CENTER_LEFT);
    Label serialLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_SERIAL));
    sipNameStrategySerial = new TextField();
    sipNameStrategySerial.setMinWidth(DEFAULT_WIDTH);
    sipNameStrategySerial.setMaxWidth(DEFAULT_WIDTH);

    String serial = ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_SERIAL);
    if (StringUtils.isBlank(serial)) {
      serial = Constants.MISC_DEFAULT_HUNGARIAN_SIP_SERIAL;
    }
    sipNameStrategySerial.setText(serial);

    serialLabelAndField.getChildren().addAll(serialLabel, HorizontalSpace.create(), sipNameStrategySerial);

    form.getChildren().addAll(transferringLabelAndField, serialLabelAndField);
    form.managedProperty().bind(form.visibleProperty());
    return form;
  }

  private HBox createSipTypes() {
    HBox sipTypesBox = new HBox(5);
    sipTypesBox.setAlignment(Pos.CENTER_LEFT);

    Label sipTypesLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_SIP_FORMAT));

    sipTypes = new ComboBox<>();
    sipTypes.setMinWidth(DEFAULT_WIDTH);
    sipTypes.setId("sipTypes");
    sipTypes.getItems().addAll(SIP_TYPES);
    sipTypes.getSelectionModel().selectFirst();

    sipTypes.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        // sip name strategies combo box update
        SipType type = (SipType) newValue.getKey();
        sipAgentNameField.setVisible(SipType.EARK2.equals(type) || SipType.SIPS.equals(type));
        sipAgentNoteField.setVisible(SipType.EARK2.equals(type) || SipType.SIPS.equals(type));

        Set<SipNameStrategy> enabledStrategies = type.getSipNameStrategies();
        sipNameStrategyComboBox.setEnabledItems(enabledStrategies);

        // update start button to display the text "start" or "continue"
        if (start != null) {
          if (type.requiresMETSHeaderInfo() && METSHeaderUtils.getFieldList(type).length > 0) {
            start.setText(I18n.t(Constants.I18N_CONTINUE));
          } else {
            start.setText(I18n.t(Constants.I18N_START));
          }
        }
      }
      setIsDisableStart();

      stage.sizeToScene();
    });

    sipTypesBox.getChildren().addAll(sipTypesLabel, HorizontalSpace.create(), sipTypes);
    return sipTypesBox;
  }

  private void createBottom() {
    HBox bottom = new HBox();
    bottom.setPadding(new Insets(0, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_LEFT);

    Button cancel = new Button(I18n.t(Constants.I18N_CANCEL));
    cancel.setOnAction(actionEvent -> stage.close());

    start = new Button();
    SipType sipType = (SipType) sipTypes.getValue().getKey();
    if (sipTypes.getValue() != null && sipType.requiresMETSHeaderInfo()
      && METSHeaderUtils.getFieldList(sipType).length > 0) {
      start.setText(I18n.t(Constants.I18N_CONTINUE));
    } else {
      start.setText(I18n.t(Constants.I18N_START));
    }

    start.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        // set defaults
        SipType sipType = (SipType) sipTypes.getValue().getKey();
        SipNameStrategy sipNameStrategy = (SipNameStrategy) sipNameStrategyComboBox.getValue().getKey();
        SIPNameBuilder sipNameBuilder = null;

        // get values from text fields
        if (sipType.equals(SipType.HUNGARIAN)) {
          String serial = sipNameStrategySerial.getText();
          String nextSerial;
          try {
            nextSerial = String.format(Constants.SIP_NAME_STRATEGY_SERIAL_FORMAT_NUMBER,
              Integer.valueOf(serial) + CreationModalPreparation.this.selectedSIP);
          } catch (NumberFormatException e) {
            serial = ConfigurationManager.getAppConfig(Constants.CONF_K_EXPORT_LAST_SERIAL);
            if (StringUtils.isBlank(serial)) {
              serial = Constants.MISC_DEFAULT_HUNGARIAN_SIP_SERIAL;
            }
            nextSerial = String.format(Constants.SIP_NAME_STRATEGY_SERIAL_FORMAT_NUMBER,
              Integer.valueOf(serial) + CreationModalPreparation.this.selectedSIP);
          }
          sipNameBuilder = new SIPNameBuilderHungarian(sipNameStrategyTransferring.getText(), serial, sipNameStrategy);

          // persist in config file
          ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_TRANSFERRING,
            sipNameStrategyTransferring.getText(), true);

          ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_SERIAL, nextSerial, true);
        } else if (sipType.equals(SipType.EARK)) {
          sipNameBuilder = new SIPNameBuilderEARK(sipNameStrategyPrefix.getText(), sipNameStrategy);
          ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_PREFIX, sipNameStrategyPrefix.getText(), true);
        } else if (sipType.equals(SipType.EARK2)) {
          sipNameBuilder = new SIPNameBuilderEARK2(sipNameStrategyPrefix.getText(), sipNameStrategy);
          ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_PREFIX, sipNameStrategyPrefix.getText(), true);
        } else if (sipType.equals(SipType.BAGIT)) {
          sipNameBuilder = new SIPNameBuilderBagit(sipNameStrategyPrefix.getText(), sipNameStrategy);
          ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_PREFIX, sipNameStrategyPrefix.getText(), true);
        } else if (sipType.equals(SipType.SIPS)) {
          sipNameBuilder = new SIPNameBuilderSIPS(sipNameStrategyPrefix.getText(), sipNameStrategy);
          ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_PREFIX, sipNameStrategyPrefix.getText(), true);
        }

        // persist switches in config file
        ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_ITEM_EXPORT_SWITCH,
          String.valueOf(itemExportSwitch.isSelected()));
        ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_REPORT_CREATION_SWITCH,
          String.valueOf(reportCreationSwitch.isSelected()));
        // 20170411 bferreira: sipExportSwitch was purposely left out because
        // there is some logic in place to select that toggle

        // persist SIP type
        ConfigurationManager.setAppConfig(Constants.CONF_K_LAST_SIP_TYPE,
          (String) sipTypes.getSelectionModel().getSelectedItem().getValue());

        // persist output folder in config file
        ConfigurationManager.setAppConfig(Constants.CONF_K_EXPORT_LAST_SIP_OUTPUT_FOLDER,
          outputFolder.toAbsolutePath().toString());

        if (sipType.requiresMETSHeaderInfo() && METSHeaderUtils.getFieldList(sipType).length > 0) {
          stage.showMETSHeaderModal(CreationModalPreparation.this, outputFolder, sipExportSwitch.isSelected(),
            itemExportSwitch.isSelected(), sipType, sipNameBuilder, reportCreationSwitch.isSelected());
        } else {
          stage.startCreation(outputFolder, sipExportSwitch.isSelected(), itemExportSwitch.isSelected(), sipNameBuilder,
            reportCreationSwitch.isSelected(), new IPHeader(), Optional.of(sipAgentName.getText()),
            Optional.of(sipAgentNote.getText()));

        }
      }
    });

    start.setDisable(isDisableStart);

    bottom.getChildren().addAll(cancel, HorizontalSpace.create(), start);
    setBottom(bottom);
  }
}
