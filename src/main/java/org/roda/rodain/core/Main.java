package org.roda.rodain.core;

import org.roda.rodain.utils.LoggingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/01/2016.
 */
public class Main {
  private static final Logger log = LoggerFactory.getLogger(RodaIn.class.getName());
  public static void main(String[] args) {
    // get the java version
    String javaString = Runtime.class.getPackage().getSpecificationVersion();
    double javaVersion = Double.parseDouble(javaString);
    if (javaVersion < 1.8) {
      String format = AppProperties.getLocalizedString("Main.useJava8");
      log.error(String.format(format, javaVersion));
      return;
    }

    System.setErr(new PrintStream(new LoggingOutputStream()));

    RodaIn.startApp(args);
  }
}
