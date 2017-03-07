package org.roda.rodain.core.rules.filters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 04-01-2016.
 */
public class IgnoredFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(IgnoredFilter.class.getName());

  private static Set<String> rules = new HashSet<>();
  private static Map<String, Pattern> rulesPatterns = new HashMap<>();

  private IgnoredFilter() {
  }

  /**
   * Adds a new ignore rule.
   * 
   * @param rule
   *          The new ignore rule.
   */
  public static void addIgnoreRule(String rule) {
    if (rules.add(rule)) {
      rulesPatterns.put(rule, Pattern.compile(rule));
    }
  }

  /**
   * Checks if the file/directory matches one of the configured filters
   * 
   * @param path
   *          The path to be filtered
   * @return True if the path matches one filter, false otherwise
   */
  public static boolean isIgnored(Path path) {
    boolean result = false;
    if (!rules.isEmpty()) {
      int i = path.getNameCount() - 1;
      for (; i >= 0; i--) {
        if (isIgnored(path.getName(i).toString())) {
          result = true;
          break;
        }
      }

      if (!result) {
        result = !containsAtLeastOneNotIgnoredFile(path);
      }
    }
    return result;
  }

  /**
   * Checks if a value has been ignored by a rule set in the configuration file
   * of the application.
   * 
   * @param value
   *          The value to be filtered
   * @return True if the value is ignored, false otherwise.
   */
  private static boolean isIgnored(String value) {
    boolean result = false;
    for (String rule : rules) {
      Pattern p = rulesPatterns.get(rule);
      Matcher m = p.matcher(value);
      if (m.matches()) {
        result = true;
        break;
      }
    }

    return result;
  }

  /**
   * 
   * @return true if path is not a directory
   * @return false if path is an empty directory
   */
  public static boolean containsAtLeastOneNotIgnoredFile(Path path) {
    boolean res = true;
    if (Files.isDirectory(path)) {
      try {
        if (isDirectoryEmpty(path)) {
          res = false;
        } else {
          // FIXME 20170309 hsilva: the following is very expensive to use
          // ValidFilesCounter visitor = new ValidFilesCounter();
          // Files.walkFileTree(path, visitor);
          // int validFiles = visitor.getValidFiles();
          // if (validFiles == 0) {
          // res = false;
          // }
        }
      } catch (Exception e) {
        LOGGER.debug("Error while checking if directory contains at least on valid file: {}", path, e);
      }
    }
    return res;
  }

  private static boolean isDirectoryEmpty(Path path) {
    boolean isEmpty = true;
    Stream<Path> list = null;
    try {
      list = Files.list(path);
      if (list.findFirst().isPresent()) {
        isEmpty = false;
      }
    } catch (IOException e) {
      // do nothing
    } finally {
      if (list != null) {
        list.close();
      }
    }
    return isEmpty;
  }
}
