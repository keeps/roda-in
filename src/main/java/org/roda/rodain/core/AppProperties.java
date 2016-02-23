package org.roda.rodain.core;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.filechooser.FileSystemView;

import org.roda.rodain.utils.FolderBasedUTF8Control;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28/12/2015.
 */
public class AppProperties {
  private static final String ENV_VARIABLE = "RODAIN_HOME";
  private static final String CONFIGFOLDER = "roda-in";
  public static final Path rodainPath = getRodainPath();
  private static final Logger log = LoggerFactory.getLogger(AppProperties.class.getName());
  private static Properties style = load("styles"), config = load("config"), ext_config,
    descLevels = load("roda-description-levels-hierarchy");
  private static ResourceBundle resourceBundle;
  public static Locale locale;

  private AppProperties() {

  }

  /**
   * Creates the external properties files if they don't exist. Loads the
   * external properties files.
   */
  public static void initialize() {
    Path configPath = rodainPath.resolve("config.properties");

    try {
      // create folder in home if it doesn't exist
      if (!Files.exists(rodainPath)) {
        rodainPath.toFile().mkdir();
      }
      // copy config file
      if (!Files.exists(configPath)) {
        Files.copy(ClassLoader.getSystemResourceAsStream("properties/config.properties"), configPath);
      } else { // if the file already exists, we need to check if it's missing
               // any properties
        Properties internal = load("config");
        Properties external = new Properties();
        external.load(new FileInputStream(configPath.toFile()));
        boolean store = false;
        for (Object key : internal.keySet()) {
          if (!external.containsKey(key)) {
            external.put(key, internal.get(key));
            store = true;
          }
        }
        if (store) {
          OutputStream out = new FileOutputStream(configPath.toFile());
          external.store(out, "Stored missing properties");
        }
      }
      // copy metadata templates
      String templatesRaw = config.getProperty("metadata.templates");
      String[] templates = templatesRaw.split(",");
      for (String templ : templates) {
        String templateName = "metadata.template." + templ.trim() + ".file";
        String fileName = config.getProperty(templateName);
        // if (!Files.exists(rodainPath.resolve(fileName)))
        Files.copy(ClassLoader.getSystemResourceAsStream(fileName), rodainPath.resolve(fileName),
          StandardCopyOption.REPLACE_EXISTING);
      }

      // load config
      ext_config = new Properties();
      ext_config.load(new FileInputStream(configPath.toFile()));

      String appLanguage = getConfig("app.language");
      if (appLanguage != null) {
        locale = Locale.forLanguageTag(appLanguage);
      } else {
        locale = Locale.getDefault();
      }

      resourceBundle = ResourceBundle.getBundle("properties/lang", locale, new FolderBasedUTF8Control());
    } catch (IOException e) {
      log.error("Error copying config file", e);
    } catch (MissingResourceException e) {
      locale = Locale.forLanguageTag("en");
      resourceBundle = ResourceBundle.getBundle("properties/lang", locale, new FolderBasedUTF8Control());
    }
  }

  private static Path getRodainPath() {
    String envString = System.getenv(ENV_VARIABLE);
    if (envString != null) {
      Path envPath = Paths.get(envString);
      if (Files.exists(envPath) && Files.isDirectory(envPath)) {
        return envPath.resolve(CONFIGFOLDER);
      }
    }
    String documentsString = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
    Path documentsPath = Paths.get(documentsString);
    return documentsPath.resolve(CONFIGFOLDER);
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

  public static String getMetadataFile(String propertyName) {
    String completeKey = "metadata.template." + propertyName + ".file";
    try {
      if (ext_config.containsKey(completeKey)) {
        Path filePath = rodainPath.resolve(ext_config.getProperty(completeKey));
        if (Files.exists(filePath)) {
          return Utils.readFile(filePath.toString(), Charset.defaultCharset());
        }
      }
      String fileName = config.getProperty(completeKey);
      InputStream contentStream = ClassLoader.getSystemResource(fileName).openStream();
      return Utils.convertStreamToString(contentStream);
    } catch (IOException e) {
      log.error("Error reading metadata file", e);
    }
    return "";
  }

  /**
   * @param key
   *          The name of the property (style)
   * @return The value of the property (style)
   */
  public static String getStyle(String key) {
    return style.getProperty(key);
  }

  /**
   * @param key
   *          The name of the property (config)
   * @return The value of the property (config)
   */
  public static String getConfig(String key) {
    if (ext_config.containsKey(key))
      return ext_config.getProperty(key);
    return config.getProperty(key);
  }

  /**
   * @param key
   *          The name of the property (description levels hierarchy)
   * @return The value of the property (description levels hierarchy)
   */
  public static String getDescLevels(String key) {
    return descLevels.getProperty(key);
  }

  /**
   * Uses ResourceBundle to get the language specific string
   *
   * @param key
   *          The name of the property
   * @return The value of the property using
   */
  public static String getLocalizedString(String key) {
    return resourceBundle.getString(key);
  }
}
