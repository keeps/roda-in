package org.roda.rodain.ui.rules.ui;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Constants.RuleType;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.creators.SipPreviewCreator;
import org.roda.rodain.core.utils.TreeVisitor;
import org.roda.rodain.core.utils.WalkFileTree;
import org.roda.rodain.ui.ModalStage;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.rules.Rule;
import org.roda.rodain.ui.rules.VisitorStack;
import org.roda.rodain.ui.schema.ui.SchemaNode;
import org.roda.rodain.ui.source.items.SourceTreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19-10-2015.
 */
public class RuleModalController {
  private static final Logger LOGGER = LoggerFactory.getLogger(RuleModalController.class.getName());
  private static ModalStage stage;
  private static RuleModalPane pane;
  private static Set<SourceTreeItem> sourceSet;
  private static SchemaNode schema;

  private static VisitorStack visitors = new VisitorStack();

  private RuleModalController() {
    // do nothing
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
      stage = new ModalStage(primStage);
    stage.setWidth(800);
    stage.setHeight(580);

    LoadingPane loadingPane = new LoadingPane(schemaNode);
    stage.setRoot(loadingPane, false);

    sourceSet = source;
    schema = schemaNode;

    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        pane = new RuleModalPane(primStage, sourceSet, schema);
        return null;
      }
    };

    task.setOnSucceeded(event -> Platform.runLater(() -> stage.setRoot(pane, false)));

    new Thread(task).start();
  }

  /**
   * Confirms the creation of a new Rule.
   * <p/>
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
    try {
      RuleType assocType = pane.getAssociationType();
      MetadataOption metadataOption = pane.getMetadataOption();
      Path metadataPath = null;
      String templateType = null;
      String metadataVersion = null;
      String metadataType = null;
      switch (metadataOption) {
        case DIFF_DIRECTORY:
          metadataPath = pane.getDiffDir();
          templateType = pane.getMetadataTypeDiffFolder();
          metadataType = ConfigurationManager.getMetadataConfig(templateType + Constants.CONF_K_SUFIX_TYPE);
          break;
        case SINGLE_FILE:
          metadataPath = pane.getFromFile();
          templateType = pane.getMetadataTypeSingleFile();
          metadataType = ConfigurationManager.getMetadataConfig(templateType + Constants.CONF_K_SUFIX_TYPE);
          break;
        case SAME_DIRECTORY:
          templateType = pane.getSameFolderPattern();
          metadataType = pane.getMetadataTypeSameFolder();
          break;
        case TEMPLATE:
          String template = pane.getTemplate();
          if (template != null) {
            String[] splitted = template.split(Constants.MISC_METADATA_SEP);
            templateType = splitted[0];
            metadataType = splitted[1];
            metadataVersion = splitted.length == 3 ? splitted[2] : null;
          }
          break;
        default:
          break;
      }

      if (metadataType != null && metadataVersion == null) {
        metadataVersion = ConfigurationManager.getMetadataConfig(templateType + Constants.CONF_K_SUFIX_VERSION);
      }

      Rule rule = new Rule(sourceSet, assocType, metadataPath, templateType, metadataOption, metadataType,
        metadataVersion, schema.getDob().getId());
      rule.addObserver(schema);

      TreeVisitor visitor = rule.apply();

      // create set with the selected paths
      Set<String> sourcePaths = new HashSet<>();
      for (SourceTreeItem sti : sourceSet) {
        sourcePaths.add(sti.getPath());
        rule.addObserver(sti);
      }

      WalkFileTree fileWalker = visitors.add(sourcePaths, visitor);
      schema.addRule(rule);

      Platform.runLater(() -> {
        RuleModalProcessing processing = new RuleModalProcessing((SipPreviewCreator) visitor, visitor, visitors,
          fileWalker);
        stage.setRoot(processing, false);
        stage.setHeight(180);
        stage.setWidth(400);
        stage.centerOnScreen();
        RodaInApplication.getSchemePane().showTree();
      });
    } catch (Exception e) {
      LOGGER.error("Exception in confirm rule", e);
    }
  }

  /**
   * Creates a new RuleModalRemoving pane and sets the stage's root scene as
   * that pane.
   *
   * @param r
   *          The rule to be removed
   * @see RuleModalRemoving
   */
  public static void removeRule(Rule r) {
    RuleModalRemoving removing;
    if (stage.isShowing() && stage.getScene().getRoot() instanceof RuleModalRemoving) {
      removing = (RuleModalRemoving) stage.getScene().getRoot();
      r.addObserver(removing);
    } else {
      removing = new RuleModalRemoving();
      r.addObserver(removing);
      stage.setRoot(removing, false);
      stage.setHeight(120);
      stage.setWidth(400);
      stage.centerOnScreen();
    }
    removing.addRule(r);
  }

  /**
   * Creates a new RuleModalRemoving pane and sets the stage's root scene as
   * that pane.
   *
   * @param sip
   *          The SIP to be removed
   * @see RuleModalRemoving
   */
  public static void removeSipPreview(SipPreview sip) {
    RuleModalRemoving removing;
    if (stage.isShowing() && stage.getScene().getRoot() instanceof RuleModalRemoving) {
      removing = (RuleModalRemoving) stage.getScene().getRoot();
      sip.addObserver(removing);
    } else {
      removing = new RuleModalRemoving();
      sip.addObserver(removing);
      stage.setRoot(removing, false);
      stage.setHeight(120);
      stage.setWidth(400);
      stage.centerOnScreen();
    }
    removing.addSIP(sip);
  }

  /**
   * Closes the stage of the modal window.
   */
  public static void cancel() {
    Platform.runLater(() -> stage.close());
  }
}
