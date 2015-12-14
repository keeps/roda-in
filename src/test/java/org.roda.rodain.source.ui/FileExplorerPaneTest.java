package org.roda.rodain.source.ui;

import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.roda.rodain.core.Footer;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
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
public class FileExplorerPaneTest extends ApplicationTest {
  private Path testDir;
  private FileExplorerPane fileExplorer;

  @Override
  public void start(Stage stage) throws Exception {
    setUp();
    new Footer(); //footer needs to be initialized because of setStatus
    fileExplorer = new FileExplorerPane(stage);
    fileExplorer.setFileExplorerRoot(testDir);
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
    createFile(testDir.resolve("test.txt"));
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
  public void firstTest() {
    TreeItem root = fileExplorer.getTreeView().getRoot();
    // Root exists, is a SourceTreeDirectory and its path is testDir
    assert root != null;
    assert root instanceof SourceTreeDirectory;
    assert ((SourceTreeDirectory) root).getPath().equals(testDir.toString());
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