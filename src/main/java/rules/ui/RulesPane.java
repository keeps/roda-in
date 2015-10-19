package rules.ui;

import java.util.HashSet;
import java.util.Set;

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

import rules.Rule;
import rules.RuleTypes;
import rules.VisitorStack;
import schema.ui.SchemaNode;
import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeItem;
import core.Footer;
import core.Main;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class RulesPane extends BorderPane {
    private HBox createRule;
    private static ListView<RuleComponent> listView;
    private VisitorStack visitors = new VisitorStack();

    public RulesPane(Stage stage){
        listView = new ListView<>();
        createCreateRule();

        this.setTop(createRule);
        this.setCenter(listView);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.33));
    }

    private void createCreateRule(){
        Label title = new Label("Mapping Rules");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        createRule = new HBox();
        createRule.setPadding(new Insets(10, 10, 10, 10));
        createRule.setSpacing(10);
        createRule.setAlignment(Pos.TOP_LEFT);
        createRule.getChildren().add(title);

    }

    public Set<Rule> getRules(){
        HashSet<Rule> rules = new HashSet<>();
        for(RuleComponent rc: listView.getItems()) {
            // create new Rule objects to avoid interfering with the existing ones
            SourceTreeDirectory source = rc.getRule().getSource();
            RuleTypes type = rc.getType();
            int level = rc.getLevel();
            rules.add(new Rule(source, type, level));
        }
        return rules;
    }
    public void removeChild(RuleComponent rule){
        listView.getItems().remove(rule);
    }

    /* TEMP !!!!!!
    * TODO
    * */
    public static void addChild(RuleComponent comp){
        listView.getItems().add(comp);
    }
}
