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
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by adrapereira on 14-12-2015.
 */
public class IgnoreTest extends ApplicationTest {
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
    /*
      Create a directory structure to test the file explorer
     */
    String home = System.getProperty("user.home");
    Path homePath = Paths.get(home);

    testDir = homePath.resolve("RODA-In Test Dir");
    createDir(testDir);
    /*
      Dir 1
     */
    Path dir1 = testDir.resolve("dir1");
    createDir(dir1);
    for (int i = 0; i < 120; i++) {
      createFile(dir1.resolve("file" + i + ".txt"));
    }
    /*
      Dir 2
     */
    Path dir2 = testDir.resolve("dir2");
    createDir(dir2);
    for (int i = 0; i < 120; i++) {
      createDir(dir2.resolve("dir" + i));
    }
    /*
      Dir 3
     */
    Path dir3 = testDir.resolve("dir3");
    createDir(dir3);
    for (int i = 0; i < 70; i++) {
      createFile(dir3.resolve("file" + i + ".txt"));
      createDir(dir3.resolve("dir" + i));
    }
    /*
      Dir 4
     */
    Path dir4 = testDir.resolve("dir4");
    createDir(dir4);
    createFile(dir4.resolve("file1.txt"));
    createFile(dir4.resolve("file2.txt"));
    createFile(dir4.resolve("file3.txt"));

    //dir4/dirA
    Path dirA = dir4.resolve("dirA");
    createDir(dirA);

    // dir4/dirA/dirAA
    Path dirAA = dirA.resolve("dirAA");
    createDir(dirAA);
    Path dirAAA = dirAA.resolve("dirAAA");
    createDir(dirAAA);
    createFile(dirAAA.resolve("file1.txt"));
    Path dirAAB = dirAA.resolve("dirAAB");
    createDir(dirAAB);
    createFile(dirAAB.resolve("file1.txt"));
    createFile(dirAAB.resolve("file2.txt"));
    Path dirAAC = dirAA.resolve("dirAAC");
    createDir(dirAAC);
    for (int i = 0; i < 10; i++)
      createFile(dirAAC.resolve("file" + i + ".txt"));

    // dir4/dirA/dirAB
    Path dirAB = dirA.resolve("dirAB");
    createDir(dirAB);
    createFile(dirAB.resolve("file1.txt"));

    //dir4/dirB
    Path dirB = dir4.resolve("dirB");
    createDir(dirB);
    for (int i = 0; i < 13; i++)
      createFile(dirB.resolve("file" + i + ".txt"));
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
  }


  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(testDir.toFile());
  }

  private void createDir(Path p) {
    File file = p.toFile();
    file.mkdir();
  }

  private void createFile(Path p) {
    try {
      PrintWriter writer = new PrintWriter(p.toString(), "UTF-8");
      writer.println(p.toString());
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

  }
}