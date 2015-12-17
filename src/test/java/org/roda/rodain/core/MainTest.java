package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SchemaPane;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.source.ui.FileExplorerPane;
import org.roda.rodain.testing.Utils;
import org.testfx.framework.junit.ApplicationTest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by adrapereira on 17-12-2015.
 */
public class MainTest extends ApplicationTest {
  private Path testDir;
  private Main main;
  private SchemaPane schemaPane;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    main = new Main();
    main.start(stage);

    schemaPane = Main.getSchemaPane();
    fileExplorer = Main.getPreviewExplorer();

    Path path = Paths.get("src/test/resources/plan_with_errors.json");
    InputStream stream = new FileInputStream(path.toFile());
    schemaPane.loadClassificationSchemeFromStream(stream);
  }

  @Before
  public void setUp() throws Exception {
    testDir = Utils.createFolderStructure();
  }

  @Test
  public void schemePane() {
    sleep(1000); //wait for the classification scheme to load

    clickOn("Unicamp");
    TreeItem selected = schemaPane.getTreeView().getSelectionModel().getSelectedItem();
    int selectedIndex = schemaPane.getTreeView().getSelectionModel().getSelectedIndex();
    assert selected.getValue().equals("Unicamp");
    assert selectedIndex == 7;

    doubleClickOn("Unicamp");
    assert selected.getChildren().size() == 1;
  }

  @Test
  public void association() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        fileExplorer.setFileExplorerRoot(testDir);
      }
    });

    sleep(2000); //wait for the tree to be created
    doubleClickOn("dir4");
    sleep(1000); //wait for the node to expand
    drag("dirB").dropTo("Unicamp");
    sleep(1000); //wait for the modal to open
    clickOn("#assoc3");
    clickOn("Continue");
    clickOn("#meta4");
    clickOn("Confirm");
    sleep(2000); //wait for the SIPs creation

    clickOn("file1.txt");
    TreeItem selected = schemaPane.getTreeView().getSelectionModel().getSelectedItem();
    assert selected instanceof SipPreviewNode;
    TreeItem parent = selected.getParent();
    assert parent instanceof SchemaNode;
    assert ((SchemaNode) parent).getDob().getTitle().equals("Unicamp");

    assert parent.getChildren().size() == 14;

    clickOn("Remove");
    sleep(1000); //wait for the SIP removal
    assert parent.getChildren().size() == 13;


    clickOn("12 items");
    clickOn("#removeRule1");
    sleep(1000); // wait for the rule to be removed

    assert parent.getChildren().size() == 1;
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(testDir.toFile());
  }

}