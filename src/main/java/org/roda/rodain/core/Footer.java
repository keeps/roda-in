package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * The Node used as the footer of the UI to show a status message.
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class Footer extends HBox {
  public static final Label status = new Label();

  public Footer() {
    super();
    getStyleClass().add("footer");

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    this.setPadding(new Insets(5, 5, 5, 5));
    this.setSpacing(10);
    this.setAlignment(Pos.CENTER_LEFT);
    this.getChildren().add(status);
  }

  /**
   * Sets the status label with the String received as parameter.
   * 
   * @param st
   *          The String to be set as the status.
   */
  public static void setStatus(final String st) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        status.setText(st);
      }
    });
  }
}
