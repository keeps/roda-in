package org.roda.rodain.source.ui;

import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.roda.rodain.core.Footer;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.testing.Utils;
import org.testfx.framework.junit.ApplicationTest;

import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 15-12-2015.
 */
public class IgnoreTest extends ApplicationTest {
  private static Path testDir;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    setUpBeforeClass();
    new Footer(); //footer needs to be initialized because of setStatus
    fileExplorer = new FileExplorerPane(stage);
    fileExplorer.setFileExplorerRoot(testDir);

    Scene scene = new Scene(fileExplorer, 600, 600);
    stage.setScene(scene);
    stage.show();
  }

  public void setUpBeforeClass() throws Exception {
    testDir = Utils.createFolderStructure();
  }

  @Test
  public void ignore() {
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    root.setExpanded(true);
    sleep(500);
    fileExplorer.getTreeView().getSelectionModel().selectIndices(4);
    SourceTreeDirectory dir4 = (SourceTreeDirectory) root.getChildren().get(3);

    clickOn("#bt_ignore");
    assert dir4.getState() == SourceTreeItemState.IGNORED;
    assert root.getChildren().size() == 3;
    assert root.getChildren().get(2) != dir4;

    fileExplorer.toggleIgnoredShowing();
    sleep(500);
    assert root.getChildren().size() == 4;
    assert root.getChildren().get(3) == dir4;

    doubleClickOn(dir4.getValue());
    SourceTreeDirectory dirA = (SourceTreeDirectory) dir4.getChildren().get(0);
    assert dirA.getState() == SourceTreeItemState.IGNORED;

    dirA.setExpanded(true);
    sleep(500);
    rightClickOn("dirAA");
    clickOn("Remove Ignore");
    SourceTreeDirectory dirAA = (SourceTreeDirectory) dirA.getChildren().get(0);
    assert dirAA.getState() == SourceTreeItemState.NORMAL;

    rightClickOn("dirAB");
    clickOn("Remove Ignore");
    SourceTreeDirectory dirAB = (SourceTreeDirectory) dirA.getChildren().get(1);
    assert dirAB.getState() == SourceTreeItemState.NORMAL;

    doubleClickOn("dirAB");
    sleep(500);
    SourceTreeFile file1AB = (SourceTreeFile) dirAB.getChildren().get(0);
    assert file1AB.getState() == SourceTreeItemState.NORMAL;

    assert dirA.getState() == SourceTreeItemState.NORMAL;
    assert dir4.getState() == SourceTreeItemState.NORMAL;

    SourceTreeDirectory dirB = (SourceTreeDirectory) dir4.getChildren().get(1);
    assert dirB.getState() == SourceTreeItemState.IGNORED;
    SourceTreeFile fileA = (SourceTreeFile) dir4.getChildren().get(2);
    assert fileA.getState() == SourceTreeItemState.IGNORED;

    clickOn("dir4");
    clickOn("Ignore");
    assert dir4.getState() == SourceTreeItemState.IGNORED;
    assert fileA.getState() == SourceTreeItemState.IGNORED;
    assert dirA.getState() == SourceTreeItemState.IGNORED;
    assert dirAB.getState() == SourceTreeItemState.IGNORED;

    rightClickOn("dir4");
    clickOn("Remove Ignore");
    assert dir4.getState() == SourceTreeItemState.NORMAL;
    assert fileA.getState() == SourceTreeItemState.NORMAL;
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FileUtils.deleteDirectory(testDir.toFile());
  }
}