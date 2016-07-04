package org.roda.rodain.testing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-12-2015.
 */
public class Utils {
  public static final Path homeDir = getHomeDir();
  public static final Path testDir = getTestDir();

  private static Path getHomeDir() {
    String home = System.getProperty("java.io.tmpdir");
    return Paths.get(home);
  }

  private static Path getTestDir() {
    String home = System.getProperty("java.io.tmpdir");
    Path homePath = Paths.get(home);
    return homePath.resolve("RODA-In Test Dir");
  }

  public static Path createFolderStructure() {
    /*
      Create a directory structure to test the file explorer
     */
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
    createFile(dir4.resolve("fileA.txt"));
    createFile(dir4.resolve("fileB.txt"));
    createFile(dir4.resolve("fileC.txt"));

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

    return testDir;
  }

  private static void createDir(Path p) {
    File file = p.toFile();
    file.mkdir();
  }

  private static void createFile(Path p) {
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

  public static void removeTestDir() throws Exception {
    if (testDir != null && Files.exists(testDir))
      FileUtils.deleteDirectory(testDir.toFile());
  }

  public static void takeScreenshot(Stage stage, String fileName){
    Platform.runLater(() -> {
      try {
        WritableImage snapshot = stage.getScene().snapshot(null);
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "PNG", new File(fileName));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}
