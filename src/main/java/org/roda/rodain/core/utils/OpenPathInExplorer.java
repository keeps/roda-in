package org.roda.rodain.core.utils;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 18-05-2016.
 */
public class OpenPathInExplorer {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenPathInExplorer.class.getName());
  private static String OS = System.getProperty("os.name").toLowerCase();

  /**
   * Opens the file of the parametrized path.
   * 
   * @param path
   *          The path to the file that should be opened.
   */
  public static void open(Path path) {
    String fileName = path.toString();
    open(fileName);
  }

  /**
   * Opens the file of the parametrized path.
   * 
   * @param file
   *          The path string to the file that should be opened.
   */
  public static void open(String file) {
    // Different commands for different operating systems
    if (isWindows()) {
      executeCommand("explorer", file);
    } else if (isMac()) {
      executeCommand("open", file);
    } else if (isUnix()) {
      boolean result = executeCommand("xdg-open", file);
      if (!result) {
        executeCommand("gnome-open", file);
      }
    }
  }

  private static boolean executeCommand(String command, String file) {
    try {
      ProcessBuilder pb = new ProcessBuilder(command, file);
      pb.start();
    } catch (IOException e) {
      LOGGER.info("Error opening file from SIP content", e);
      return false;
    }
    return true;
  }

  private static boolean isWindows() {
    return OS.contains("win");
  }

  private static boolean isMac() {
    return OS.contains("mac");
  }

  private static boolean isUnix() {
    return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
  }
}
