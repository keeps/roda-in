package org.roda.rodain;

import java.io.PrintStream;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.utils.LoggingOutputStream;
import org.roda.rodain.ui.RodaInApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As the name says, it's application main class, the one with a main static
 * method & the one and only one that should be invoked to start the application
 * 
 * @author Andre Pereira apereira@keep.pt
 * @since 19/01/2016.
 */
public class Main {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

  public static void main(String[] args) {
    // get the java version
    /* String javaString = Runtime.class.getPackage().getSpecificationVersion();
    double javaVersion = Double.parseDouble(javaString);
    if (javaVersion < 1.8) {
      String format = I18n.t(Constants.I18N_MAIN_USE_JAVA8);
      LOGGER.error(String.format(format, javaVersion));
      return;
    }
     */
    System.setErr(new PrintStream(new LoggingOutputStream()));

    RodaInApplication.start(args);
  }
}
