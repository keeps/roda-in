package org.roda.rodain.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.roda.rodain.creation.ui.CreationModalPreparation;
import org.roda.rodain.inspection.InspectionPane;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SchemaPane;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.source.ui.FileExplorerPane;
import org.roda.rodain.testing.Utils;
import org.testfx.framework.junit.ApplicationTest;

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
public class MainTest extends ApplicationTest {
  private static Path testDir, output;
  private SchemaPane schemaPane;
  private FileExplorerPane fileExplorer;
  private InspectionPane inspectionPane;
  private static RodaIn main;
  private Stage stage;

  @Override
  public void start(Stage stage) throws Exception {
    this.stage = stage;
    main = new RodaIn();
    main.start(stage);

    sleep(6000);

    schemaPane = RodaIn.getSchemePane();
    fileExplorer = RodaIn.getFileExplorer();
    inspectionPane = RodaIn.getInspectionPane();

    Path path = Paths.get("src/test/resources/plan_with_errors.json");
    InputStream stream = new FileInputStream(path.toFile());
    schemaPane.loadClassificationSchemeFromStream(stream);
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
    sleep(5000);
    push(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    sleep(3000);
    try {
      push(KeyCode.RIGHT);
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }
    sleep(5000);
    clickOn(I18n.t("SchemaPane.add"));
    sleep(2000);
    clickOn(".schemaNode");
    sleep(1000);
    clickOn("#descObjTitle");
    eraseText(50);
    write("Node1");
    sleep(1000);

    TreeItem<String> item = RodaIn.getSchemePane().getTreeView().getSelectionModel().getSelectedItem();
    assert "Node1".equals(item.getValue());

    clickOn(I18n.t("SchemaPane.add"));
    sleep(500);
    clickOn(I18n.t("SchemaPane.newNode"));
    sleep(500);
    clickOn("#descObjTitle");
    eraseText(50);
    write("Node2");
    sleep(1000);

    doubleClickOn(".tree-view");

    clickOn("Node2");

    TreeItem<String> newItem = RodaIn.getSchemePane().getTreeView().getSelectionModel().getSelectedItem();
    assert newItem instanceof SchemaNode;
    SchemaNode newNode = (SchemaNode) newItem;
    DescriptionObject dobj = newNode.getDob();
    assert dobj != null;

    sleep(2000);
    drag("Node2").dropTo(".tree-view");
    assert RodaIn.getSchemePane().getTreeView().getRoot().getChildren().size() == 2;
    sleep(1000);
    clickOn("Node2").clickOn("#removeLevel");
    sleep(5000);
    try {
      clickOn("OK");
    } catch (Exception e) {
      push(KeyCode.RIGHT);
      push(KeyCode.ENTER);
    }
    assert RodaIn.getSchemePane().getTreeView().getRoot().getChildren().size() == 1;
  }

  @Test
  public void loadAClassificationPlan() {
    sleep(5000); // wait for the classification scheme to load

    Platform.runLater(() -> {
      stage.setMaximized(false);
      stage.setMaximized(true);
    });

    clickOn("UCP");
    TreeItem selected = schemaPane.getTreeView().getSelectionModel().getSelectedItem();
    int selectedIndex = schemaPane.getTreeView().getSelectionModel().getSelectedIndex();
    assert "UCP".equals(selected.getValue());
    assert selectedIndex == 7;

    doubleClickOn(".tree-view");

    doubleClickOn("UCP");

    assert selected.getChildren().size() == 1;
  }

  @Test
  public void associateFilesAndFoldersToAnItemAndExportTheSIPs() {
    Platform.runLater(() -> {
      fileExplorer.setFileExplorerRoot(testDir);
      stage.setMaximized(false);
      stage.setMaximized(true);
    });

    sleep(5000); // wait for the tree to be created
    doubleClickOn("dir4");
    sleep(2000); // wait for the node to expand
    drag("dirB").dropTo("UCP");
    sleep(2000); // wait for the modal to open
    clickOn("#assoc3");
    clickOn("#btConfirm");
    sleep(2000); // wait for the modal to update
    clickOn("#btConfirm");
    sleep(6000); // wait for the SIPs creation

    clickOn("UCP");
    clickOn("file1.txt");
    TreeItem selected = schemaPane.getTreeView().getSelectionModel().getSelectedItem();
    assert selected instanceof SipPreviewNode;
    TreeItem parent = selected.getParent();
    assert parent instanceof SchemaNode;
    assert "UCP".equals(((SchemaNode) parent).getDob().getTitle());

    assert parent.getChildren().size() == 14;

    clickOn("UCP");
    clickOn("#removeRule1");
    sleep(1000); // wait for the rule to be removed

    assert parent.getChildren().size() == 1;

    // create 2 SIPs
    clickOn("fileA.txt");
    press(KeyCode.CONTROL);
    clickOn("fileB.txt");
    release(KeyCode.CONTROL);

    drag().dropTo("UCP");
    sleep(1000); // wait for the modal to open
    clickOn("#assoc2");
    clickOn(I18n.t("continue"));
    sleep(1000); // wait for the modal to update
    clickOn("#meta4");
    clickOn(I18n.t("confirm"));
    sleep(5000); // wait for the SIPs creation

    clickOn(I18n.t("Main.file"));
    clickOn(I18n.t("Main.exportSips"));
    output = Utils.homeDir.resolve("SIPs output");
    output.toFile().mkdir();
    CreationModalPreparation.setOutputFolder(output.toString());
    clickOn(I18n.t("start"));

    sleep(5000);
    clickOn(I18n.t("close"));

    clickOn(I18n.t("Main.file"));
    clickOn(I18n.t("Main.exportSips"));
    clickOn("#sipTypes").clickOn("BagIt");
    CreationModalPreparation.setOutputFolder(output.toString());
    clickOn(I18n.t("start"));
    sleep(5000);
    clickOn(I18n.t("close"));

    clickOn("FTP");
    sleep(1000);

    clickOn("UCP");
    clickOn("#removeRule2");
    sleep(1000); // wait for the rule to be removed
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
    doubleClickOn("dir4");
    sleep(2000); // wait for the node to expand
    drag("dirB").dropTo("UCP");
    sleep(2000); // wait for the modal to open
    clickOn("#assoc3");
    clickOn("#btConfirm");
    sleep(2000); // wait for the modal to update
    clickOn("#btConfirm");
    sleep(3000); // wait for the SIPs creation

    clickOn("UCP");

    clickOn(I18n.t("SchemaPane.add"));
    sleep(2000);
    clickOn(I18n.t("SchemaPane.newNode"));
    clickOn("file10.txt");
    sleep(1000);

    Platform.runLater(() -> {
      schemaPane.getTreeView().getSelectionModel().selectRange(10,15);
      inspectionPane.update(schemaPane.getTreeView().getSelectionModel().getSelectedItems());
    });

    sleep(3000);

    clickOn("#descObjTitle");
    eraseText(50);
    write("Testing");
    sleep(1000);

    sleep(1000);
    clickOn(I18n.t("apply"));
  }
}
