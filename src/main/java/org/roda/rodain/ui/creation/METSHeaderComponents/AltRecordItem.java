package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.ui.creation.HorizontalSpace;
import org.roda_project.commons_ip.model.IPAltRecordID;

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
public class AltRecordItem extends AbstractItem {
  // i18n configurable strings
  private final String i18nValueLabel;
  private final String i18nTypeLabel;

  // default (pre-selected and non-modifiable) values
  private final String predefinedType;

  private Button btnRemove;

  private TextField tfType;
  private TextField tfValue;
  private ComboBox<String> cbValue;

  public AltRecordItem(AbstractGroup parent, String i18nValueLabel, String i18nValueDescription, String i18nTypeLabel,
    String i18nTypeDescription, String predefinedType, String[] possibleValues) {
    super(parent);
    this.i18nValueLabel = i18nValueLabel;
    this.i18nTypeLabel = i18nTypeLabel;

    this.predefinedType = predefinedType;

    VBox inputsColumn = new VBox(5);
    HBox.setHgrow(inputsColumn, Priority.ALWAYS);

    if (!hasPredefinedType()) {
      HBox rowType = new HBox(5);
      rowType.setAlignment(Pos.CENTER_LEFT);
      Label labelType = new Label(i18nTypeLabel);
      labelType.setTooltip(new Tooltip(i18nTypeDescription));
      tfType = new TextField();
      tfType.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfType.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      rowType.getChildren().addAll(labelType, HorizontalSpace.create(), tfType);
      inputsColumn.getChildren().add(rowType);

      this.useStyleForMultipleFields();
    }

    HBox rowValue = new HBox(5);
    rowValue.setAlignment(Pos.CENTER_LEFT);
    Label labelValue = new Label(i18nValueLabel);
    labelValue.setTooltip(new Tooltip(i18nValueDescription));
    if (possibleValues.length != 0) {
      cbValue = new ComboBox<>();
      cbValue.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      cbValue.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      cbValue.getItems().addAll(possibleValues);
      cbValue.getSelectionModel().selectFirst();
      rowValue.getChildren().addAll(labelValue, HorizontalSpace.create(), cbValue);
    } else {
      tfValue = new TextField();
      tfValue.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
      tfValue.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
      rowValue.getChildren().addAll(labelValue, HorizontalSpace.create(), tfValue);
    }
    inputsColumn.getChildren().add(rowValue);

    if (!hasPredefinedType()) {
      Platform.runLater(tfType::requestFocus);
    } else {
      if (cbValue != null) {
        Platform.runLater(cbValue::requestFocus);
      } else {
        Platform.runLater(tfValue::requestFocus);
      }
    }

    btnRemove = getRemoveButton();
    btnRemove.minHeightProperty().bind(inputsColumn.heightProperty());
    btnRemove.maxHeightProperty().bind(inputsColumn.heightProperty());

    VBox removeButtonColumn = new VBox(5, btnRemove);

    this.getChildren().addAll(inputsColumn, removeButtonColumn);
  }

  public AltRecordItem(AbstractGroup parent, String i18nValueLabel, String i18nValueDescription, String i18nTypeLabel,
    String i18nTypeDescription, String predefinedType, String[] possibleValues, IPAltRecordID savedValue) {
    this(parent, i18nValueLabel, i18nValueDescription, i18nTypeLabel, i18nTypeDescription, predefinedType,
      possibleValues);

    if (tfType != null) {
      tfType.setText(savedValue.getType());
    }

    if (tfValue != null) {
      tfValue.setText(savedValue.getValue());
    }

    if (cbValue != null) {
      cbValue.getSelectionModel().select(savedValue.getValue());
    }
  }

  @Override
  protected boolean internalIsEmpty() {
    if (!hasPredefinedType() && StringUtils.isNotBlank(tfType.getText())) {
      return false;
    }

    if (StringUtils.isNotBlank(getFieldValue())) {
      return false;
    }

    return true;
  }

  @Override
  protected boolean internalIsValid(List<String> addFailureReasonsToThisList) {
    if (!hasPredefinedType() && StringUtils.isBlank(tfType.getText())) {
      addFailureReasonsToThisList
        .add(I18n.t(Constants.I18N_CREATIONMODALMETSHEADER_ERROR_MANDATORY_CAN_NOT_BE_BLANK, i18nTypeLabel));
    }

    if (StringUtils.isBlank(getFieldValue())) {
      addFailureReasonsToThisList
        .add(I18n.t(Constants.I18N_CREATIONMODALMETSHEADER_ERROR_MANDATORY_CAN_NOT_BE_BLANK, i18nValueLabel));
    }

    return addFailureReasonsToThisList.isEmpty();
  }

  public IPAltRecordID getValue() {
    IPAltRecordID altRecord = new IPAltRecordID();

    if (hasPredefinedType()) {
      altRecord.setType(predefinedType);
    } else {
      altRecord.setType(tfType.getText());
    }
    altRecord.setValue(getFieldValue());

    return altRecord;
  }

  private boolean hasPredefinedType() {
    return StringUtils.isNotBlank(predefinedType);
  }

  private String getFieldValue() {
    if (tfValue != null) {
      return tfValue.getText();
    } else if (cbValue != null) {
      return cbValue.getSelectionModel().getSelectedItem();
    } else {
      return null;
    }
  }
}
