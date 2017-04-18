package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.ui.creation.HorizontalSpace;
import org.roda_project.commons_ip.model.IPAgent;
import org.roda_project.commons_ip.utils.METSEnums;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class AgentItem extends AbstractItem {
  // i18n configurable strings
  private final String i18nNameLabel;
  private final String i18nNoteLabel;
  private final String i18nTypeLabel;
  private final String i18nOtherTypeLabel;
  private final String i18nRoleLabel;

  // mandatory field configs
  boolean mandatoryNote;

  // default (pre-selected and non-modifiable) values
  String predefinedType;
  String predefinedRole;
  String predefinedOtherType;
  List<AgentGroup.NameAndNotePair> predefinedNamesAndNotes;

  private Button btnRemove;

  private TextField tfName;
  private TextField tfNote;
  private TextField tfType;
  private TextField tfRole;
  private TextField tfOtherType;
  private ComboBox<AgentGroup.NameAndNotePair> cbNameAndNote;

  AgentItem(AbstractGroup parent, String i18nNameLabel, String i18nNameDescription, String i18nNoteLabel,
    String i18nNoteDescription, boolean mandatoryNote, String i18nTypeLabel, String i18nTypeDescription,
    String predefinedType, String i18nRoleLabel, String i18nRoleDescription, String predefinedRole,
    String i18nOtherTypeLabel, String i18nOtherTypeDescription, String predefinedOtherType,
    List<AgentGroup.NameAndNotePair> predefinedNamesAndNotes) {
    super(parent);

    this.i18nNameLabel = i18nNameLabel;
    this.i18nNoteLabel = i18nNoteLabel;
    this.i18nTypeLabel = i18nTypeLabel;
    this.i18nOtherTypeLabel = i18nOtherTypeLabel;
    this.i18nRoleLabel = i18nRoleLabel;
    this.mandatoryNote = mandatoryNote;
    this.predefinedType = predefinedType;
    this.predefinedRole = predefinedRole;
    this.predefinedOtherType = predefinedOtherType;
    this.predefinedNamesAndNotes = predefinedNamesAndNotes;

    this.useStyleForMultipleFields();

    VBox inputsColumn = new VBox(5);
    HBox.setHgrow(inputsColumn, Priority.ALWAYS);

    boolean typeMayBeOther = true;
    if (!hasPredefinedType()) {
      HBox rowType = new HBox(5);
      rowType.setAlignment(Pos.CENTER_LEFT);
      Label labelType = new Label(i18nTypeLabel);
      labelType.setTooltip(new Tooltip(i18nTypeDescription));
      tfType = new TextField();
      tfType.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfType.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      Platform.runLater(tfType::requestFocus);
      rowType.getChildren().addAll(labelType, HorizontalSpace.create(), tfType);
      inputsColumn.getChildren().add(rowType);
    } else {
      try {
        METSEnums.CreatorType type = METSEnums.CreatorType.valueOf(predefinedType);
        typeMayBeOther = METSEnums.CreatorType.OTHER.equals(type);
      } catch (IllegalArgumentException e) {
        // ignore invalid default value
      }
    }

    if (typeMayBeOther && !hasPredefinedOtherType()) {
      HBox rowOtherType = new HBox(5);
      rowOtherType.setAlignment(Pos.CENTER_LEFT);
      Label labelOtherType = new Label(i18nOtherTypeLabel);
      labelOtherType.setTooltip(new Tooltip(i18nOtherTypeDescription));
      tfOtherType = new TextField();
      tfOtherType.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfOtherType.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      rowOtherType.getChildren().addAll(labelOtherType, HorizontalSpace.create(), tfOtherType);
      inputsColumn.getChildren().add(rowOtherType);
    }

    if (!hasPredefinedRole()) {
      HBox rowRole = new HBox(5);
      rowRole.setAlignment(Pos.CENTER_LEFT);
      Label labelRole = new Label(i18nRoleLabel);
      labelRole.setTooltip(new Tooltip(i18nRoleDescription));
      tfRole = new TextField();
      tfRole.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfRole.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      rowRole.getChildren().addAll(labelRole, HorizontalSpace.create(), tfRole);
      inputsColumn.getChildren().add(rowRole);
    }

    if (!predefinedNamesAndNotes.isEmpty()) {
      HBox rowNameAndNote = new HBox(5);
      rowNameAndNote.setAlignment(Pos.CENTER_LEFT);
      Label labelNameAndNote = new Label(i18nNameLabel + "/" + i18nNoteLabel);
      labelNameAndNote.setTooltip(new Tooltip(i18nNameDescription + " / " + i18nNoteDescription));
      cbNameAndNote = new ComboBox<>();
      cbNameAndNote.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      cbNameAndNote.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      cbNameAndNote.getItems().addAll(predefinedNamesAndNotes);
      cbNameAndNote.getSelectionModel().selectFirst();
      rowNameAndNote.getChildren().addAll(labelNameAndNote, HorizontalSpace.create(), cbNameAndNote);

      inputsColumn.getChildren().addAll(rowNameAndNote);
    } else {
      HBox rowName = new HBox(5);
      rowName.setAlignment(Pos.CENTER_LEFT);
      Label labelName = new Label(i18nNameLabel);
      labelName.setTooltip(new Tooltip(i18nNameDescription));
      tfName = new TextField();
      tfName.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfName.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      rowName.getChildren().addAll(labelName, HorizontalSpace.create(), tfName);

      HBox rowNote = new HBox(5);
      rowNote.setAlignment(Pos.CENTER_LEFT);
      Label labelNote = new Label(i18nNoteLabel);
      labelNote.setTooltip(new Tooltip(i18nNoteDescription));
      tfNote = new TextField();
      tfNote.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfNote.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      rowNote.getChildren().addAll(labelNote, HorizontalSpace.create(), tfNote);

      inputsColumn.getChildren().addAll(rowName, rowNote);
    }

    if (!hasPredefinedType()) {
      Platform.runLater(tfType::requestFocus);
    } else if (typeMayBeOther && !hasPredefinedOtherType()) {
      Platform.runLater(tfOtherType::requestFocus);
    } else if (!hasPredefinedRole()) {
      Platform.runLater(tfRole::requestFocus);
    } else if (!predefinedNamesAndNotes.isEmpty()) {
      Platform.runLater(cbNameAndNote::requestFocus);
    } else {
      Platform.runLater(tfName::requestFocus);
    }

    btnRemove = getRemoveButton();
    btnRemove.minHeightProperty().bind(inputsColumn.heightProperty());
    btnRemove.maxHeightProperty().bind(inputsColumn.heightProperty());

    VBox removeButtonColumn = new VBox(5, btnRemove);

    this.getChildren().addAll(inputsColumn, removeButtonColumn);
  }

  public AgentItem(AbstractGroup parent, String i18nNameLabel, String i18nNameDescription, String i18nNoteLabel,
    String i18nNoteDescription, boolean mandatoryNote, String i18nTypeLabel, String i18nTypeDescription,
    String predefinedType, String i18nRoleLabel, String i18nRoleDescription, String predefinedRole,
    String i18nOtherTypeLabel, String i18nOtherTypeDescription, String predefinedOtherType,
    List<AgentGroup.NameAndNotePair> namesAndNotes, IPAgent savedItem) {

    this(parent, i18nNameLabel, i18nNameDescription, i18nNoteLabel, i18nNoteDescription, mandatoryNote, i18nTypeLabel,
      i18nTypeDescription, predefinedType, i18nRoleLabel, i18nRoleDescription, predefinedRole, i18nOtherTypeLabel,
      i18nOtherTypeDescription, predefinedOtherType, namesAndNotes);

    if (tfType != null) {
      tfType.setText(savedItem.getType().toString());
    }

    if (tfOtherType != null) {
      tfOtherType.setText(savedItem.getOtherType());
    }

    if (tfRole != null) {
      tfRole.setText(savedItem.getRole());
    }

    if (cbNameAndNote != null) {
      AgentGroup.NameAndNotePair nameAndNote = new AgentGroup.NameAndNotePair(savedItem.getName(), savedItem.getNote());
      if (namesAndNotes.contains(nameAndNote)) {
        cbNameAndNote.getSelectionModel().select(nameAndNote);
      }
    } else {
      tfName.setText(savedItem.getName());
      tfNote.setText(savedItem.getNote());
    }
  }

  @Override
  protected boolean internalIsValid(List<String> addFailureReasonsToThisList) {
    if (!hasPredefinedType() && StringUtils.isBlank(tfType.getText())) {
      addFailureReasonsToThisList.add(i18nTypeLabel + " is a mandatory field and can not be blank");
    }

    METSEnums.CreatorType type = null;
    try {
      if (hasPredefinedType()) {
        type = METSEnums.CreatorType.valueOf(predefinedType);
      } else {
        type = METSEnums.CreatorType.valueOf(tfType.getText());
      }
    } catch (IllegalArgumentException e) {
      String collect = Stream.of(METSEnums.CreatorType.values()).map(Enum::toString).collect(Collectors.joining(", "));
      addFailureReasonsToThisList.add(String.format("'%s' is not a valid %s. Must be one of %s",
        StringUtils.isBlank(tfType.getText()) ? "<blank>" : tfType.getText(), i18nTypeLabel, collect));
    }

    if (type != null && type.equals(METSEnums.CreatorType.OTHER) && !hasPredefinedOtherType()
      && StringUtils.isBlank(tfOtherType.getText())) {
      addFailureReasonsToThisList.add(i18nOtherTypeLabel + " is a mandatory field when " + i18nTypeLabel + " is "
        + METSEnums.CreatorType.OTHER.toString() + " and can not be blank");
    }

    if (!hasPredefinedRole() && StringUtils.isBlank(tfRole.getText())) {
      addFailureReasonsToThisList.add(i18nRoleLabel + " is a mandatory field and can not be blank");
    }

    if (cbNameAndNote == null) {
      if (StringUtils.isBlank(tfName.getText())) {
        addFailureReasonsToThisList.add(i18nNameLabel + " is a mandatory field and can not be blank");
      }

      if (mandatoryNote && StringUtils.isBlank(tfNote.getText())) {
        addFailureReasonsToThisList.add(i18nNoteLabel + " is a mandatory field and can not be blank");
      }
    }

    return addFailureReasonsToThisList.isEmpty();
  }

  public IPAgent getValue() {
    IPAgent ipAgent = new IPAgent();

    if (cbNameAndNote != null) {
      AgentGroup.NameAndNotePair selectedItem = cbNameAndNote.getSelectionModel().getSelectedItem();
      ipAgent.setName(selectedItem.getName());
      ipAgent.setNote(selectedItem.getNote());
    } else {
      ipAgent.setName(tfName.getText());
      ipAgent.setNote(tfNote.getText());
    }

    if (hasPredefinedRole()) {
      ipAgent.setRole(predefinedRole);
    } else {
      ipAgent.setRole(tfRole.getText());
    }

    if (hasPredefinedType()) {
      ipAgent.setType(METSEnums.CreatorType.valueOf(predefinedType));
    } else {
      ipAgent.setType(METSEnums.CreatorType.valueOf(tfType.getText()));
    }

    if (METSEnums.CreatorType.OTHER.equals(ipAgent.getType())) {
      if (hasPredefinedOtherType()) {
        ipAgent.setOtherType(predefinedOtherType);
      } else {
        ipAgent.setOtherType(tfOtherType.getText());
      }
    }

    return ipAgent;
  }

  private boolean hasPredefinedType() {
    return StringUtils.isNotBlank(predefinedType);
  }

  private boolean hasPredefinedRole() {
    return StringUtils.isNotBlank(predefinedRole);
  }

  private boolean hasPredefinedOtherType() {
    return StringUtils.isNotBlank(predefinedOtherType);
  }
}
