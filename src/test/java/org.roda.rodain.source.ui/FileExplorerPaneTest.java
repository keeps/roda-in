package org.roda.rodain.source.ui;

import javafx.geometry.VerticalDirection;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.testing.Utils;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 14-12-2015.
 */
public class FileExplorerPaneTest extends ApplicationTest {
  private static int LOAD_MORE_SIZE = 100;
  private static Path testDir;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    RodaIn main = new RodaIn();
    main.start(stage);

    sleep(5000);

    fileExplorer = RodaIn.getFileExplorer();
    fileExplorer.setFileExplorerRoot(testDir);
    sleep(5000);
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = Utils.createFolderStructure();
  }

  @Test
  public void root() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot().getChildren().get(0);
    // Root exists, is a SourceTreeDirectory and its path is testDir
    assert root != null;
    assert root instanceof SourceTreeDirectory;
    assert ((SourceTreeDirectory) root).getPath().equals(testDir.toString());

    assert root.getChildren().size() == 4;
  }

  @Test
  public void dir1() {
    sleep(5000);
    try {
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }
    TreeItem<String> root = fileExplorer.getTreeView().getRoot().getChildren().get(0);
    TreeItem<String> dir1 = root.getChildren().get(0);
    assert dir1 != null;
    assert "dir1".equals(dir1.getValue());

    doubleClickOn("dir1");
    sleep(1000);
    assert dir1.getChildren().size() == LOAD_MORE_SIZE + 1;
    assert dir1.getChildren().get(0) instanceof SourceTreeFile;

    scroll(70, VerticalDirection.DOWN);
    clickOn(I18n.t("SourceTreeLoadMore.title"));
    assert dir1.getChildren().size() == 120;

    SourceTreeFile file = (SourceTreeFile) dir1.getChildren().get(0);
    assert "file0.txt".equals(file.getValue());
    StringBuilder sb = new StringBuilder();
    sb.append(testDir).append(File.separator);
    sb.append("dir1").append(File.separator);
    sb.append("file0.txt");
    assert file.getPath().equals(sb.toString());
    for (TreeItem t : root.getChildren()) {
      t.setExpanded(false);
    }
  }

  @Test
  public void dir2() {
    sleep(5000);
    try {
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }
    TreeItem<String> root = fileExplorer.getTreeView().getRoot().getChildren().get(0);
    TreeItem<String> dir2 = root.getChildren().get(1);
    assert dir2 != null;
    assert "dir2".equals(dir2.getValue());

    doubleClickOn("dir2");
    sleep(1000);
    assert dir2.getChildren().size() == LOAD_MORE_SIZE + 1;
    assert dir2.getChildren().get(0) instanceof SourceTreeDirectory;

    scroll(70, VerticalDirection.DOWN);
    clickOn(I18n.t("SourceTreeLoadMore.title"));
    assert dir2.getChildren().size() == 120;
    for (TreeItem t : root.getChildren()) {
      t.setExpanded(false);
    }
  }

  @Test
  public void dir3() {
    sleep(5000);
    try {
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }
    TreeItem<String> root = fileExplorer.getTreeView().getRoot().getChildren().get(0);
    TreeItem<String> dir3 = root.getChildren().get(2);
    assert dir3 != null;
    assert "dir3".equals(dir3.getValue());

    doubleClickOn("dir3");
    sleep(1000);
    assert dir3.getChildren().size() == LOAD_MORE_SIZE + 1;

    scroll(70, VerticalDirection.DOWN);
    clickOn(I18n.t("SourceTreeLoadMore.title"));
    assert dir3.getChildren().size() == 140;

    List<Object> files = dir3.getChildren().stream().filter(p -> p instanceof SourceTreeFile)
      .collect(Collectors.toList());
    List<Object> dirs = dir3.getChildren().stream().filter(p -> p instanceof SourceTreeDirectory)
      .collect(Collectors.toList());

    assert files.size() == 70;
    assert dirs.size() == 70;
    for (TreeItem t : root.getChildren()) {
      t.setExpanded(false);
    }
  }

  @Test
  public void dir4() {
    sleep(5000);
    try {
      push(KeyCode.ENTER);
    } catch (Exception e) {
    }
    TreeItem<String> root = fileExplorer.getTreeView().getRoot().getChildren().get(0);
    TreeItem<String> dir4 = root.getChildren().get(3);
    assert dir4 != null;
    assert "dir4".equals(dir4.getValue());

    sleep(1000);
    doubleClickOn("dir4");
    sleep(1000);
    assert dir4.getChildren().size() == 5;

    TreeItem<String> dirA = dir4.getChildren().get(0);
    assert "dirA".equals(dirA.getValue());
    assert dirA instanceof SourceTreeDirectory;

    doubleClickOn("dirA");
    assert dirA.getChildren().size() == 2;
    SourceTreeDirectory dirAA = (SourceTreeDirectory) dirA.getChildren().get(0);
    assert "dirAA".equals(dirAA.getValue());

    doubleClickOn("dirAA");
    assert dirAA.getChildren().size() == 3;
    SourceTreeDirectory dirAAC = (SourceTreeDirectory) dirAA.getChildren().get(2);
    assert "dirAAC".equals(dirAAC.getValue());

    doubleClickOn("dirAAC");
    assert dirAAC.getChildren().size() == 10;
    SourceTreeFile file = (SourceTreeFile) dirAAC.getChildren().get(0);
    assert "file0.txt".equals(file.getValue());
    for (TreeItem t : root.getChildren()) {
      t.setExpanded(false);
    }
  }
}