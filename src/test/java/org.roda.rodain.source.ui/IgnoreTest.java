package org.roda.rodain.source.ui;

import java.nio.file.Path;

import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.testing.Utils;
import org.testfx.framework.junit.ApplicationTest;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 15-12-2015.
 */
public class IgnoreTest extends ApplicationTest {
  private static Path testDir;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    RodaIn main = new RodaIn();
    main.start(stage);

    fileExplorer = RodaIn.getPreviewExplorer();
    fileExplorer.setFileExplorerRoot(testDir);
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = Utils.createFolderStructure();
  }

  @Test
  public void ignore() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();

    clickOn("dir4");
    sleep(1000);
    SourceTreeDirectory dir4 = (SourceTreeDirectory) root.getChildren().get(3);

    clickOn("#bt_ignore");
    assert dir4.getState() == SourceTreeItemState.IGNORED;
    assert root.getChildren().size() == 3;
    assert root.getChildren().get(2) != dir4;

    push(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
    sleep(1000);
    assert root.getChildren().size() == 4;
    assert root.getChildren().get(3) == dir4;

    doubleClickOn(dir4.getValue());
    SourceTreeDirectory dirA = (SourceTreeDirectory) dir4.getChildren().get(0);
    assert dirA.getState() == SourceTreeItemState.IGNORED;

    doubleClickOn("dirA");
    sleep(1000);
    rightClickOn("dirAA");
    sleep(1000);
    clickOn("#removeIgnore");
    SourceTreeDirectory dirAA = (SourceTreeDirectory) dirA.getChildren().get(0);
    assert dirAA.getState() == SourceTreeItemState.NORMAL;

    rightClickOn("dirAB");
    sleep(1000);
    clickOn("#removeIgnore");
    SourceTreeDirectory dirAB = (SourceTreeDirectory) dirA.getChildren().get(1);
    assert dirAB.getState() == SourceTreeItemState.NORMAL;

    doubleClickOn("dirAB");
    sleep(1000);
    SourceTreeFile file1AB = (SourceTreeFile) dirAB.getChildren().get(0);
    assert file1AB.getState() == SourceTreeItemState.NORMAL;

    assert dirA.getState() == SourceTreeItemState.NORMAL;
    assert dir4.getState() == SourceTreeItemState.NORMAL;

    SourceTreeDirectory dirB = (SourceTreeDirectory) dir4.getChildren().get(1);
    assert dirB.getState() == SourceTreeItemState.IGNORED;
    SourceTreeFile fileA = (SourceTreeFile) dir4.getChildren().get(2);
    assert fileA.getState() == SourceTreeItemState.IGNORED;

    clickOn("dir4");
    clickOn(AppProperties.getLocalizedString("ignore"));
    assert dir4.getState() == SourceTreeItemState.IGNORED;
    assert fileA.getState() == SourceTreeItemState.IGNORED;
    assert dirA.getState() == SourceTreeItemState.IGNORED;
    assert dirAB.getState() == SourceTreeItemState.IGNORED;

    rightClickOn("dir4");
    clickOn("#removeIgnore");
    assert dir4.getState() == SourceTreeItemState.NORMAL;
    assert fileA.getState() == SourceTreeItemState.NORMAL;
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    Utils.removeTestDir();
  }
}