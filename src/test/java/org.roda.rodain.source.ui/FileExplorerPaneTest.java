package org.roda.rodain.source.ui;

import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.roda.rodain.core.Footer;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.testing.Utils;
import org.roda.rodain.utils.AsyncCallState;
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
  private static int LOAD_MORE_SIZE = 50;
  private Path testDir;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    setUp();
    new Footer(); //footer needs to be initialized because of setStatus
    fileExplorer = new FileExplorerPane(stage);
    fileExplorer.setFileExplorerRoot(testDir);
    Scene scene = new Scene(fileExplorer, 600, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Before
  public void setUp() throws Exception {
    testDir = Utils.createFolderStructure();
  }


  @Test
  public void root() {
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    // Root exists, is a SourceTreeDirectory and its path is testDir
    assert root != null;
    assert root instanceof SourceTreeDirectory;
    assert ((SourceTreeDirectory) root).getPath().equals(testDir.toString());

    /* Tree is well structured */
    loadMore(root);
    assert root.getChildren().size() == 4;
  }

  @Test
  public void dir1() {
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir1 = root.getChildren().get(0);
    assert dir1 != null;
    assert dir1.getValue().equals("dir1");

    loadMore(dir1);
    assert dir1.getChildren().size() == LOAD_MORE_SIZE + 1;
    assert dir1.getChildren().get(0) instanceof SourceTreeFile;

    loadMore(dir1);
    assert dir1.getChildren().size() == (LOAD_MORE_SIZE * 2) + 1;

    loadMore(dir1);
    assert dir1.getChildren().size() == 120;

    SourceTreeFile file = (SourceTreeFile) dir1.getChildren().get(0);
    assert file.getValue().equals("file0.txt");
    StringBuilder sb = new StringBuilder();
    sb.append(testDir).append(File.separator);
    sb.append("dir1").append(File.separator);
    sb.append("file0.txt");
    assert file.getPath().equals(sb.toString());
  }

  @Test
  public void dir2() {
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir2 = root.getChildren().get(1);
    assert dir2 != null;
    assert dir2.getValue().equals("dir2");

    loadMore(dir2);
    assert dir2.getChildren().size() == LOAD_MORE_SIZE + 1;
    assert dir2.getChildren().get(0) instanceof SourceTreeDirectory;

    loadMore(dir2);
    assert dir2.getChildren().size() == (LOAD_MORE_SIZE * 2) + 1;

    loadMore(dir2);
    assert dir2.getChildren().size() == 120;
  }

  @Test
  public void dir3() {
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir3 = root.getChildren().get(2);
    assert dir3 != null;
    assert dir3.getValue().equals("dir3");

    loadMore(dir3);
    assert dir3.getChildren().size() == LOAD_MORE_SIZE + 1;

    loadMore(dir3);
    assert dir3.getChildren().size() == (LOAD_MORE_SIZE * 2) + 1;

    loadMore(dir3);
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
    TreeItem<String> root = fileExplorer.getTreeView().getRoot();
    TreeItem<String> dir4 = root.getChildren().get(3);
    assert dir4 != null;
    assert dir4.getValue().equals("dir4");

    loadMore(dir4);
    assert dir4.getChildren().size() == 5;

    TreeItem<String> dirA = dir4.getChildren().get(0);
    assert dirA.getValue().equals("dirA");
    assert dirA instanceof SourceTreeDirectory;

    loadMore(dirA);
    assert dirA.getChildren().size() == 2;
    SourceTreeDirectory dirAA = (SourceTreeDirectory) dirA.getChildren().get(0);
    assert dirAA.getValue().equals("dirAA");

    loadMore(dirAA);
    assert dirAA.getChildren().size() == 3;
    SourceTreeDirectory dirAAC = (SourceTreeDirectory) dirAA.getChildren().get(2);
    assert dirAAC.getValue().equals("dirAAC");

    loadMore(dirAAC);
    assert dirAAC.getChildren().size() == 10;
    SourceTreeFile file = (SourceTreeFile) dirAAC.getChildren().get(0);
    assert file.getValue().equals("file0.txt");

  }

  private void loadMore(TreeItem dir) {
    if (dir instanceof SourceTreeDirectory) {
      AsyncCallState dirTask = ((SourceTreeDirectory) dir).loadMore();
      try {
        synchronized (dirTask) {
          dirTask.wait();
          dir.setExpanded(true);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(testDir.toFile());
  }
}