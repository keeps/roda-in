package org.roda.rodain.creation.ui;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.roda.rodain.creation.CreateSips;
import org.roda.rodain.creation.SipTypes;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreationModalStage extends Stage {
  private static final int PREPARATION_HEIGHT = 270;
  private static final int PROCESSING_HEIGHT = PREPARATION_HEIGHT - 60;
  private ColorAdjust colorAdjust;
  private Stage primaryStage;

  /**
   * The stage of the SIP exportation panels
   *
   * @param primaryStage
   *          The primary stage of the application
   */
  public CreationModalStage(Stage primaryStage) {
    super(StageStyle.TRANSPARENT);
    this.primaryStage = primaryStage;
    initModality(Modality.WINDOW_MODAL);
    initOwner(primaryStage);

    colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(-0.275);

    setResizable(true);

    Scene scene = new Scene(new HBox(), 400, PREPARATION_HEIGHT);
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/modal.css").toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/shared.css").toExternalForm());
    setScene(scene);
  }

  /**
   * Starts the CreateSips thread and changes the scene to
   * CreationModalProcessing.
   *
   * @param outputFolder
   *          The output folder for the SIP exportation
   * @param type
   *          The format of the SIPs
   */
  public void startCreation(Path outputFolder, SipTypes type, boolean exportAll, boolean exportItems, String prefix, CreationModalPreparation.NAME_TYPES name_type) {
    setHeight(PROCESSING_HEIGHT);
    CreateSips creator = new CreateSips(outputFolder, type, exportAll, exportItems, prefix, name_type);
    CreationModalProcessing pane = new CreationModalProcessing(creator, this);
    setRoot(pane);
    creator.start();
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
    final CreationModalStage thisDialog = this; // reference to be used in the
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