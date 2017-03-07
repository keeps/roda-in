package org.roda.rodain.ui;

import org.roda.rodain.core.Constants;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 15-10-2015.
 */
public class ModalStage extends Stage {
  private ColorAdjust colorAdjust;
  private Stage primaryStage;

  /**
   * Creates a new RuleModalStage to be used in the Rule creation and removal.
   *
   * @param primaryStage
   *          The primary stage of the application.
   */
  public ModalStage(Stage primaryStage) {
    super(StageStyle.TRANSPARENT);
    this.primaryStage = primaryStage;
    initModality(Modality.WINDOW_MODAL);
    initOwner(primaryStage);

    colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(-0.275);

    setResizable(false);

    Scene scene = new Scene(new HBox(), 800, 580);
    scene.getStylesheets().add(ClassLoader.getSystemResource(Constants.RSC_CSS_SHARED).toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource(Constants.RSC_CSS_MODAL).toExternalForm());
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
  public void setRoot(Parent root, boolean showAndWait) {
    this.getScene().setRoot(root);

    primaryStage.getScene().getRoot().setEffect(colorAdjust);

    // allow the dialog to be dragged around.
    final Delta dragDelta = new Delta();
    final ModalStage thisDialog = this; // reference to be used in the
    // handlers
    root.setOnMousePressed(event -> {
      // record a delta distance for the drag and drop operation.
      dragDelta.x = thisDialog.getX() - event.getScreenX();
      dragDelta.y = thisDialog.getY() - event.getScreenY();
    });
    root.setOnMouseDragged(event -> {
      thisDialog.setX(event.getScreenX() + dragDelta.x);
      thisDialog.setY(event.getScreenY() + dragDelta.y);
    });
    if (showAndWait) {
      showAndWait();
    } else {
      show();
    }

  }

  // records relative x and y co-ordinates.
  class Delta {
    double x, y;
  }
}
