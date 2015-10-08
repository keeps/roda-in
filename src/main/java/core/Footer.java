package core;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import rules.Rule;
import rules.VisitorStack;
import utils.TreeVisitor;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;


/**
 * Created by adrapereira on 28-09-2015.
 */
public class Footer extends HBox {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Footer.class.getName());
    public static final Label status = new Label();
    private Stage stage;
    static Button btn;

    public Footer(Stage st){
        super();
        this.stage = st;
        btn = new Button("Create SIPs");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        this.setPadding(new Insets(10, 10, 10, 10));
        this.setSpacing(10);
        this.setAlignment(Pos.TOP_RIGHT);
        this.getChildren().addAll(status, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null) return;
                Path path = selectedDirectory.toPath();
                Footer.setStatus("Creating BagIts...");
                btn.setDisable(true);
                CreateBagits creator = new CreateBagits(path.toString());
                creator.start();
            }
        });
    }

    public static void setStatus(final String st){
        Platform.runLater(new Runnable() {
            public void run() {
                status.setText(st);
            }
        });
    }

    public static void activeButton(){
        Platform.runLater(new Runnable() {
            public void run() {
                btn.setDisable(false);
            }
        });
    }

}
