package org.roda.rodain.core;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.roda.rodain.utils.FolderBasedUTF8Control;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28/12/2015.
 */
public class AppProperties {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class.getName());

  private static final Path rodainPath = computeRodainPath();
  private static final String ENV_VARIABLE = "RODAIN_HOME";
  private static final String CONFIGFOLDER = "roda-in";
  private static PropertiesConfiguration style = load("styles"), config = load("config"), ext_config,
    descLevels = load("roda-description-levels-hierarchy");
  private static PropertiesConfiguration start_ext_config;
  private static ResourceBundle resourceBundle, defaultResourceBundler;
  private static Locale locale;

  private static Set<Path> allSchemas;

  private AppProperties() {
  }

  /**
   * Creates the external properties files if they don't exist. Loads the
   * external properties files.
   */
  public static void initialize() {
    Path configPath = rodainPath.resolve("config.properties");

    try {
      createFolderStructure();
      // copy config file
      if (!Files.exists(configPath)) {
        Files.copy(ClassLoader.getSystemResourceAsStream("properties/config.properties"), configPath);
      } else { // if the file already exists, we need to check if it's missing
               // any properties
        PropertiesConfiguration internal = load("config");
        PropertiesConfiguration external = new PropertiesConfiguration();
        external.load(new FileReader(configPath.toFile()));
        boolean store = false;
        Iterator<String> keys = internal.getKeys();
        while (keys.hasNext()) {
          String key = keys.next();
          if (!external.containsKey(key)) {
            external.addProperty(key, internal.getProperty(key));
            store = true;
          }
          if (key.startsWith("metadata.template.") && internal.getProperty(key) == null) {
            external.setProperty(key, internal.getProperty(key));
            store = true;
          }
        }
        if (store) {
          OutputStream out = new FileOutputStream(configPath.toFile());
          external.save(out);
        }
      }
      // copy metadata templates
      String templatesRaw = getConfig("metadata.templates");
      String[] templates = templatesRaw.split(",");
      for (String templ : templates) {
        String templateName = "metadata.template." + templ.trim() + ".file";
        String fileName = config.getString(templateName);
        Files.copy(ClassLoader.getSystemResourceAsStream("templates/" + fileName),
          rodainPath.resolve("samples").resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        // copy the sample to the templates folder too, if it doesn't exist
        // already
        if (!Files.exists(rodainPath.resolve("templates").resolve(fileName))) {
          Files.copy(ClassLoader.getSystemResourceAsStream("templates/" + fileName),
            rodainPath.resolve("templates").resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
      }

      String typesRaw = getConfig("metadata.types");
      String[] types = typesRaw.split(",");
      for(String type :types) {
        String schemaName = "metadata.type." + type.trim() + ".schema";
        String schemaFileName = config.getString(schemaName);
        if(schemaFileName == null || schemaFileName.length() == 0){
          continue;
        }
        Files.copy(ClassLoader.getSystemResourceAsStream("templates/" + schemaFileName),
                rodainPath.resolve("schemas").resolve(schemaFileName), StandardCopyOption.REPLACE_EXISTING);
      }
      // ensure that the xlink.xsd and xml.xsd files are in the application home
      // folder
      Files.copy(ClassLoader.getSystemResourceAsStream("xlink.xsd"), rodainPath.resolve("schemas").resolve("xlink.xsd"),
        StandardCopyOption.REPLACE_EXISTING);
      Files.copy(ClassLoader.getSystemResourceAsStream("xml.xsd"), rodainPath.resolve("schemas").resolve("xml.xsd"),
        StandardCopyOption.REPLACE_EXISTING);

      // get all schema files in the roda-in home directory
      allSchemas = new HashSet<>();
      File folder = rodainPath.toFile();
      File[] listOfFiles = folder.listFiles();

      for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile()) {
          File file = listOfFiles[i];
          if (file.getName().endsWith(".xsd")) {
            allSchemas.add(Paths.get(file.getPath()));
          }
        }
      }
      // load config
      ext_config = new PropertiesConfiguration();
      ext_config.load(new FileInputStream(configPath.toFile()));
      // keep the starting configuration to use when saving
      start_ext_config = new PropertiesConfiguration();
      start_ext_config.load(new FileInputStream(configPath.toFile()));

      String appLanguage = getConfig("app.language");
      if (appLanguage != null && !"".equals(appLanguage)) {
        locale = Locale.forLanguageTag(appLanguage);
      } else {
        locale = Locale.getDefault();
      }

      resourceBundle = ResourceBundle.getBundle("properties/lang", locale, new FolderBasedUTF8Control());
      defaultResourceBundler = ResourceBundle.getBundle("properties/lang", Locale.ENGLISH,
        new FolderBasedUTF8Control());
    } catch (IOException e) {
      LOGGER.error("Error copying config file", e);
    } catch (MissingResourceException e) {
      LOGGER.info("Can't find the language resource for the current locale", e);
      locale = Locale.forLanguageTag("en");
      resourceBundle = ResourceBundle.getBundle("properties/lang", locale, new FolderBasedUTF8Control());
    } catch (ConfigurationException e) {
      LOGGER.error("Error loading the config file", e);
    } finally {
      // force the default locale for the JVM
      Locale.setDefault(locale);
    }
  }

  private static void createFolderStructure() {
    // create folder in home if it doesn't exist
    if (!Files.exists(rodainPath)) {
      rodainPath.toFile().mkdir();
    }
    // create schemas folder
    if (!Files.exists(rodainPath.resolve("schemas"))) {
      rodainPath.resolve("schemas").toFile().mkdir();
    }
    // create templates folder
    if (!Files.exists(rodainPath.resolve("templates"))) {
      rodainPath.resolve("templates").toFile().mkdir();
    }
    // create samples folder
    if (!Files.exists(rodainPath.resolve("samples"))) {
      rodainPath.resolve("samples").toFile().mkdir();
    }
    // create LOGGER folder
    if (!Files.exists(rodainPath.resolve("log"))) {
      rodainPath.resolve("log").toFile().mkdir();
    }
  }

  private static Path computeRodainPath() {
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

  /**
   * @return The path of the application folder.
   */
  public static Path getRodainPath() {
    return rodainPath;
  }

  /**
   * @return The locale of the application.
   */
  public static Locale getLocale() {
    return locale;
  }

  private static PropertiesConfiguration load(String fileName) {
    PropertiesConfiguration result = null;
    try {
      result = new PropertiesConfiguration("properties/" + fileName + ".properties");
    } catch (ConfigurationException e) {
      LOGGER.error("Error loading the config file", e);
    }
    return result;
  }

  /**
   * @param templateName
   *          The name of the template
   * @return The content of the template file
   */
  public static String getMetadataFile(String templateName) {
    String completeKey = "metadata.template." + templateName + ".file";
    return getFile(completeKey);
  }

  /**
   * @param templateType
   *          The name of the template
   * @return The content of the schema file associated to the template
   */
  public static String getSchemaFile(String templateType) {
    String completeKey = "metadata.type." + templateType + ".schema";
    if (ext_config.containsKey(completeKey)) {
      Path filePath = rodainPath.resolve("schemas").resolve(ext_config.getString(completeKey));
      if (Files.exists(filePath)) {
        try {
          return Utils.readFile(filePath.toString(), Charset.defaultCharset());
        } catch (IOException e) {
          LOGGER.error("Unable to get schema file: " + filePath, e);
        }
      }
    }
    return null;
  }

  /**
   * @param templateType
   *          The name of the template
   * @return The path of the schema file associated to the template
   */
  public static Path getSchemaPath(String templateType) {
    String completeKey = "metadata.type." + templateType + ".schema";
    if (ext_config.containsKey(completeKey)) {
      Path filePath = rodainPath.resolve("schemas").resolve(ext_config.getString(completeKey));
      if (Files.exists(filePath)) {
        return filePath;
      }
    }
    String fileName = config.getString(completeKey);
    URL temp = ClassLoader.getSystemResource("templates/" + fileName);
    if (temp != null)
      return Paths.get(temp.getPath());
    else
      return null;
  }

  private static String getFile(String completeKey) {
    try {
      if (ext_config.containsKey(completeKey)) {
        Path filePath = rodainPath.resolve("templates").resolve(ext_config.getString(completeKey));
        if (Files.exists(filePath)) {
          return Utils.readFile(filePath.toString(), Charset.defaultCharset());
        }
      }
      String fileName = config.getString(completeKey);
      URL temp = ClassLoader.getSystemResource("templates/" + fileName);
      if (temp == null) {
        return "";
      }
      InputStream contentStream = temp.openStream();
      return Utils.convertStreamToString(contentStream);
    } catch (IOException e) {
      LOGGER.error("Error reading metadata file", e);
    }
    return "";
  }

  /**
   * @param key
   *          The name of the property (style)
   * @return The value of the property (style)
   */
  public static String getStyle(String key) {
    return style.getString(key);
  }

  /**
   * @param key
   *          The name of the property (config)
   * @return The value of the property (config)
   */
  public static String getConfig(String key) {
    Object res;
    if (ext_config != null && ext_config.containsKey(key)) {
      res = ext_config.getProperty(key);
    } else {
      res = config.getProperty(key);
    }
    if (res == null)
      return null;
    if (res instanceof String) {
      return (String) res;
    }
    // if it isn't a string then it must be a list Ex: a,b,c,d
    return String.join(",", (List<String>) res);
  }

  /**
   * @param key
   *          The name of the property (description levels hierarchy)
   * @return The value of the property (description levels hierarchy)
   */
  public static String getDescLevels(String key) {
    Object res = descLevels.getProperty(key);
    if (res instanceof String) {
      return (String) res;
    }
    // if it isn't a string then it must be a list Ex: a,b,c,d
    return String.join(",", descLevels.getStringArray(key));
  }

  public static String getLocalizedString(String key, String languageTag) {
    ResourceBundle localResourceBundle = ResourceBundle.getBundle("properties/lang", Locale.forLanguageTag(languageTag),
      new FolderBasedUTF8Control());
    String result = null;

    try {
      result = localResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      LOGGER.warn(String.format("Missing translation for %s in language: %s", key,
        localResourceBundle.getLocale().getDisplayName()));
      result = getLocalizedString(key);
    }
    return result;
  }

  /**
   * Uses ResourceBundle to get the language specific string
   *
   * @param key
   *          The name of the property
   * @return The value of the property using
   */
  public static String getLocalizedString(String key) {
    String result = null;
    try {
      result = resourceBundle.getString(key);
    } catch (MissingResourceException e) {
      LOGGER.warn(String.format("Missing translation for %s in language: %s", key, locale.getDisplayName()));
      try {
        result = defaultResourceBundler.getString(key);
      } catch (Exception e1) {
        LOGGER.warn(String.format("Missing translation for %s in language: %s", key, Locale.ENGLISH));
      }
    }
    return result;
  }

  /**
   * Sets the value of a configuration.
   * 
   * @param key
   *          The key of the property.
   * @param value
   *          The value of the property
   */
  public static void setConfig(String key, String value) {
    ext_config.setProperty(key, value);
  }

  /**
   * Saves the configuration to a file in the application home folder.
   */
  public static void saveConfig() {
    try {
      Path configPath = rodainPath.resolve("config.properties");
      PropertiesConfiguration temp = new PropertiesConfiguration();
      temp.load(new FileReader(configPath.toFile()));
      if (ext_config != null) {
        HashSet<String> keys = new HashSet<>();
        ext_config.getKeys().forEachRemaining(keys::add);
        temp.getKeys().forEachRemaining(keys::add);
        keys.forEach(s -> {
          // Add new properties to the ext_config
          if (temp.containsKey(s) && !ext_config.containsKey(s)) {
            ext_config.addProperty(s, temp.getProperty(s));
          } else {
            // check if there's any property in the current file that is
            // different
            // from the ones we loaded in the beginning of the execution of the
            // application.
            if (temp.containsKey(s) && start_ext_config.containsKey(s)
              && !temp.getProperty(s).equals(start_ext_config.getProperty(s))) {
              // Set the property to keep the changes made outside the
              // application
              ext_config.setProperty(s, temp.getProperty(s));
            }
          }
        });
      }
      ext_config.save(configPath.toFile());
    } catch (ConfigurationException | FileNotFoundException e) {
      LOGGER.error("Error loading the config file", e);
    }
  }
}
