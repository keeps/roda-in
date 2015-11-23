package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.roda.rodain.creation.CreateSips;
import org.roda.rodain.creation.SipTypes;
import org.roda.rodain.creation.ui.CreationModalPane;
import org.roda.rodain.creation.ui.CreationModalStage;
import org.roda.rodain.utils.FontAwesomeImageCreator;

import java.io.File;
import java.nio.file.Path;


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
