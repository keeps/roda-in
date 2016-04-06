package org.roda.rodain.core;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
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

  /**
   * Starts the application.
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.setProperty("roda-in-log-path", AppProperties.getRodainPath().resolve("roda-in.log").toString());
    System.setProperty("roda-in-archive-log-path", AppProperties.getRodainPath().resolve("Log archive").toString());
    configureLogback();

    // get the java version
    String javaString = Runtime.class.getPackage().getSpecificationVersion();
    double javaVersion = Double.parseDouble(javaString);
    if (javaVersion < 1.8) {
      String format = I18n.t("Main.useJava8");
      log.error(String.format(format, javaVersion));
      return;
    }

    System.setErr(new PrintStream(new LoggingOutputStream()));

    RodaIn.startApp(args);
  }

  private static void configureLogback() {
    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();
      configurator.doConfigure(ClassLoader.getSystemResource("logback.xml"));
    } catch (JoranException e) {
      log.error("Error configuring logback", e);
    }
  }
}
