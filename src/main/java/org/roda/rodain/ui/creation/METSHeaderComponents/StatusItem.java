package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.ui.creation.HorizontalSpace;
import org.roda_project.commons_ip.utils.IPEnums;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class StatusItem extends AbstractItem {
  private Button btnRemove;

  private ComboBox<String> cbValue;

  public StatusItem(AbstractGroup parent, String i18nValueLabel, String i18nValueDescription) {
    super(parent);

    this.setAlignment(Pos.CENTER_LEFT);
    Label label = new Label(i18nValueLabel);
    label.setTooltip(new Tooltip(i18nValueDescription));
    cbValue = new ComboBox<>();
    cbValue.setMinWidth(DEFAULT_TEXTFIELD_WIDTH);
    cbValue.setMaxWidth(DEFAULT_TEXTFIELD_WIDTH);
    cbValue.getItems()
      .addAll(Stream.of(IPEnums.IPStatus.values()).map(IPEnums.IPStatus::toString).collect(Collectors.toList()));
    cbValue.getSelectionModel().selectFirst();
    Platform.runLater(cbValue::requestFocus);

    btnRemove = getRemoveButton();

    this.getChildren().addAll(label, HorizontalSpace.create(), cbValue, btnRemove);
  }

  public StatusItem(AbstractGroup parent, String i18nValueLabel, String i18nValueDescription, IPEnums.IPStatus status) {
    this(parent, i18nValueLabel, i18nValueDescription);
    if (cbValue.getItems().contains(status.toString())) {
      cbValue.getSelectionModel().select(status.toString());
    }
  }

  @Override
  protected boolean internalIsEmpty() {
    return false;
  }

  @Override
  protected boolean internalIsValid(List<String> addFailureReasonsToThisList) {
    // status defaults to NEW if blank
    try {
      IPEnums.IPStatus.valueOf(getComboboxValue());
      return true;
    } catch (IllegalArgumentException e) {
      String collect = Stream.of(IPEnums.IPStatus.values()).map(Enum::toString).collect(Collectors.joining(", "));
      addFailureReasonsToThisList.add(String.format("'%s' is not a valid status. Must be one of %s",
        StringUtils.isBlank(getComboboxValue()) ? "<blank>" : getComboboxValue(), collect));
      return false;
    }
  }

  public IPEnums.IPStatus getValue() {
    return IPEnums.IPStatus.valueOf(getComboboxValue());
  }

  private String getComboboxValue() {
    if (cbValue != null && cbValue.getSelectionModel() != null) {
      return cbValue.getSelectionModel().getSelectedItem();
    } else {
      return "";
    }
  }
}
