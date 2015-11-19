package org.roda.rodain.core;

import java.io.File;
import java.nio.file.Path;

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
import org.roda.rodain.utils.FontAwesomeImageCreator;


/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class Footer extends HBox {
    public static final Label status = new Label();
    private Stage stage;
    static Button btn;

    public Footer(Stage st){
        super();
        this.stage = st;
        btn = new Button("Create SIPs");
        Image icon = FontAwesomeImageCreator.im_w_chevron_right;
        btn.setGraphic(new ImageView(icon));
        btn.setGraphicTextGap(10);
        btn.setContentDisplay(ContentDisplay.RIGHT);

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        this.setPadding(new Insets(5, 5, 5, 5));
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(status, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null)
                    return;
                Path path = selectedDirectory.toPath();
                Footer.setStatus("Creating BagIts...");
                btn.setDisable(true);
                CreateSips creator = new CreateSips(path, SipTypes.BAGIT);
                creator.start();
            }
        });
    }

    public static void setStatus(final String st){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                status.setText(st);
            }
        });
    }

    public static void activeButton(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btn.setDisable(false);
            }
        });
    }

}
