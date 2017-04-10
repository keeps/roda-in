package org.roda.rodain.ui.creation;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Shorthand class to create horizontal empty space
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class HorizontalSpace extends HBox {
  public static HorizontalSpace create() {
    return new HorizontalSpace();
  }

  private HorizontalSpace() {
    HBox.setHgrow(this, Priority.ALWAYS);
  }
}
