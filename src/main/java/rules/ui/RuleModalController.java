package rules.ui;

import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import rules.MetadataTypes;
import rules.Rule;
import rules.RuleTypes;
import rules.VisitorStack;
import schema.ui.SchemaNode;
import source.ui.items.SourceTreeItem;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Created by adrapereira on 19-10-2015.
 */
public class RuleModalController implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RuleModalController.class.getName());
    private static RuleModalStage stage;
    private static RuleModalPane pane;
    private static Set<SourceTreeItem> sourceSet;
    private static SchemaNode schema;

    private static VisitorStack visitors = new VisitorStack();

    private RuleModalController(){
        visitors.addObserver(this);
    }

    public static void newAssociation(Stage primStage, Set<SourceTreeItem> source, SchemaNode schemaNode){
        stage = new RuleModalStage(primStage, source, schemaNode);
        pane = new RuleModalPane(primStage, source, schemaNode);
        sourceSet = source;
        schema = schemaNode;

        stage.setRoot(pane);
    }

    public static void confirm(){
        stage.close();
        try {
            RuleTypes assocType = pane.getAssociationType();
            int level = 0;
            if(assocType == RuleTypes.SIPPERFOLDER)
                level = pane.getLevel();
            MetadataTypes metaType = pane.getMetadataType();
            String metadata = null;
            switch (metaType){
                case SAMEDIRECTORY:
                    metadata = pane.getSameDir();
                    break;
                case DIFFDIRECTORY:
                    metadata = pane.getDiffDir();
                    break;
                case SINGLEFILE:
                    metadata = pane.getFromFile();
                    break;
                default: break;
            }
            Rule rule = new Rule(sourceSet, assocType, level, metadata, metaType);
            schema.addRule(rule);

        } catch (Exception e) {
            log.debug(e.toString());
        }
    }

    public static void cancel(){
        stage.close();
    }

    @Override
    public void update(Observable o, Object arg) {
        log.info("Update");
    }
}
