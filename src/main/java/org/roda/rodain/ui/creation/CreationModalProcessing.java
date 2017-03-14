package org.roda.rodain.ui.creation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.creation.CreateSips;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.utils.OpenPathInExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreationModalProcessing extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreationModalProcessing.class.getName());
  private static final int MAX_COUNT_ERROR_MESSAGES = 5;
  private static final long MAX_TIME_ERROR_MESSAGES = 10000;
  private static CreateSips creator;
  private static CreationModalStage stage;

  // top
  private Label subtitleSuccess, subtitleError;
  private String subtitleFormat;
  private String etaFormatHour, etaFormatHours, etaFormatMinute, etaFormatMinutes, etaFormatLessMin, etaFormatLess30;
  // center
  private ProgressBar progress;
  private Label sipName, sipAction, eta, etaLabel, elapsedTime;
  private HBox etaBox;
  private static Timer timer;

  private HBox finishedBox;
  private static Stack<Long> errorMessages;
  private static boolean displayErrorMessage;

  /**
   * Creates a pane to show the progress of the SIP exportation.
   *
   * @param creatorArg
   *          The SIP creator object
   * @param stage
   *          The stage of the pane
   */
  public CreationModalProcessing(CreateSips creatorArg, CreationModalStage stage) {
    creator = creatorArg;
    CreationModalProcessing.stage = stage;
    errorMessages = new Stack<>();
    displayErrorMessage = true;

    etaFormatHour = String.format("< %%d %s ", I18n.t(Constants.I18N_CREATIONMODALPROCESSING_HOUR));
    etaFormatHours = String.format("< %%d %s ", I18n.t(Constants.I18N_CREATIONMODALPROCESSING_HOURS));
    etaFormatMinute = String.format("%%d %s", I18n.t(Constants.I18N_CREATIONMODALPROCESSING_MINUTE));
    etaFormatMinutes = String.format("%%d %s", I18n.t(Constants.I18N_CREATIONMODALPROCESSING_MINUTES));
    etaFormatLessMin = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_LESS_MINUTE);
    etaFormatLess30 = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_LESS_SECONDS);

    subtitleFormat = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_SUBTITLE);

    getStyleClass().add("sipcreator");

    createTop();
    createCenter();
    createBottom();

    createUpdateTask();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.getStyleClass().add("hbox");
    top.setAlignment(Pos.CENTER);

    Label title = new Label(I18n.t(Constants.I18N_CREATIONMODALPREPARATION_CREATING_SIPS));
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox();
    center.setAlignment(Pos.CENTER_LEFT);
    center.setPadding(new Insets(0, 10, 10, 10));

    etaBox = new HBox(10);
    etaLabel = new Label(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_REMAINING));
    etaLabel.setMinWidth(70);
    etaLabel.getStyleClass().add("boldText");
    eta = new Label();
    etaBox.getChildren().addAll(etaLabel, eta);

    HBox subtitles = new HBox(5);
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    subtitleSuccess = new Label("");
    subtitleSuccess.setId("subtitle");

    subtitleError = new Label("");
    subtitleError.setId("subtitle");

    subtitles.getChildren().addAll(subtitleSuccess, space, subtitleError);

    progress = new ProgressBar();
    progress.setPadding(new Insets(5, 0, 10, 0));
    progress.setPrefSize(380, 25);

    HBox elapsed = new HBox(10);
    elapsed.maxWidth(380);
    Label lElapsed = new Label(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ELAPSED));
    lElapsed.getStyleClass().add("boldText");
    lElapsed.setMinWidth(70);
    elapsedTime = new Label("");
    elapsed.getChildren().addAll(lElapsed, elapsedTime);

    HBox sip = new HBox(10);
    sip.maxWidth(380);
    Label lName = new Label(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_CURRENT_SIP));
    lName.getStyleClass().add("boldText");
    lName.setMinWidth(70);
    sipName = new Label("");
    sip.getChildren().addAll(lName, sipName);

    HBox action = new HBox(10);
    Label lAction = new Label(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ACTION));
    lAction.getStyleClass().add("boldText");
    lAction.setMinWidth(70);
    sipAction = new Label("");
    action.getChildren().addAll(lAction, sipAction);

    center.getChildren().addAll(subtitles, progress, etaBox, elapsed, sip, action);
    setCenter(center);
  }

  private void createBottom() {
    HBox bottom = new HBox();
    bottom.setPadding(new Insets(0, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_LEFT);
    Button cancel = new Button(I18n.t(Constants.I18N_CANCEL));
    cancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        timer.cancel();
        creator.cancel();
        stage.close();
      }
    });

    bottom.getChildren().add(cancel);
    setBottom(bottom);

    finishedBox = new HBox(10);
    finishedBox.setPadding(new Insets(0, 10, 10, 10));
    finishedBox.setAlignment(Pos.CENTER_RIGHT);
    Button close = new Button(I18n.t(Constants.I18N_CLOSE));
    Button open = new Button(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_OPEN_FOLDER));

    close.setOnAction(event -> stage.close());
    open.setOnAction((event) -> OpenPathInExplorer.open(creator.getOutputPath()));

    finishedBox.getChildren().addAll(open, close);
  }

  private void createUpdateTask() {
    TimerTask updater = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            int created = creator.getCreatedSipsCount();
            int size = creator.getSipsCount();
            int errors = creator.getErrorCount();
            double etaDouble = creator.getTimeRemainingEstimate();
            long startedTime = creator.getStartedTime();
            updateETA(etaDouble);
            double prog = creator.getProgress();

            if (errors > 0) {
              subtitleError.setText(errors + I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ERRORS));
            }
            subtitleSuccess.setText(String.format(subtitleFormat, created, size, (int) (prog * 100)));
            progress.setProgress(prog);

            sipName.setText(creator.getSipName());
            sipAction.setText(creator.getAction());
            // format elapsed time
            long millis = System.currentTimeMillis() - startedTime;
            long second = (millis / 1000) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            elapsedTime.setText(String.format("%02d:%02d:%02d", hour, minute, second));

            // stop the timer when all the SIPs have been created
            if ((created + errors) == size) {
              eta.setText(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_FINISHED));
              progress.setProgress(100);
              finished();
            }
          }
        });
      }
    };

    timer = new Timer();
    timer.schedule(updater, 0, 200);
  }

  private void updateETA(double etaDouble) {
    if (etaDouble >= 0) {
      if (etaBox.getChildren().isEmpty()) {
        etaBox.getChildren().addAll(etaLabel, eta);
      }
      int second = (int) ((etaDouble / 1000) % 60);
      int minute = (int) ((etaDouble / (1000 * 60)) % 60);
      int hour = (int) ((etaDouble / (1000 * 60 * 60)) % 24);
      String result;
      if (hour > 0) {
        if (hour == 1)
          result = String.format(etaFormatHour, hour);
        else
          result = String.format(etaFormatHours, hour);
      } else if (minute > 0) {
        if (minute == 1)
          result = String.format(etaFormatMinute, minute);
        else
          result = String.format(etaFormatMinutes, minute);
      } else if (second > 30) {
        result = etaFormatLessMin;
      } else
        result = etaFormatLess30;
      eta.setText(result);
    } else {
      etaBox.getChildren().clear();
      eta.setText(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_IMPOSSIBLE_ESTIMATE));
    }
  }

  private void finished() {
    timer.cancel();
    setBottom(finishedBox);
  }

  /**
   * Shows an alert with the error message regarding an exception found when
   * exporting a SIP.
   * 
   * @param descriptionObject
   *          The SIP being exported when the exception was thrown
   * @param ex
   *          The thrown exception
   */
  public static void showError(Sip descriptionObject, Exception ex) {
    Platform.runLater(() -> {
      if(displayErrorMessage) {
        addErrorMessage();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(stage);
        alert.setTitle(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ALERT_TITLE));
        String header = String.format(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ALERT_HEADER), descriptionObject.getTitle());
        alert.setHeaderText(header);
        StringBuilder content = new StringBuilder(ex.getLocalizedMessage());
        content.append("\n");
        content.append(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_CAUSE));
        if (ex.getCause() != null) {
          content.append(": ").append(ex.getCause().getLocalizedMessage());
        }
        alert.setContentText(content.toString());
        alert.getDialogPane().setStyle(ConfigurationManager.getStyle("export.alert"));

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(ex.getMessage());
        for (StackTraceElement ste : ex.getStackTrace()) {
          pw.println("\t" + ste);
        }
        String exceptionText = sw.toString();

        Label label = new Label(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ALERT_STACK_TRACE));

        TextArea textArea = new TextArea(exceptionText);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.minWidthProperty().bind(alert.getDialogPane().widthProperty().subtract(20));
        textArea.maxWidthProperty().bind(alert.getDialogPane().widthProperty().subtract(20));

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        textArea.minHeightProperty().bind(expContent.heightProperty().subtract(20));
        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().minWidthProperty().bind(stage.widthProperty());
        alert.getDialogPane().minHeightProperty().bind(stage.heightProperty());

        // Without this setStyle the pane won't resize correctly. Black magic...
        alert.getDialogPane().setStyle(ConfigurationManager.getStyle("creationmodalprocessing.blackmagic"));

        alert.show();
        checkIfTooManyErrors();
      }
    });
  }

  private static void checkIfTooManyErrors(){
    long limit = System.currentTimeMillis() - MAX_TIME_ERROR_MESSAGES;
    boolean allAfterLimit = errorMessages.size() == MAX_COUNT_ERROR_MESSAGES &&
        errorMessages.stream().allMatch(messageTime -> messageTime > limit);
    if(allAfterLimit){
      displayErrorMessage = false;
      Alert dlg = new Alert(Alert.AlertType.INFORMATION);
      dlg.initStyle(StageStyle.UNDECORATED);
      dlg.setHeaderText(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ERROR_MESSAGES_STOPPED_HEADER));
      dlg.setTitle("");
      dlg.setContentText(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ERROR_MESSAGES_STOPPED_CONTENT));
      dlg.initModality(Modality.APPLICATION_MODAL);
      dlg.initOwner(stage);

      dlg.show();
    }
  }

  private static void addErrorMessage(){
    //If the stack is too big, remove elements until it's the right size.
    while (errorMessages.size() >= MAX_COUNT_ERROR_MESSAGES) {
      errorMessages.remove(0);
    }
    errorMessages.push(System.currentTimeMillis());
  }
}
