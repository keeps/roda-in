package org.roda.rodain.rules.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-11-2015.
 */
public class HBoxCell extends HBox {

  public HBoxCell(String icon, String title, String description, Node options) {
    super(20);

    getStyleClass().add("cell");
    setAlignment(Pos.CENTER_LEFT);

    VBox rightBox = new VBox(5);
    Label lTitle = new Label(title);
    lTitle.getStyleClass().add("title");

    Label lDescription = new Label(description);
    lDescription.getStyleClass().add("description");
    lDescription.setWrapText(true);
    lDescription.setMaxWidth(675);

    rightBox.getChildren().addAll(lTitle, lDescription, options);

    Image imIcon = new Image(ClassLoader.getSystemResourceAsStream(icon));
    getChildren().addAll(new ImageView(imIcon), rightBox);
  }
}
