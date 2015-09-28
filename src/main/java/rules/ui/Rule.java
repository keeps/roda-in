package rules.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class Rule extends VBox {

    public Rule(){
        Label lab = new Label("Teste - Linha 1\nTeste - Linha 2");
        HBox.setHgrow(lab, Priority.ALWAYS);

        this.getChildren().addAll(lab);
    }

}
