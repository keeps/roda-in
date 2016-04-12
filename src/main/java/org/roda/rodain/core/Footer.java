package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import org.roda.rodain.source.ui.FileExplorerPane;

/**
 * The Node used as the footer of the UI to show a fileExplorerStatus message.
 *
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class Footer extends VBox {
  private static Label fileExplorerStatus, classPlanStatus;
  private static HBox fileExplorerBox;
  private static SplitPane splitPane;
  private static Footer instance = null;

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
    fileExplorerStatus.setPadding(new Insets(5, 5, 5, 5));
    fileExplorerStatus.setAlignment(Pos.CENTER_LEFT);
    fileExplorerStatus.setTextAlignment(TextAlignment.CENTER);
    fileExplorerBox.getChildren().add(fileExplorerStatus);

    classPlanStatus = new Label();
    classPlanStatus.setPadding(new Insets(5, 5, 5, 5));
    classPlanStatus.setAlignment(Pos.CENTER_LEFT);
    classPlanStatus.setTextAlignment(TextAlignment.CENTER);

    splitPane.getItems().addAll(fileExplorerBox, classPlanStatus);
    this.getChildren().addAll(separator, splitPane);
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
}
