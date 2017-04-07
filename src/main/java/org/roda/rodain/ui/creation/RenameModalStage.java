package org.roda.rodain.ui.creation;

import org.roda.rodain.core.Constants;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RenameModalStage extends Stage {
  private static final int PREPARATION_HEIGHT = 160;
  private static final int PROCESSING_HEIGHT = PREPARATION_HEIGHT - 60;
  private ColorAdjust colorAdjust;
  private Stage primaryStage;

  /**
   * The stage of the SIP exportation panels
   *
   * @param primaryStage
   *          The primary stage of the application
   */
  public RenameModalStage(Stage primaryStage) {
    super(StageStyle.TRANSPARENT);
    this.primaryStage = primaryStage;
    initModality(Modality.WINDOW_MODAL);
    initOwner(primaryStage);

    colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(-0.275);

    setResizable(true);

    Scene scene = new Scene(new HBox(), 400, PREPARATION_HEIGHT);
    scene.getStylesheets().add(ClassLoader.getSystemResource(Constants.RSC_CSS_MODAL).toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource(Constants.RSC_CSS_SHARED).toExternalForm());
    setScene(scene);
  }

  /**
   * Used to remove the color adjustment effect on the background and remove
   * this Stage.
   */
  @Override
  public void close() {
    getOwner().getScene().getRoot().setEffect(null);
    super.close();
  }

  /**
   * Sets the root Scene of this Stage, applies a color adjustment effect and
   * enables dragging of the window.
   *
   * @param root
   *          The pane to be set as root
   */
  public void setRoot(Parent root) {
    this.getScene().setRoot(root);
    primaryStage.getScene().getRoot().setEffect(colorAdjust);

    // allow the dialog to be dragged around.
    final Delta dragDelta = new Delta();
    // reference to be used in the handlers
    final RenameModalStage thisDialog = this;

    root.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        // record a delta distance for the drag and drop operation.
        dragDelta.x = thisDialog.getX() - mouseEvent.getScreenX();
        dragDelta.y = thisDialog.getY() - mouseEvent.getScreenY();
      }
    });
    root.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        thisDialog.setX(mouseEvent.getScreenX() + dragDelta.x);
        thisDialog.setY(mouseEvent.getScreenY() + dragDelta.y);
      }
    });

    show();
  }

  // records relative x and y co-ordinates.
  class Delta {
    double x, y;
  }
}