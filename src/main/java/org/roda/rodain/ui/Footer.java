package org.roda.rodain.ui;

import java.util.Timer;
import java.util.TimerTask;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.ui.source.FileExplorerPane;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * The Node used as the footer of the UI to show a fileExplorerStatus message.
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class Footer extends VBox {
  private static Label fileExplorerStatus, classPlanStatus, memoryUsage;
  private static HBox fileExplorerBox;
  private static SplitPane splitPane;
  private static Footer instance = null;
  private static Timer timer;

  /**
   * Creates a new Footer object
   */
  private Footer() {
    super();
    getStyleClass().add("footer");

    Separator separator = new Separator();

    splitPane = new SplitPane();
    splitPane.setId("footer-split-pane");

    fileExplorerBox = new HBox();
    fileExplorerStatus = new Label();
    fileExplorerStatus.setPadding(new Insets(5, 5, 5, 15));
    fileExplorerStatus.setAlignment(Pos.CENTER_LEFT);
    fileExplorerStatus.setTextAlignment(TextAlignment.CENTER);
    fileExplorerBox.getChildren().add(fileExplorerStatus);

    HBox otherBox = new HBox();
    classPlanStatus = new Label();
    classPlanStatus.setPadding(new Insets(5, 5, 5, 15));
    classPlanStatus.setAlignment(Pos.CENTER_LEFT);
    classPlanStatus.setTextAlignment(TextAlignment.CENTER);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    memoryUsage = new Label();
    memoryUsage.setPadding(new Insets(5, 15, 5, 5));
    memoryUsage.setAlignment(Pos.CENTER_RIGHT);
    memoryUsage.setTextAlignment(TextAlignment.CENTER);

    otherBox.getChildren().addAll(classPlanStatus, space, memoryUsage);

    splitPane.getItems().addAll(fileExplorerBox, otherBox);
    this.getChildren().addAll(separator, splitPane);

    memoryAutoUpdater();
  }

  public static Footer getInstance() {
    if (instance == null) {
      instance = new Footer();
    }
    return instance;
  }

  /**
   * Sets the fileExplorerStatus label with the String received as parameter.
   *
   * @param st
   *          The String to be set as the fileExplorerStatus.
   */
  public static void setFileExplorerStatus(final String st) {
    Platform.runLater(() -> fileExplorerStatus.setText(st));
  }

  /**
   * Sets the classPlanStatus label with the String received as parameter.
   *
   * @param st
   *          The String to be set as the classPlanStatus.
   */
  public static void setClassPlanStatus(final String st) {
    Platform.runLater(() -> classPlanStatus.setText(st));
  }

  public static void addBindings(FileExplorerPane fileExplorerPane) {
    fileExplorerBox.minWidthProperty().bind(fileExplorerPane.widthProperty());
    fileExplorerBox.maxWidthProperty().bind(fileExplorerPane.widthProperty());
  }

  private void memoryAutoUpdater() {
    TimerTask updater = new TimerTask() {
      @Override
      public void run() {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        String value = String.format(I18n.t(Constants.I18N_FOOTER_MEMORY), Controller.formatSize(total - free),
          Controller.formatSize(total));
        Platform.runLater(() -> memoryUsage.setText(value));
      }
    };

    timer = new Timer();
    timer.schedule(updater, 1000, 1000);
  }

  public void cancelMemoryAutoUpdater() {
    timer.cancel();
  }
}
