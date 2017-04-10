package org.roda.rodain.ui.creation.METSHeaderComponents;

import org.roda.rodain.core.Constants;

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
    this("");
  }

  public Section(String title) {
    super(5);

    Label labelTitle = new Label(title);
    labelTitle.getStyleClass().add(Constants.CSS_METS_HEADER_SECTION_TITLE);

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
}
