package rodain.rules.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import rodain.core.Main;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import rodain.inspection.LoadingPane;
import rodain.rules.MetadataTypes;
import rodain.rules.Rule;
import rodain.rules.RuleTypes;
import rodain.rules.VisitorStack;
import rodain.schema.ui.SchemaNode;
import rodain.source.ui.items.SourceTreeItem;
import rodain.utils.TreeVisitor;

import java.util.HashSet;
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

    private static LoadingPane loadingPane;

    private static VisitorStack visitors = new VisitorStack();

    private RuleModalController(){
        visitors.addObserver(this);
    }

    public static void newAssociation(final Stage primStage, Set<SourceTreeItem> source, SchemaNode schemaNode){
        if(stage == null)
            stage = new RuleModalStage(primStage);
        loadingPane = new LoadingPane(source, schemaNode);
        stage.setRoot(loadingPane);

        sourceSet = source;
        schema = schemaNode;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pane = new RuleModalPane(primStage, sourceSet, schema);
                return null;
            }
        };

        // After everything is loaded, we add all the items to the TreeView at once.
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.setRoot(pane);
                    }
                });
            }
        });

        new Thread(task).start();
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
            //rule.addObserver(this);
            rule.addObserver(schema);
            TreeVisitor visitor = rule.apply();

            //create set with the selected paths
            Set<String> sourcePaths = new HashSet<>();
            for(SourceTreeItem sti: sourceSet)
                sourcePaths.add(sti.getPath());
            visitors.add(sourcePaths, visitor);

            schema.addRule(rule);
            Main.mapSelected(rule.getId());
        } catch (Exception e) {
            log.debug("" + e);
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
