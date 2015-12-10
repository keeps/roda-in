package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The Node used as the footer of the UI to show a status message.
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class Footer extends VBox {
  private static Label status;

  public Footer() {
    super(5);
    getStyleClass().add("footer");
    setAlignment(Pos.CENTER_LEFT);

    Separator separator = new Separator();

    status = new Label();
    status.setPadding(new Insets(0, 0, 5, 5));
    status.setAlignment(Pos.CENTER_LEFT);
    status.setTextAlignment(TextAlignment.CENTER);

    this.getChildren().addAll(separator, status);
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
