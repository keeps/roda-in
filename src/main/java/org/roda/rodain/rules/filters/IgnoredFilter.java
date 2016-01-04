package org.roda.rodain.rules.filters;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 04-01-2016.
 */
public class IgnoredFilter {
  private static Set<String> rules = new HashSet<>();

  private IgnoredFilter() {
  }

  public static void addIgnoreRule(String rule) {
    rules.add(rule);
  }

  public static boolean isIgnored(String value) {
    boolean result = false;
    for (String rule : rules) {
      Pattern p = Pattern.compile(rule);
      Matcher m = p.matcher(value);
      if (m.matches()) {
        result = true;
        break;
      }
    }
    return result;
  }

  public static boolean isIgnored(Path path) {
    int i = path.getNameCount() - 1;
    boolean result = false;
    for (; i >= 0; i--) {
      if (isIgnored(path.getName(i).toString())) {
        result = true;
        break;
      }
    }
    return result;
  }
}
