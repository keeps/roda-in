package rules.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class Rule extends BorderPane {

    public Rule(){
        super();
        setStyle("-fx-border-color: black;");
        setPadding(new Insets(10, 10, 10, 10));

        createTop();
        createCenter();
        createBottom();
    }

    private void createTop(){

    }

    private void createCenter(){
        GridPane gridCenter = new GridPane();
        gridCenter.setHgap(10);

        ToggleGroup group = new ToggleGroup();

        RadioButton porFicheiro = new RadioButton("1 SIP por ficheiro");
        porFicheiro.setToggleGroup(group);
        porFicheiro.setSelected(true);

        RadioButton porPasta = new RadioButton("1 SIP por pasta até ao nível");
        porPasta.setToggleGroup(group);

        ObservableList<Integer> options = FXCollections.observableArrayList(0,1,2,3,4,5,6,7,8,9,10);
        ComboBox<Integer> level = new ComboBox<Integer>(options);
        level.setValue(3);

        gridCenter.add(porFicheiro, 0, 1);
        gridCenter.add(porPasta, 0, 2);
        gridCenter.add(level, 1, 2);

        setCenter(gridCenter);
    }

    private void createBottom(){
        Button remove = new Button("Remove");
        Button apply = new Button("Apply");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox buttons = new HBox();
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.TOP_RIGHT);
        buttons.getChildren().addAll(remove, space, apply);

        setBottom(buttons);
    }

}
