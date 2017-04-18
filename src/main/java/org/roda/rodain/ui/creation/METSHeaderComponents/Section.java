package org.roda.rodain.ui.creation.METSHeaderComponents;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Section extends VBox {
  private AbstractGroup lastGroupAdded = null;

  private Section() {
    this("", "", "");
  }

  public Section(String sipTypeName, String type, String fallbackI18N) {
    super(0);

    Label labelTitle = new Label(getLabelText(sipTypeName, type, fallbackI18N));
    labelTitle.getStyleClass().addAll(Constants.CSS_METS_HEADER_SECTION_TITLE, Constants.CSS_TITLE);

    VBox header = new VBox(0, labelTitle);
    header.getStyleClass().add(Constants.CSS_METS_HEADER_SECTION);
    header.setPadding(new Insets(10, 10, 10, 10));
    header.setAlignment(Pos.CENTER_LEFT);

    this.setPadding(new Insets(10, 10, 10, 10));
    this.setAlignment(Pos.CENTER_LEFT);
    this.getChildren().add(header);
  }

  public void addGroup(AbstractGroup group) {
    if (lastGroupAdded != null) {
      lastGroupAdded.styleAsNormalGroup();
    }
    getChildren().add(group);
    group.styleAsLastGroup();
    lastGroupAdded = group;
  }

  private String getLabelText(String sipTypeName, String type, String fallbackI18N) {
    String statusI18N = I18n.t(ConfigurationManager.getConfig(
      Constants.CONF_K_METS_HEADER_FIELDS_PREFIX + sipTypeName + Constants.CONF_K_METS_HEADER_TYPES_SEPARATOR + type));
    if (StringUtils.isBlank(statusI18N)) {
      statusI18N = METSHeaderUtils.getTextFromI18N(fallbackI18N);
    }
    return statusI18N;
  }
}
