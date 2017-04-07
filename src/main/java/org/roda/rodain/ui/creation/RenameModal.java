package org.roda.rodain.ui.creation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.inspection.trees.SipDataTreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class RenameModal extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(RenameModal.class.getName());
  private static final int MAX_COUNT_ERROR_MESSAGES = 5;
  private static final long MAX_TIME_ERROR_MESSAGES = 10000;
  private static RenameModalStage stage;
  private String representationName;

  // center
  private TextField name;
  private Label errorLabel;

  private static Deque<Long> errorMessages;
  private static boolean displayErrorMessage;

  /**
   * Creates a pane to show the progress of the SIP exportation.
   *
   * @param creatorArg
   *          The SIP creator object
   * @param stage
   *          The stage of the pane
   */
  public RenameModal(String representationName, RenameModalStage stage) {
    RenameModal.stage = stage;
    this.representationName = representationName;
    errorMessages = new ArrayDeque<>();
    displayErrorMessage = true;

    getStyleClass().add(Constants.CSS_SIPCREATOR);

    createTop();
    createCenter();
    createBottom();

    stage.close();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setAlignment(Pos.CENTER);

    Label title = new Label(I18n.t(Constants.I18N_RENAME_REPRESENTATION));
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox();
    center.setAlignment(Pos.CENTER_LEFT);
    center.setPadding(new Insets(0, 10, 10, 10));

    name = new TextField();
    name.setText(representationName);

    name.setOnKeyReleased(new EventHandler() {
      @Override
      public void handle(Event event) {
        errorLabel.setVisible(false);
      }
    });

    errorLabel = new Label(I18n.t(Constants.RENAME_REPRESENTATION_ALREADY_EXISTS));
    errorLabel.setVisible(false);
    errorLabel.setPadding(new Insets(5, 0, 0, 0));
    errorLabel.setTextFill(Color.web("#ff0033"));

    center.getChildren().addAll(name, errorLabel);
    setCenter(center);
  }

  private void createBottom() {
    HBox bottom = new HBox();
    bottom.setPadding(new Insets(0, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_LEFT);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Button cancel = new Button(I18n.t(Constants.I18N_CANCEL));
    cancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        stage.close();
      }
    });

    Button confirm = new Button(I18n.t(Constants.I18N_CONFIRM));
    confirm.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        if ("".equals(name.getText())) {
          return;
        } else {
          if (!representationName.equals(name.getText())) {
            Set<Sip> sips = RodaInApplication.getSelectedDescriptionObjects().keySet();
            SipRepresentation representation = null;
            boolean errored = false;

            for (Sip sip : RodaInApplication.getSelectedDescriptionObjects().keySet()) {
              if (sip instanceof SipPreview) {
                for (SipRepresentation sipRepresentation : ((SipPreview) sip).getRepresentations()) {
                  if (representationName.equals(sipRepresentation.getName())) {
                    representation = sipRepresentation;
                  }

                  if (name.getText().equals(sipRepresentation.getName())) {
                    errored = true;
                    errorLabel.setVisible(true);
                  }
                }
              }
            }

            if (!errored && representation != null) {
              SipDataTreeView.getRepresentationItem().setValue(name.getText());
              representation.setName(name.getText());
              stage.close();
            }
          } else {
            stage.close();
          }
        }
      }
    });

    bottom.getChildren().addAll(cancel, space, confirm);
    setBottom(bottom);
  }
}
