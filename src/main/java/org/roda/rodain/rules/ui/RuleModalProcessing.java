package org.roda.rodain.rules.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.roda.rodain.rules.VisitorStack;
import org.roda.rodain.rules.VisitorState;
import org.roda.rodain.rules.sip.SipPreviewCreator;
import org.roda.rodain.utils.TreeVisitor;
import org.roda.rodain.utils.WalkFileTree;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class RuleModalProcessing extends BorderPane {
  private SipPreviewCreator creator;
  private TreeVisitor visitor;
  private VisitorStack visitorStack;
  private WalkFileTree fileWalker;

  // top
  private Label sipsCreatedLabel, filesProcessedLabel;
  private String sipsCreatedFormat = "Created %d SIP previews";
  private String filesProcessedFormat = "Processed %d directories and %d files";

  private Timer timer;


  /**
   * Creates a new RuleModalProcessing object, that indicates the progress of the SipPreview creation.
   *
   * @param creator      The SipPreviewCreator object
   * @param visitor      The TreeVisitor object
   * @param visitorStack The VisitorStack, so the process can be cancelled
   * @param fileWalker   The WalkFileTree object, to get the processed files and directories
   */
  public RuleModalProcessing(SipPreviewCreator creator, TreeVisitor visitor, VisitorStack visitorStack, WalkFileTree fileWalker) {
    this.creator = creator;
    this.visitor = visitor;
    this.visitorStack = visitorStack;
    this.fileWalker = fileWalker;

    getStyleClass().add("sipcreator");

    createTop();
    createCenter();
    createBottom();

    createUpdateTask();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setAlignment(Pos.CENTER);
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(10, 10, 10, 0));

    Label title = new Label("Creating SIP previews");
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox(5);
    center.setPadding(new Insets(0, 10, 10, 10));
    center.setAlignment(Pos.CENTER_LEFT);

    ProgressBar progress = new ProgressBar();
    progress.setPadding(new Insets(5, 0, 10, 0));
    progress.setPrefSize(380, 25);

    sipsCreatedLabel = new Label();
    filesProcessedLabel = new Label();

    center.getChildren().addAll(progress, sipsCreatedLabel, filesProcessedLabel);
    setCenter(center);
  }

  private void createBottom() {
    HBox bottom = new HBox();
    bottom.setPadding(new Insets(0, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_RIGHT);
    Button cancel = new Button("Cancel");
    cancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        visitorStack.cancel(visitor);
        close();
      }
    });

    bottom.getChildren().add(cancel);
    setBottom(bottom);
  }

  private void createUpdateTask() {
    TimerTask updater = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            int sips = creator.getCount();
            int files = fileWalker.getProcessedFiles();
            int dirs = fileWalker.getProcessedDirs();

            sipsCreatedLabel.setText(String.format(sipsCreatedFormat, sips));
            filesProcessedLabel.setText(String.format(filesProcessedFormat, dirs, files));

            if (visitorStack.getState(visitor.getId()) == VisitorState.VISITOR_DONE) {
              close();
            }
          }
        });
      }
    };

    timer = new Timer();
    timer.schedule(updater, 0, 500);
  }

  private void close() {
    timer.cancel();
    RuleModalController.cancel();
  }
}
