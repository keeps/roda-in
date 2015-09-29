package rules.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import rules.Rule;
import schema.ui.SchemaNode;
import source.ui.items.SourceTreeDirectory;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class RuleComponent extends BorderPane {
    private static final Logger log = Logger.getLogger(RuleComponent.class.getName());
    private Rule rule;

    public RuleComponent(SourceTreeDirectory sourcePath, SchemaNode descriptionObject){
        super();
        setStyle("-fx-border-color: gray;");
        setPadding(new Insets(0, 10, 0, 10));

        rule = new Rule(sourcePath, descriptionObject);

        createTop();
        createCenter();
        createBottom();
    }

    private void createTop(){
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0, 0, 10, 0));

        HBox hbox = new HBox();
        HBox.setHgrow(hbox, Priority.ALWAYS);
        hbox.setStyle("-fx-background-color: lightgray;");
        hbox.setPadding(new Insets(5, 2, 2, 2));
        pane.getChildren().add(hbox);

        Label source = new Label(rule.getFolderName());
        source.setMinHeight(24);
        source.setFont(new Font("System", 14));
        source.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));

        Label descObj = new Label(rule.getDescObjName());
        descObj.setMinHeight(24);
        descObj.setFont(new Font("System", 14));
        descObj.setGraphic(new ImageView(rule.getSchemaNode().getImage()));
        descObj.setContentDisplay(ContentDisplay.RIGHT);
        descObj.setTextAlignment(TextAlignment.LEFT);

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        hbox.getChildren().addAll(source, space, descObj);

        setTop(pane);
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
