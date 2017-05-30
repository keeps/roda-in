package org.roda.rodain.ui.creation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javafx.event.Event;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.creation.CreateSips;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.utils.UnsafeDouble;
import org.roda_project.commons_ip.model.IPHeader;

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
 * @since 19/11/2015.
 */
public class CreationModalStage extends Stage {
  private static final int PREPARATION_HEIGHT = 300;
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

    setOnCloseRequest(Event::consume);
    setResizable(true);

    Scene scene = new Scene(new HBox());
    scene.getStylesheets().add(ClassLoader.getSystemResource(Constants.RSC_CSS_MODAL).toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource(Constants.RSC_CSS_SHARED).toExternalForm());
    setScene(scene);
  }

  /**
   * Changes the scene to CreationModalMETSHeader.
   */
  public void showMETSHeaderModal(CreationModalPreparation previousPanel, Path outputFolder, boolean exportAll,
    boolean exportItems, Constants.SipType sipType, SIPNameBuilder sipNameBuilder, boolean createReport) {
    CreationModalMETSHeader pane = new CreationModalMETSHeader(this, previousPanel, outputFolder, exportAll,
      exportItems, sipType, sipNameBuilder, createReport);
    setRoot(pane);
  }

  /**
   * Starts the CreateSips thread and changes the scene to
   * CreationModalProcessing.
   *
   * @param outputFolder
   *          The output folder for the SIP exportation
   */
  public void startCreation(Path outputFolder, boolean exportAll, boolean exportItems, SIPNameBuilder sipNameBuilder,
    boolean createReport, IPHeader METSHeader) {
    setHeight(PROCESSING_HEIGHT);
    CreateSips creator = new CreateSips(outputFolder, sipNameBuilder.getSIPType(), exportItems, sipNameBuilder,
      createReport, METSHeader);
    CreationModalProcessing pane = new CreationModalProcessing(creator, this);
    setRoot(pane);

    Map<Sip, List<String>> sips;
    if (exportAll) {
      sips = RodaInApplication.getAllDescriptionObjects();
    } else {
      sips = RodaInApplication.getSelectedDescriptionObjects();
    }
    creator.start(sips);
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
    final UnsafeDouble dragDelta = new UnsafeDouble();
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
}
