package org.roda.rodain.source.ui;

import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.Utils;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.source.FileExplorerPane;
import org.roda.rodain.ui.source.items.SourceTreeDirectory;
import org.roda.rodain.ui.source.items.SourceTreeFile;
import org.testfx.framework.junit.ApplicationTest;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 15-12-2015.
 */
public class IgnoreTest extends ApplicationTest {
  private static Path testDir;
  private FileExplorerPane fileExplorer;
  private Stage stage;

  @Override
  public void start(Stage stage) throws Exception {
    this.stage = stage;
    RodaInApplication main = new RodaInApplication();
    main.start(stage);

    sleep(5000);

    fileExplorer = RodaInApplication.getFileExplorer();
    fileExplorer.setFileExplorerRoot(testDir);
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = Utils.createFolderStructure();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    PathCollection.reset();
  }

  @Test
  public void ignoreAndUnignoreToTestThePathCollectionIgnoreAlgorithm() {
    sleep(7000);
    Platform.runLater(() -> {
      stage.setMaximized(false);
      stage.setMaximized(true);
    });
    try {
      push(KeyCode.ENTER);
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }
    TreeItem<String> root = fileExplorer.getTreeView().getRoot().getChildren().get(0);

    clickOn("dir4");
    sleep(1000);
    SourceTreeDirectory dir4 = (SourceTreeDirectory) root.getChildren().get(3);

    clickOn("#bt_ignore");
    assert dir4.getState() == PathState.IGNORED;
    assert root.getChildren().size() == 3;
    assert root.getChildren().get(2) != dir4;

    push(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
    sleep(1000);
    assert root.getChildren().size() == 4;
    assert root.getChildren().get(3) == dir4;

    doubleClickOn(dir4.getValue());
    SourceTreeDirectory dirA = (SourceTreeDirectory) dir4.getChildren().get(0);
    assert dirA.getState() == PathState.IGNORED;
    sleep(1000);

    if (!dirA.isExpanded())
      doubleClickOn("dirA");
    sleep(1000);
    SourceTreeDirectory dirAA = (SourceTreeDirectory) dirA.getChildren().get(0);
    dirAA.removeIgnore();
    sleep(1000);
    assert dirAA.getState() == PathState.NORMAL;
    if (dirAA.isExpanded())
      doubleClickOn("dirAA");

    SourceTreeDirectory dirAB = (SourceTreeDirectory) dirA.getChildren().get(1);
    dirAB.removeIgnore();
    sleep(1000);
    assert dirAB.getState() == PathState.NORMAL;

    if (!dirAB.isExpanded())
      doubleClickOn("dirAB");
    sleep(1000);
    SourceTreeFile file1AB = (SourceTreeFile) dirAB.getChildren().get(0);
    assert file1AB.getState() == PathState.NORMAL;

    assert dirA.getState() == PathState.NORMAL;
    assert dir4.getState() == PathState.NORMAL;

    SourceTreeDirectory dirB = (SourceTreeDirectory) dir4.getChildren().get(1);
    assert dirB.getState() == PathState.IGNORED;
    SourceTreeFile fileA = (SourceTreeFile) dir4.getChildren().get(2);
    assert fileA.getState() == PathState.IGNORED;

    clickOn("dir4");
    clickOn(I18n.t("ignore"));
    assert dir4.getState() == PathState.IGNORED;
    assert fileA.getState() == PathState.IGNORED;
    assert dirA.getState() == PathState.IGNORED;
    assert dirAB.getState() == PathState.IGNORED;

    dir4.removeIgnore();
    sleep(1000);
    assert dir4.getState() == PathState.NORMAL;
    assert fileA.getState() == PathState.NORMAL;
  }
}
