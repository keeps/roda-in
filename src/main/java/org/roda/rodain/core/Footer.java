package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class Footer extends HBox {
    public static final Label status = new Label();

    public Footer(){
        super();

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        this.setPadding(new Insets(5, 5, 5, 5));
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().add(status);
    }

    public static void setStatus(final String st){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                status.setText(st);
            }
        });
    }
}
