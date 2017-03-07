package org.roda.rodain.ui.rules.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-11-2015.
 */
public class HBoxCell extends HBox {

  /**
   * Creates a new HBoxCell using the data received as parameter.
   *
   * @param icon
   *          The icon to be used on the cell.
   * @param title
   *          The title of the cell.
   * @param description
   *          The description of the cell.
   * @param options
   *          A node with options to be included at the bottom of the cell. This
   *          can be used to add buttons, combo boxes, etc.
   */
  public HBoxCell(String id, String icon, String title, String description, Node options) {
    super(20);
    setPadding(new Insets(3, 3, 3, 3));
    setId(id);

    getStyleClass().add("cell");
    setAlignment(Pos.CENTER_LEFT);

    VBox rightBox = new VBox(5);
    Label lTitle = new Label(title);
    lTitle.getStyleClass().add("title");
    HBox.setHgrow(rightBox, Priority.ALWAYS);

    Label lDescription = new Label(description);
    lDescription.getStyleClass().add("description");
    lDescription.setWrapText(true);
    lDescription.setMaxWidth(675);

    rightBox.getChildren().addAll(lTitle, lDescription, options);

    Image imIcon = new Image(ClassLoader.getSystemResourceAsStream(icon));
    getChildren().addAll(new ImageView(imIcon), rightBox);
  }
}
