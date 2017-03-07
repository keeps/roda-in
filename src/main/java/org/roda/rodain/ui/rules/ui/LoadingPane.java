package org.roda.rodain.ui.rules.ui;

import java.io.IOException;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.ui.schema.ui.SchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 27-10-2015.
 */
public class LoadingPane extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadingPane.class.getName());
  private static Image loadingGif;
  private SchemaNode schema;

  /**
   * Creates a new LoadingPane object
   *
   * @param schemaNode
   *          The SchemaNode to be used to set the title of the pane
   */
  public LoadingPane(SchemaNode schemaNode) {
    super();
    this.schema = schemaNode;
    createTop();
    getStyleClass().add("modal");

    HBox centerBox = new HBox();
    centerBox.setAlignment(Pos.CENTER);
    try {
      if (loadingGif == null) {
        loadingGif = new Image(ClassLoader.getSystemResource(Constants.RSC_LOADING_GIF).openStream());
      }
      centerBox.getChildren().add(new ImageView(loadingGif));
    } catch (IOException e) {
      LOGGER.error("Error reading loading GIF", e);
    }
    setCenter(centerBox);
  }

  private void createTop() {
    StackPane pane = new StackPane();
    pane.setPadding(new Insets(0, 0, 10, 0));

    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER_LEFT);
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(10, 10, 10, 10));
    pane.getChildren().add(box);

    Label title = new Label(I18n.t(Constants.I18N_LOADINGPANE_CREATE_ASSOCIATION) + Constants.MISC_DOUBLE_QUOTE_W_SPACE
      + schema.getDob().getTitle() + Constants.MISC_DOUBLE_QUOTE);
    title.setId("title");

    box.getChildren().add(title);

    setTop(pane);
  }
}
