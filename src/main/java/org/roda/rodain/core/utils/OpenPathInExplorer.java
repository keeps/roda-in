package org.roda.rodain.core.utils;

import java.io.IOException;
import java.nio.file.Path;

import org.roda.rodain.core.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 18-05-2016.
 */
public class OpenPathInExplorer {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenPathInExplorer.class.getName());

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
    if (Controller.systemIsWindows()) {
      executeCommand("explorer", file);
    } else if (Controller.systemIsMac()) {
      executeCommand("open", file);
    } else if (Controller.systemIsUnix()) {
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
}
