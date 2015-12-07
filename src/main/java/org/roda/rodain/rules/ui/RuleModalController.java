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
import org.roda.rodain.rules.sip.TemplateType;
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

  private RuleModalController() {

  }

  /**
   * Creates the scene to show the modal window with the options to create a new
   * Rule.
   * 
   * @param primStage
   *          The main stage of the application.
   * @param source
   *          The set of items chosen by the user to create the new association.
   * @param schemaNode
   *          The destination of the SIPs that will be created.
   */
  public static void newAssociation(final Stage primStage, Set<SourceTreeItem> source, SchemaNode schemaNode) {
    if (stage == null)
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

  /**
   * Confirms the creation of a new Rule.
   *
   * <p>
   * The method starts by getting the association and metadata types chosen by
   * the user. Then creates a new Rule using that information and, if it
   * applies, the options from each chosen type (file names, maximum folder
   * depth, etc.).
   * </p>
   * <p>
   * The next step is to create the tree visitor to walk the files tree and add
   * it to the queue of visitors. Finally, notifies the other components of the
   * interface that a new association has been created and they need to adapt to
   * it.
   * </p>
   */
  public static void confirm() {
    stage.close();
    try {
      RuleTypes assocType = pane.getAssociationType();
      int level = 0;
      if (assocType == RuleTypes.SIP_PER_FOLDER)
        level = pane.getLevel();
      MetadataTypes metaType = pane.getMetadataType();
      Path metadataPath = null;
      TemplateType templateType = null;
      switch (metaType) {
        case SAME_DIRECTORY:
          metadataPath = pane.getSameDir();
          break;
        case DIFF_DIRECTORY:
          metadataPath = pane.getDiffDir();
          break;
        case SINGLE_FILE:
          metadataPath = pane.getFromFile();
          break;
        case TEMPLATE:
          templateType = pane.getTemplate();
          break;
        default:
          break;
      }
      Rule rule = new Rule(sourceSet, assocType, level, metadataPath, templateType, metaType);
      rule.addObserver(schema);

      TreeVisitor visitor = rule.apply();

      // create set with the selected paths
      Set<String> sourcePaths = new HashSet<>();
      for (SourceTreeItem sti : sourceSet) {
        sourcePaths.add(sti.getPath());
        rule.addObserver(sti);
      }

      visitors.add(sourceSet, sourcePaths, visitor);

      schema.addRule(rule);
      Main.inspectionNotifyChanged();
    } catch (Exception e) {
      log.debug("Exception in confirm rule", e);
    }
  }

  /**
   * Closes the stage of the modal window.
   */
  public static void cancel() {
    stage.close();
  }
}
