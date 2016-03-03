package org.roda.rodain.rules.ui;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 15-10-2015.
 */
public class RuleModalStage extends Stage {
  private ColorAdjust colorAdjust;
  private Stage primaryStage;

  /**
   * Creates a new RuleModalStage to be used in the Rule creation and removal.
   *
   * @param primaryStage
   *          The primary stage of the application.
   */
  public RuleModalStage(Stage primaryStage) {
    super(StageStyle.TRANSPARENT);
    this.primaryStage = primaryStage;
    initModality(Modality.WINDOW_MODAL);
    initOwner(primaryStage);

    colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(-0.275);

    setResizable(false);

    Scene scene = new Scene(new HBox(), 800, 580);
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/modal.css").toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/shared.css").toExternalForm());
    setScene(scene);
  }

  /**
   * Removes the background color effect and closes the stage.
   */
  @Override
  public void close() {
    getOwner().getScene().getRoot().setEffect(null);
    super.close();
  }

  /**
   * Sets the root pane of the stage.
   *
   * @param root
   *          The pane to be set as root.
   */
  public void setRoot(Parent root) {
    this.getScene().setRoot(root);

    primaryStage.getScene().getRoot().setEffect(colorAdjust);

    // allow the dialog to be dragged around.
    final Delta dragDelta = new Delta();
    final RuleModalStage thisDialog = this; // reference to be used in the
    // handlers
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
