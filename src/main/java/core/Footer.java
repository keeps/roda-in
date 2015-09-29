package core;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class Footer extends HBox{
    private static final Logger log = Logger.getLogger(Footer.class.getName());
    public static final Label status = new Label();

    public Footer(){
        super();
        Button btn = new Button("Create SIPs");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        this.setPadding(new Insets(10, 10, 10, 10));
        this.setSpacing(10);
        this.setAlignment(Pos.TOP_RIGHT);
        this.getChildren().addAll(status, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Footer.setStatus("Carregou no \"Create SIPs\"");
            }
        });
    }

    public static void setStatus(String st){
        status.setText(st);
    }


}
