package org.roda.rodain.rules.ui;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import org.roda.rodain.core.Main;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.rules.VisitorStack;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.TreeVisitor;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19-10-2015.
 */
public class RuleModalController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RuleModalController.class.getName());
    private static RuleModalStage stage;
    private static RuleModalPane pane;
    private static Set<SourceTreeItem> sourceSet;
    private static SchemaNode schema;

    private static LoadingPane loadingPane;

    private static VisitorStack visitors = new VisitorStack();

    private RuleModalController(){

    }

    public static void newAssociation(final Stage primStage, Set<SourceTreeItem> source, SchemaNode schemaNode){
        if(stage == null)
            stage = new RuleModalStage(primStage);
        loadingPane = new LoadingPane(schemaNode);
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
            Path metadataPath = null;
            String metadataResource = null;
            switch (metaType){
                case SAMEDIRECTORY:
                    metadataPath = pane.getSameDir();
                    break;
                case DIFFDIRECTORY:
                    metadataPath = pane.getDiffDir();
                    break;
                case SINGLEFILE:
                    metadataPath = pane.getFromFile();
                    break;
                case NEWTEXT:
                    metadataResource = pane.getTemplate();
                default: break;
            }
            Rule rule = new Rule(sourceSet, assocType, level, metadataPath, metadataResource, metaType);
            rule.addObserver(schema);

            TreeVisitor visitor = rule.apply();

            //create set with the selected paths
            Set<String> sourcePaths = new HashSet<>();
            for(SourceTreeItem sti: sourceSet) {
                sourcePaths.add(sti.getPath());
                rule.addObserver(sti);
            }

            visitors.add(sourceSet, sourcePaths, visitor);

            schema.addRule(rule);
            Main.mapSelected(rule);
            Main.inspectionNotifyChanged();
        } catch (Exception e) {
            log.debug("Exception in confirm rule", e);
        }
    }

    public static void cancel(){
        stage.close();
    }
}
