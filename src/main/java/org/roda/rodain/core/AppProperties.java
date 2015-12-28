package org.roda.rodain.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28/12/2015.
 */
public class AppProperties {
  private static final Logger log = LoggerFactory.getLogger(AppProperties.class.getName());
  private static Properties style = load("styles"), config = load("config"), descLevels = load("roda-description-levels-hierarchy");

  private AppProperties() {

  }

  private static Properties load(String fileName) {
    Properties result = null;
    try {
      result = new Properties();
      result.load(ClassLoader.getSystemResource("properties/" + fileName + ".properties").openStream());
    } catch (IOException e) {
      log.error("Error while loading properties", e);
    }
    return result;
  }
  /**
   * @param key The name of the property (style)
   * @return The value of the property (style)
   */
  public static String getStyle(String key) {
    return style.getProperty(key);
  }

  /**
   * @param key The name of the property (config)
   * @return The value of the property (config)
   */
  public static String getConfig(String key) {
    return config.getProperty(key);
  }

  /**
   * @param key The name of the property (description levels hierarchy)
   * @return The value of the property (description levels hierarchy)
   */
  public static String getDescLevels(String key) {
    return descLevels.getProperty(key);
  }
}
