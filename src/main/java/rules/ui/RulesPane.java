package rules.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import schema.ui.SchemaNode;
import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeItem;
import core.Footer;
import core.Main;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class RulesPane extends BorderPane {
    private static final Logger log = Logger.getLogger(RulesPane.class.getName());
    private HBox createRule;
    private ListView<RuleComponent> listView;

    public RulesPane(Stage stage){
        createCreateRule();
        createListView();

        this.setTop(createRule);
        this.setCenter(listView);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
    }

    private void createCreateRule(){
        Button btn = new Button("Create Rule");
        Label title = new Label("Mapping Rules");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        createRule = new HBox();
        createRule.setPadding(new Insets(10, 10, 10, 10));
        createRule.setSpacing(10);
        createRule.setAlignment(Pos.TOP_RIGHT);
        createRule.getChildren().addAll(title, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                SourceTreeItem source = Main.getSourceSelectedItem();
                SchemaNode descObj = Main.getSchemaSelectedItem();
                if(source != null && descObj != null) { //both trees need to have 1 element selected
                    if(source instanceof SourceTreeDirectory) { //the source needs to be a directory
                        RuleComponent ruleC = new RuleComponent((SourceTreeDirectory) source, descObj);
                        listView.getItems().add(ruleC);
                    }
                }
                Footer.setStatus("Carregou no \"Create Rule\": " + source + " <-> " + descObj);
            }
        });
    }

    private void createListView(){
        listView = new ListView<RuleComponent>();

        //Disable selection in listview
        listView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        listView.getSelectionModel().select(-1);
                    }
                });
            }
        });

    }
}
