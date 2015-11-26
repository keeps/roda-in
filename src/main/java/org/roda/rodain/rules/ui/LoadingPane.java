package org.roda.rodain.rules.ui;

import java.io.IOException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.roda.rodain.schema.ui.SchemaNode;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 27-10-2015.
 */
public class LoadingPane extends BorderPane {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(LoadingPane.class.getName());
  private static Image loadingGif;
  private SchemaNode schema;

  public LoadingPane(SchemaNode schemaNode) {
    super();
    this.schema = schemaNode;
    createTop();
    getStyleClass().add("modal");

    HBox centerBox = new HBox();
    centerBox.setAlignment(Pos.CENTER);
    try {
      if (loadingGif == null)
        loadingGif = new Image(ClassLoader.getSystemResource("loading.GIF").openStream());
      centerBox.getChildren().add(new ImageView(loadingGif));
    } catch (IOException e) {
      log.error("Error reading loading GIF", e);
    }
    setCenter(centerBox);
  }

  private void createTop() {
    StackPane pane = new StackPane();
    pane.setPadding(new Insets(0, 0, 10, 0));

    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER);
    box.getStyleClass().add("hbox");
    box.setPadding(new Insets(5, 5, 5, 5));
    pane.getChildren().add(box);

    Label title = new Label("Create association to \"" + schema.getDob().getTitle() + "\"");
    title.setId("title");

    box.getChildren().add(title);

    setTop(pane);
  }
}
