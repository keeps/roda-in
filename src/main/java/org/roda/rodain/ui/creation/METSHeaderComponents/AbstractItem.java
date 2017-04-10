package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.ArrayList;
import java.util.List;

import org.roda.rodain.core.Constants;
import org.roda.rodain.ui.utils.FontAwesomeImageCreator;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * This is the element corresponding to one IPHeader sub-object (one agent, one
 * AltRecord, etc). It is managed by an AbstractGroup.
 *
 * Each AbstractItem instance should be translatable into a single object used
 * in the IPHeader.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class AbstractItem extends HBox {
  private static final String ICON_CLOSE = FontAwesomeImageCreator.TIMES;
  protected static final int DEFAULT_TEXTFIELD_WIDTH = 240;

  protected List<String> validationFailures = new ArrayList<>();

  private AbstractGroup parent;
  private Button removeButton = null;

  AbstractItem(AbstractGroup parent) {
    super(5);
    this.parent = parent;
    addSpacingStyleToRow();
  }

  public void setRemovable(boolean removable) {
    getRemoveButton().setDisable(!removable);
  }

  private void addSpacingStyleToRow() {
    if (parent.getMaximum() == 1) {
      this.getStyleClass().add(Constants.CSS_METS_HEADER_ITEM_WITHOUT_SIBLINGS);
    } else {
      this.getStyleClass().add(Constants.CSS_METS_HEADER_ITEM_WITH_SIBLINGS);
    }
  }

  protected Button getRemoveButton() {
    if (removeButton == null) {
      removeButton = new Button();
      removeButton.setTooltip(new Tooltip("Remove this item"));
      removeButton.setOnAction(event -> parent.removeItem(this));
      Platform.runLater(() -> {
        Image image = FontAwesomeImageCreator.generate(ICON_CLOSE, Color.WHITE);
        ImageView imageView = new ImageView(image);
        removeButton.setGraphic(imageView);
      });
      if (parent.getMinimum() == parent.getMaximum()) {
        removeButton.setVisible(false);
      }
    }
    return removeButton;
  }

  protected abstract boolean internalIsValid(List<String> addFailureReasonsToThisList);

  /**
   * Check if this is a valid item. If it is not valid, the reasons for that can
   * be obtained using getValidationFailures()
   */
  public boolean isValid() {
    validationFailures = new ArrayList<>();
    return internalIsValid(validationFailures);
  }

  /**
   * isValid() should be called first, and then this method should be called
   * when the validation fails, to get the validation failure reasons
   */
  public List<String> getValidationFailures() {
    return validationFailures;
  }

  protected void useStyleForMultipleFields() {
    this.getStyleClass().add(Constants.CSS_METS_HEADER_ITEM_WITH_MULTIPLE_FIELDS);

  }
}
