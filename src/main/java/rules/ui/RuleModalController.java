package rules.ui;

import javafx.stage.Stage;
import schema.ui.SchemaNode;
import source.ui.items.SourceTreeItem;

import java.util.Set;

/**
 * Created by adrapereira on 19-10-2015.
 */
public class RuleModalController {
    private static RuleModalStage stage;
    private static RuleModalPane pane;

    private RuleModalController(){
    }

    public static void newAssociation(Stage primStage, Set<SourceTreeItem> source, SchemaNode schema){
        stage = new RuleModalStage(primStage, source, schema);
        pane = new RuleModalPane(source, schema);

        stage.setRoot(pane);
    }

    public static void confirm(){

    }

    public static void cancel(){
        stage.close();
    }
}
