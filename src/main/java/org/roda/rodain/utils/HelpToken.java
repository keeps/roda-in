package org.roda.rodain.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;


/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-07-2016.
 */
public class HelpToken extends Button {

  public HelpToken(String content, Color color, PopOver.ArrowLocation arrowLocation){
    super("");
    getStyleClass().add("helpToken");
    // Set the question mark icon
    Platform.runLater(() -> setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.QUESTION, color))));

    // Create the label which will have the help text
    Label contentHelp = new Label(content);
    contentHelp.setWrapText(true);

    // Create the popover with the help text that will be displayed when the user clicks the button
    PopOver editPopOver = new PopOver();
    editPopOver.setDetachable(false);
    editPopOver.setArrowLocation(arrowLocation);

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add("helpTokenPopOver");
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);
    popOverContent.getChildren().add(contentHelp);
    editPopOver.setContentNode(popOverContent);

    setOnAction(event -> editPopOver.show(this));
  }

  public HelpToken(String content, PopOver.ArrowLocation arrowLocation){
    this(content, Color.BLACK, arrowLocation);
  }
}
