package org.roda.rodain.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.roda.rodain.ApplicationTestBase;
import org.roda.rodain.core.schema.ClassificationSchema;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.creation.CreationModalPreparation;
import org.roda.rodain.ui.inspection.InspectionPane;
import org.roda.rodain.ui.schema.ui.SchemaNode;
import org.roda.rodain.ui.schema.ui.SchemaPane;
import org.roda.rodain.ui.schema.ui.SipPreviewNode;
import org.roda.rodain.ui.source.FileExplorerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-12-2015.
 */
public class MainTest extends ApplicationTestBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class.getName());

  private static Path testDir, output;
  private SchemaPane schemaPane;
  private FileExplorerPane fileExplorer;
  private InspectionPane inspectionPane;
  private static RodaInApplication main;
  private Stage stage;

  @Override
  public void start(Stage stage) throws Exception {
    this.stage = stage;
    main = new RodaInApplication();
    main.start(stage);

    do {
      sleep(100);
      schemaPane = RodaInApplication.getSchemePane();
      fileExplorer = RodaInApplication.getFileExplorer();
      inspectionPane = RodaInApplication.getInspectionPane();
    } while (schemaPane == null || fileExplorer == null || inspectionPane == null);

    Path path = Paths.get("src/test/resources/plan_with_errors.json");
    InputStream stream = new FileInputStream(path.toFile());
    loadClassificationSchemeFromStream(stream);
  }

  /**
   * Creates a ClassificationSchema object from the InputStream and builds a
   * tree using it.
   *
   * @param stream
   *          The stream with the JSON file used to create the
   *          ClassificationSchema
   */
  private void loadClassificationSchemeFromStream(InputStream stream) {
    try {
      schemaPane.getRootNode().getChildren().clear();
      // create ObjectMapper instance
      ObjectMapper objectMapper = new ObjectMapper();
      // convert stream to object
      ClassificationSchema scheme = objectMapper.readValue(stream, ClassificationSchema.class);
      schemaPane.updateClassificationSchema(scheme);
    } catch (IOException e) {
      LOGGER.error("Error reading classification scheme from stream", e);
    }
  }

  @Before
  public void setUpBeforeClass() throws Exception {
    testDir = Utils.createFolderStructure();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    PathCollection.reset();
    main.stop();
  }

  @Test
  public void createNewClassificationPlanWithRemovals() {
    String node1 = "Node1";
    String node2 = "Node2";

    // waitUntilNodeAppearsAndClick(I18n.t(Constants.I18N_SCHEMAPANE_CREATE));

    push(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    clickOk();

    waitUntilNodeAppearsAndClick(I18n.t("SchemaPane.add"));
    waitUntilNodeAppearsAndClick(I18n.t("continue"));
    waitUntilNodeAppearsAndClick(".schemaNode");
    waitUntilNodeAppearsAndClick("#descObjTitle");

    overwriteText(node1);

    awaitCondition(() -> node1
      .equals(RodaInApplication.getSchemePane().getTreeView().getSelectionModel().getSelectedItem().getValue()), 2);

    waitUntilNodeAppearsAndClick(I18n.t("SchemaPane.add"));
    waitUntilNodeAppearsAndClick(I18n.t("continue"));
    waitUntilNodeAppearsAndClick(I18n.t("SchemaPane.newNode"));
    waitUntilNodeAppearsAndClick("#descObjTitle");

    overwriteText(node2);

    TreeItem<String> newItem = RodaInApplication.getSchemePane().getTreeView().getSelectionModel().getSelectedItem();
    assert newItem instanceof SchemaNode;
    SchemaNode newNode = (SchemaNode) newItem;
    Sip dobj = newNode.getDob();
    assert dobj != null;

    waitUntilNodeAppearsAndClick(node1);
    waitUntilNodeAppearsAndClick(node2);
    waitUntilNodeAppears(".tree-view");

    drag().dropTo(".tree-view");

    assert RodaInApplication.getSchemePane().getTreeView().getRoot().getChildren().size() == 2;

    waitUntilNodeAppearsAndClick("#" + Constants.CSS_REMOVE_LEVEL);

    clickOk();
    assert RodaInApplication.getSchemePane().getTreeView().getRoot().getChildren().size() == 1;
  }

  @Test
  public void loadAClassificationPlan() {
    Platform.runLater(() -> {
      stage.setMaximized(false);
      stage.setMaximized(true);
    });

    waitUntilNodeAppearsAndClick(lookup("UCP").lookup(".cellText").query());

    assert schemaPane.getTreeView().getSelectionModel().getSelectedIndex() == 7;

    TreeItem selected = schemaPane.getTreeView().getSelectionModel().getSelectedItem();
    assert "UCP".equals(selected.getValue());

    waitUntilNodeAppears(".tree-view");
    waitUntilNodeAppearsAndDoubleClick(lookup("UCP").lookup(".cellText").query());
    assert selected.getChildren().size() == 1;
  }

  @Test
  public void associateFilesAndFoldersToAnItemAndExportTheSIPs() throws IOException {
    Platform.runLater(() -> {
      fileExplorer.setFileExplorerRoot(testDir);
      stage.setMaximized(false);
      stage.setMaximized(true);
    });

    waitUntilNodeAppearsAndDoubleClick("dir4");
    waitUntilNodeAppears("dirB");

    waitUntilNodeAppears("UCP");

    // 2017/05/05 bferreira: workaround to drop at location (instead of droping
    // on node) because for some reason the node looses the location after
    // dropping, and then a NullPointer is thrown
    javafx.geometry.Point2D ucpLocation = point("UCP").query();
    drag("dirB").dropTo(ucpLocation);

    // modal should open
    waitUntilNodeAppearsAndClick("#assoc3");
    waitUntilNodeAppearsAndClick("#btConfirm");
    // modal should update
    waitUntilNodeAppearsAndClick("#btConfirm");
    // SIP should be created

    waitUntilNodeAppearsAndClick("UCP");
    waitUntilNodeAppearsAndClick("file1.txt");
    TreeItem selected = schemaPane.getTreeView().getSelectionModel().getSelectedItem();
    assert selected instanceof SipPreviewNode;
    TreeItem parent = selected.getParent();
    assert parent instanceof SchemaNode;
    assert "UCP".equals(((SchemaNode) parent).getDob().getTitle());

    assert parent.getChildren().size() == 14;

    waitUntilNodeAppearsAndClick("UCP");

    waitUntilNodeAppearsAndClick("#removeRule1");
    // rule should be removed, wait for that
    waitUntilNodeDisappears("#removeRule1");

    assert parent.getChildren().size() == 1;

    // create 2 SIPs
    waitUntilNodeAppearsAndClick("fileA.txt");
    press(KeyCode.CONTROL);
    waitUntilNodeAppearsAndClick("fileB.txt");
    release(KeyCode.CONTROL);

    drag().dropTo("UCP");

    # failing to find the right "UCP" here

    // modal should open
    waitUntilNodeAppearsAndClick("#assoc2");
    // modal should update
    waitUntilNodeAppearsAndClick(I18n.t("continue"));
    // modal should update
    waitUntilNodeAppearsAndClick("#meta4");
    waitUntilNodeAppearsAndClick(I18n.t("confirm"));
    // SIPs should be created, wait for that

    waitUntilNodeAppearsAndClick(I18n.t("Main.file"));
    waitUntilNodeAppearsAndClick(I18n.t("Main.exportSips"));
    output = Utils.homeDir.resolve("SIPs output");
    boolean outputFolderCreated = output.toFile().mkdir();
    if (!outputFolderCreated) {
      FileUtils.cleanDirectory(output.toFile());
    }
    CreationModalPreparation.setOutputFolder(output.toString());
    waitUntilNodeAppearsAndClick(I18n.t("start"));

    waitUntilNodeAppearsAndClick(I18n.t("close"));

    waitUntilNodeAppearsAndClick(I18n.t("Main.file"));
    waitUntilNodeAppearsAndClick(I18n.t("Main.exportSips"));
    waitUntilNodeAppearsAndClick("#sipTypes");
    waitUntilNodeAppearsAndClick("BagIt");
    CreationModalPreparation.setOutputFolder(output.toString());
    waitUntilNodeAppearsAndClick(I18n.t("start"));
    waitUntilNodeAppearsAndClick(I18n.t("close"));

    waitUntilNodeAppearsAndClick("FTP");

    waitUntilNodeAppearsAndClick("UCP");
    waitUntilNodeAppearsAndClick("#removeRule2");

    // wait for the rule to be removed
    waitUntilNodeDisappears("#removeRule2");
  }

  @Test
  public void associateFilesAndFoldersToAnItemAndSelectMultiple() {
    Platform.runLater(() -> {
      fileExplorer.setFileExplorerRoot(testDir);
      stage.setMaximized(false);
      sleep(500);
      stage.setMaximized(true);
    });

    sleep(5000); // wait for the tree to be created
    waitUntilNodeAppearsAndDoubleClick("dir4");
    sleep(2000); // wait for the node to expand
    drag("dirB").dropTo("UCP");
    sleep(2000); // wait for the modal to open
    waitUntilNodeAppearsAndClick("#assoc3");
    waitUntilNodeAppearsAndClick("#btConfirm");
    sleep(2000); // wait for the modal to update
    waitUntilNodeAppearsAndClick("#btConfirm");
    sleep(3000); // wait for the SIPs creation

    waitUntilNodeAppearsAndClick("UCP");

    waitUntilNodeAppearsAndClick(I18n.t("SchemaPane.add"));
    sleep(2000);
    waitUntilNodeAppearsAndClick(I18n.t("continue"));
    sleep(2000);
    waitUntilNodeAppearsAndClick(I18n.t("SchemaPane.newNode"));
    waitUntilNodeAppearsAndClick("file10.txt");
    sleep(1000);

    Platform.runLater(() -> {
      schemaPane.getTreeView().getSelectionModel().selectRange(10, 15);
      inspectionPane.update(schemaPane.getTreeView().getSelectionModel().getSelectedItems());
    });

    sleep(3000);

    waitUntilNodeAppearsAndClick("#descObjTitle");
    eraseText(50);
    write("Testing");
    sleep(1000);

    sleep(1000);
    waitUntilNodeAppearsAndClick(I18n.t("apply"));
  }

  @Test
  public void create10000FilesAndCreateSIPsForEachOne() {
    Path test10000Dir = Utils.create10000Files();

    sleep(5000);
    push(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    sleep(3000);
    try {
      push(KeyCode.RIGHT);
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }

    Platform.runLater(() -> {
      fileExplorer.setFileExplorerRoot(test10000Dir);
      stage.setMaximized(false);
      sleep(500);
      stage.setMaximized(true);
      schemaPane.createClassificationScheme();

    });

    sleep(10000); // wait for the tree to be created

    drag(test10000Dir.getFileName().toString()).dropTo("#schemaPaneDropBox");

    sleep(1000); // wait for the modal to open
    waitUntilNodeAppearsAndClick("#assoc3"); // SIP per File
    waitUntilNodeAppearsAndClick(I18n.t("continue"));
    sleep(1000); // wait for the modal to update
    waitUntilNodeAppearsAndClick(I18n.t("confirm"));
    sleep(5000); // wait for the SIPs creation

    long startTimestamp = System.currentTimeMillis();
    int timeout = 120 * 1000; // 120 seconds timeout for the SIP creation
    boolean stop = false;
    while (!stop && System.currentTimeMillis() - startTimestamp < timeout) {
      try {
        waitUntilNodeAppearsAndClick(I18n.t("RuleModalProcessing.creatingPreview").toUpperCase());
        sleep(1000);
      } catch (Exception e) {
        stop = true;
        // it means that the modal has been closed and SIPs are created
      }
    }

    assert stop;
    assert RodaInApplication.getAllDescriptionObjects().size() == 10000;
  }
}
