package org.roda.rodain.source.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javafx.geometry.VerticalDirection;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.Footer;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.testing.Utils;
import org.testfx.framework.junit.ApplicationTest;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 14-12-2015.
 */
public class FileExplorerPaneTest extends ApplicationTest {
  private static int LOAD_MORE_SIZE = 50;
  private static Path testDir;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    new Footer(); //footer needs to be initialized because of setStatus
    AppProperties.initialize();
    fileExplorer = new FileExplorerPane(stage);
    fileExplorer.setFileExplorerRoot(testDir);
    Scene scene = new Scene(fileExplorer, 600, 600);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = Utils.createFolderStructure();
  }


  @Test
  public void root() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    // Root exists, is a SourceTreeDirectory and its path is testDir
    assert root != null;
    assert root instanceof SourceTreeDirectory;
    assert ((SourceTreeDirectory) root).getPath().equals(testDir.toString());

    assert root.getChildren().size() == 4;
  }

  @Test
  public void dir1() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir1 = root.getChildren().get(0);
    assert dir1 != null;
    assert "dir1".equals(dir1.getValue());

    doubleClickOn("dir1");
    assert dir1.getChildren().size() == LOAD_MORE_SIZE + 1;
    assert dir1.getChildren().get(0) instanceof SourceTreeFile;

    scroll(50, VerticalDirection.DOWN);
    clickOn(AppProperties.getLocalizedString("SourceTreeLoadMore.title"));
    assert dir1.getChildren().size() == (LOAD_MORE_SIZE * 2) + 1;

    scroll(50, VerticalDirection.DOWN);
    clickOn(AppProperties.getLocalizedString("SourceTreeLoadMore.title"));
    assert dir1.getChildren().size() == 120;

    SourceTreeFile file = (SourceTreeFile) dir1.getChildren().get(0);
    assert "file0.txt".equals(file.getValue());
    StringBuilder sb = new StringBuilder();
    sb.append(testDir).append(File.separator);
    sb.append("dir1").append(File.separator);
    sb.append("file0.txt");
    assert file.getPath().equals(sb.toString());
  }

  @Test
  public void dir2() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir2 = root.getChildren().get(1);
    assert dir2 != null;
    assert "dir2".equals(dir2.getValue());

    doubleClickOn("dir2");
    assert dir2.getChildren().size() == LOAD_MORE_SIZE + 1;
    assert dir2.getChildren().get(0) instanceof SourceTreeDirectory;

    scroll(50, VerticalDirection.DOWN);
    clickOn(AppProperties.getLocalizedString("SourceTreeLoadMore.title"));
    assert dir2.getChildren().size() == (LOAD_MORE_SIZE * 2) + 1;

    scroll(50, VerticalDirection.DOWN);
    clickOn(AppProperties.getLocalizedString("SourceTreeLoadMore.title"));
    assert dir2.getChildren().size() == 120;
  }

  @Test
  public void dir3() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir3 = root.getChildren().get(2);
    assert dir3 != null;
    assert "dir3".equals(dir3.getValue());

    doubleClickOn("dir3");
    assert dir3.getChildren().size() == LOAD_MORE_SIZE + 1;

    scroll(50, VerticalDirection.DOWN);
    clickOn(AppProperties.getLocalizedString("SourceTreeLoadMore.title"));
    assert dir3.getChildren().size() == (LOAD_MORE_SIZE * 2) + 1;

    scroll(50, VerticalDirection.DOWN);
    clickOn(AppProperties.getLocalizedString("SourceTreeLoadMore.title"));
    assert dir3.getChildren().size() == 140;

    List<Object> files = dir3.getChildren().stream().
        filter(p -> p instanceof SourceTreeFile).
        collect(Collectors.toList());
    List<Object> dirs = dir3.getChildren().stream().
        filter(p -> p instanceof SourceTreeDirectory).
        collect(Collectors.toList());

    assert files.size() == 70;
    assert dirs.size() == 70;
  }

  @Test
  public void dir4() {
    sleep(1000);
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
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

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FileUtils.deleteDirectory(testDir.toFile());
  }
}