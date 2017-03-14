package org.roda.rodain.ui.inspection;

import java.io.IOException;

import org.controlsfx.control.PopOver;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.ui.utils.FontAwesomeImageCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-02-2016.
 */
public class ValidationPopOver extends PopOver {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationPopOver.class.getName());
  private Image loadingGif;

  /**
   * Creates a new ValidationPopOver object.
   */
  public ValidationPopOver() {
    super();

    try {
      if (loadingGif == null)
        loadingGif = new Image(ClassLoader.getSystemResource(Constants.RSC_LOADING_GIF).openStream());
    } catch (IOException e) {
      LOGGER.error("Error reading loading GIF", e);
    }

    setDetachable(false);
    setArrowLocation(ArrowLocation.TOP_RIGHT);
    setContentNode(createLoadingNode());
  }

  private Node createSuccessNode() {
    HBox content = new HBox(10);
    content.setPadding(new Insets(5, 15, 5, 15));
    content.setAlignment(Pos.CENTER);
    HBox.setHgrow(content, Priority.ALWAYS);
    Label title = new Label(I18n.t(Constants.I18N_VALID_METADATA));
    title.setStyle(Constants.CSS_FX_FONT_SIZE_16PX);
    ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.CHECK, Color.GREEN, 32));
    content.getChildren().addAll(title, iv);
    return content;
  }

  private Node createErrorNode(String message) {
    VBox content = new VBox(5);
    content.setAlignment(Pos.CENTER);
    content.setMaxWidth(500);
    content.setPadding(new Insets(5, 5, 5, 5));
    TextArea text = new TextArea(message);
    text.setEditable(false);
    text.setWrapText(true);

    HBox titleBox = new HBox(10);
    titleBox.setAlignment(Pos.CENTER);
    HBox.setHgrow(titleBox, Priority.ALWAYS);
    Label title = new Label(I18n.t(Constants.I18N_INVALID_METADATA));
    title.setStyle(Constants.CSS_FX_FONT_SIZE_16PX);
    ImageView iv = new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.TIMES, Color.RED, 32));
    titleBox.getChildren().addAll(title, iv);

    content.getChildren().addAll(titleBox, text);
    return content;
  }

  private Node createLoadingNode() {
    HBox content = new HBox();
    content.setPadding(new Insets(5, 5, 5, 5));
    ImageView iv = new ImageView(loadingGif);
    content.getChildren().add(iv);
    return content;
  }

  /**
   * Updates the content of the PopOver.
   * 
   * @param state
   *          The state of the task associated with the PopOver.
   * @param message
   *          The message to be displayed.
   */
  public void updateContent(boolean state, String message) {
    if (state) {
      setContentNode(createSuccessNode());
    } else {
      setContentNode(createErrorNode(message));
    }
  }
}
