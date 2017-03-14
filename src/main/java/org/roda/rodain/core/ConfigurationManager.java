package org.roda.rodain.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.rules.filters.IgnoredFilter;
import org.roda.rodain.core.utils.FolderBasedUTF8Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Class that contains all configurations (and configurations related methods)
 * as well as utility methods to get files from RODA-in home, etc.
 * 
 * @author Andre Pereira apereira@keep.pt
 * @since 28/12/2015.
 * @since 2017-03-09 (was renamed to ConfigurationManager)
 */
public class ConfigurationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class.getName());

  private static final Path rodainPath = computeRodainPath();
  private static Path schemasPath, templatesPath, logPath, metadataPath, helpPath, externalConfigPath,
    externalAppConfigPath;
  private static PropertiesConfiguration style = load("styles"), internalConfig = load("config"), externalConfig,
    externalAppConfig;
  private static PropertiesConfiguration startExternalConfig, startExternalAppConfig;
  private static ResourceBundle resourceBundle, defaultResourceBundle, helpBundle, defaultHelpBundle;
  private static Locale locale;

  private static Set<Path> allSchemas;

  private ConfigurationManager() {
  }

  private static Path computeRodainPath() {
    String envString = System.getenv(Constants.RODAIN_HOME_ENV_VARIABLE);
    if (envString != null) {
      Path envPath = Paths.get(envString);
      if (Files.exists(envPath) && Files.isDirectory(envPath)) {
        Path confPath = envPath.resolve(Constants.RODAIN_CONFIG_FOLDER);
        try {
          FileUtils.deleteDirectory(confPath.toFile());
        } catch (IOException e) {
          LOGGER.debug("Unable to remove configuration directory '{}'", confPath, e);
        }
        return confPath;
      }
    }
    String documentsString = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
    Path documentsPath = Paths.get(documentsString);
    return documentsPath.resolve(Constants.RODAIN_CONFIG_FOLDER);
  }

  /**
   * Creates the external properties files if they don't exist. Loads the
   * external properties files.
   */
  public static void initialize() {
    externalConfigPath = rodainPath.resolve(Constants.CONFIG_FILE);
    externalAppConfigPath = rodainPath.resolve(Constants.APP_CONFIG_FILE);

    try {
      createBaseFolderStructure();

      configureLogback();

      copyConfigFiles();

      copyMetadataTemplates();

      copyAndProcessSchemas();

      loadConfigs();

      processLanguageAndOtherResources();

      processIgnoreFilesInfo();

      copyHelpFiles();

    } catch (IOException e) {
      LOGGER.error("Error creating folders or copying config files", e);
    } catch (MissingResourceException e) {
      LOGGER.error("Can't find the language resource for the current locale", e);
      locale = Locale.forLanguageTag("en");
      resourceBundle = ResourceBundle.getBundle("properties/lang", locale, new FolderBasedUTF8Control());
      helpBundle = ResourceBundle.getBundle("properties/help", locale, new FolderBasedUTF8Control());
    } catch (Throwable e) {
      LOGGER.error("Error loading the config file", e);
    } finally {
      // force the default locale for the JVM
      Locale.setDefault(locale);
    }
  }

  private static void createBaseFolderStructure() throws IOException {
    // create folder in home if it doesn't exist
    if (!Files.exists(rodainPath)) {
      Files.createDirectory(rodainPath);
    }
    // create schemas folder
    schemasPath = rodainPath.resolve(Constants.FOLDER_SCHEMAS);
    if (!Files.exists(schemasPath)) {
      Files.createDirectory(schemasPath);
    }
    // create templates folder
    templatesPath = rodainPath.resolve(Constants.FOLDER_TEMPLATES);
    if (!Files.exists(templatesPath)) {
      Files.createDirectory(templatesPath);
    }
    // create LOGGER folder
    logPath = rodainPath.resolve(Constants.FOLDER_LOG);
    if (!Files.exists(logPath)) {
      Files.createDirectory(logPath);
    }
    // create metadata folder
    metadataPath = rodainPath.resolve(Constants.FOLDER_METADATA);
    if (!Files.exists(metadataPath)) {
      Files.createDirectory(metadataPath);
    }
    // create help folder
    helpPath = rodainPath.resolve(Constants.FOLDER_HELP);
    if (!Files.exists(helpPath)) {
      Files.createDirectory(helpPath);
    }
  }

  private static void configureLogback() {
    System.setProperty("rodain.log", logPath.toString());
    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();
      // 20170314 hsilva: logback file was named differently from what logback
      // usually expects in order to avoid auto-loading by logback as we want to
      // place the log file under roda-in home
      configurator.doConfigure(ClassLoader.getSystemResource("lllogback.xml"));
    } catch (JoranException e) {
      LOGGER.error("Error configuring logback", e);
    }
  }

  private static void copyConfigFiles() throws IOException {
    if (!Files.exists(externalConfigPath)) {
      Files.copy(ClassLoader.getSystemResourceAsStream("properties/" + Constants.CONFIG_FILE), externalConfigPath);
    }

    if (!Files.exists(externalAppConfigPath)) {
      Files.copy(ClassLoader.getSystemResourceAsStream("properties/" + Constants.APP_CONFIG_FILE),
        externalAppConfigPath);
    }
  }

  private static void copyMetadataTemplates() throws IOException {
    String templatesRaw = getConfig(Constants.CONF_K_METADATA_TEMPLATES);
    String[] templates = templatesRaw.split(Constants.MISC_COMMA);
    for (String templ : templates) {
      String templateName = Constants.CONF_K_PREFIX_METADATA + templ.trim() + Constants.CONF_K_SUFIX_TEMPLATE;
      String fileName = internalConfig.getString(templateName);
      // copy the sample to the templates folder too, if it doesn't exist
      // already
      if (!Files.exists(templatesPath.resolve(fileName))) {
        Files.copy(
          ClassLoader.getSystemResourceAsStream(Constants.FOLDER_TEMPLATES + Constants.MISC_FWD_SLASH + fileName),
          templatesPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  private static void copyAndProcessSchemas() throws IOException {
    String typesRaw = getConfig(Constants.CONF_K_METADATA_TYPES);
    String[] types = typesRaw.split(Constants.MISC_COMMA);
    for (String type : types) {
      String schemaName = Constants.CONF_K_PREFIX_METADATA + type.trim() + Constants.CONF_K_SUFIX_SCHEMA;
      String schemaFileName = internalConfig.getString(schemaName);
      if (schemaFileName == null || schemaFileName.length() == 0) {
        continue;
      }
      Files.copy(
        ClassLoader.getSystemResourceAsStream(Constants.FOLDER_TEMPLATES + Constants.MISC_FWD_SLASH + schemaFileName),
        schemasPath.resolve(schemaFileName), StandardCopyOption.REPLACE_EXISTING);
    }

    // ensure that the xlink.xsd and xml.xsd files are in the application home
    // folder
    Files.copy(ClassLoader.getSystemResourceAsStream("xlink.xsd"), schemasPath.resolve("xlink.xsd"),
      StandardCopyOption.REPLACE_EXISTING);
    Files.copy(ClassLoader.getSystemResourceAsStream("xml.xsd"), schemasPath.resolve("xml.xsd"),
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
  }

  private static void loadConfigs() throws ConfigurationException, FileNotFoundException {
    externalConfig = new PropertiesConfiguration();
    externalConfig.load(new FileInputStream(externalConfigPath.toFile()));
    externalAppConfig = new PropertiesConfiguration();
    externalAppConfig.load(new FileInputStream(externalAppConfigPath.toFile()));

    // keep the starting configuration to use when saving
    startExternalConfig = new PropertiesConfiguration();
    startExternalConfig.load(new FileInputStream(externalConfigPath.toFile()));
    startExternalAppConfig = new PropertiesConfiguration();
    startExternalAppConfig.load(new FileInputStream(externalAppConfigPath.toFile()));
  }

  private static void processLanguageAndOtherResources() {
    String appLanguage = getAppConfig("app.language");
    locale = parseLocale(appLanguage);
    resourceBundle = ResourceBundle.getBundle("properties/lang", locale, new FolderBasedUTF8Control());
    helpBundle = ResourceBundle.getBundle("properties/help", locale, new FolderBasedUTF8Control());
    defaultResourceBundle = ResourceBundle.getBundle("properties/lang", Locale.ENGLISH, new FolderBasedUTF8Control());
    defaultHelpBundle = ResourceBundle.getBundle("properties/help", Locale.ENGLISH, new FolderBasedUTF8Control());
  }

  public static Locale parseLocale(String localeString) {
    Locale locale = Locale.ENGLISH;
    if (StringUtils.isNotBlank(localeString)) {
      String[] localeArgs = localeString.split("_");

      if (localeArgs.length == 1) {
        locale = new Locale(localeArgs[0]);
      } else if (localeArgs.length == 2) {
        locale = new Locale(localeArgs[0], localeArgs[1]);
      } else if (localeArgs.length == 3) {
        locale = new Locale(localeArgs[0], localeArgs[1], localeArgs[2]);
      }
    }

    return locale;
  }

  private static void processIgnoreFilesInfo() {
    String ignorePatterns = getAppConfig(Constants.CONF_K_IGNORED_FILES);
    if (ignorePatterns != null && !ignorePatterns.trim().equalsIgnoreCase("")) {
      String[] patterns = ignorePatterns.split(Constants.MISC_COMMA);
      for (String pattern : patterns) {
        IgnoredFilter.addIgnoreRule(pattern.trim());
      }
    }
  }

  private static void copyHelpFiles() {
    final File jarFile = new File(
      ConfigurationManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    if (jarFile.isFile()) { // Run with JAR file
      try (final JarFile jar = new JarFile(jarFile)) {
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (entry.getName().startsWith(Constants.FOLDER_HELP + Constants.MISC_FWD_SLASH) && !entry.isDirectory()) {
            InputStream input = jar.getInputStream(entry);
            Files.copy(input, rodainPath.resolve(entry.getName()), StandardCopyOption.REPLACE_EXISTING);
            input.close();
          }

        }
      } catch (IOException e) {
        LOGGER.error("Error while copying help files", e);
      }
    } else { // Run with IDE
      final URL url = ConfigurationManager.class.getResource(Constants.MISC_FWD_SLASH + Constants.FOLDER_HELP);
      if (url != null) {
        try {
          final File apps = new File(url.toURI());
          for (File app : apps.listFiles()) {
            Files.copy(app.toPath(), helpPath.resolve(app.getName()), StandardCopyOption.REPLACE_EXISTING);
          }
        } catch (URISyntaxException | IOException e) {
          LOGGER.error("Error while copying help files", e);
        }
      }
    }
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

  public static String getHelpFile() {
    Path helpFile = helpPath.resolve("help_" + getLocale().toString() + ".html");
    if (!Files.exists(helpFile)) {
      helpFile = helpPath.resolve("help_en.html");
      if (!Files.exists(helpFile)) {
        helpFile = helpPath.resolve("help.html");
      }
    }
    return "file://" + helpFile.toString();
  }

  /**
   * @param templateName
   *          The name of the template
   * @return The content of the template file
   */
  public static String getTemplateContent(String templateName) {
    String completeKey = Constants.CONF_K_PREFIX_METADATA + templateName + Constants.CONF_K_SUFIX_TEMPLATE;
    return getFile(completeKey);
  }

  /**
   * @param templateType
   *          The name of the template
   * @return The content of the schema file associated to the template
   */
  public static String getSchemaFile(String templateType) {
    String completeKey = Constants.CONF_K_PREFIX_METADATA + templateType + Constants.CONF_K_SUFIX_SCHEMA;
    if (externalConfig.containsKey(completeKey)) {
      Path filePath = schemasPath.resolve(externalConfig.getString(completeKey));
      if (Files.exists(filePath)) {
        try {
          return ControllerUtils.readFile(filePath);
        } catch (IOException e) {
          LOGGER.error("Unable to get schema file '{}'", filePath, e);
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
    String completeKey = Constants.CONF_K_PREFIX_METADATA + templateType + Constants.CONF_K_SUFIX_SCHEMA;
    if (externalConfig.containsKey(completeKey)) {
      Path filePath = schemasPath.resolve(externalConfig.getString(completeKey));
      if (Files.exists(filePath)) {
        return filePath;
      }
    }
    String fileName = internalConfig.getString(completeKey);
    URL temp = ClassLoader.getSystemResource(Constants.FOLDER_TEMPLATES + Constants.MISC_FWD_SLASH + fileName);
    if (temp != null)
      return Paths.get(temp.getPath());
    else
      return null;
  }

  private static String getFile(String completeKey) {
    try {
      if (externalConfig.containsKey(completeKey)) {
        Path filePath = templatesPath.resolve(externalConfig.getString(completeKey));
        if (Files.exists(filePath)) {
          return ControllerUtils.readFile(filePath);
        }
      }
      String fileName = internalConfig.getString(completeKey);
      URL temp = ClassLoader.getSystemResource(Constants.FOLDER_TEMPLATES + Constants.MISC_FWD_SLASH + fileName);
      if (temp == null) {
        return "";
      }
      InputStream contentStream = temp.openStream();
      return ControllerUtils.convertStreamToString(contentStream);
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
   * Method to return config related to metadata (it composes the key to look
   * for by prefixing the method argument with "metadata.")
   * 
   * @param partialKey
   *          the end part of the key to look for
   */
  public static String getMetadataConfig(String partialKey) {
    return getConfig(Constants.CONF_K_PREFIX_METADATA + partialKey);
  }

  /**
   * @param key
   *          The name of the property (config)
   * @return The value of the property (config)
   */
  public static String getConfig(String key) {
    Object res;
    if (externalConfig != null && externalConfig.containsKey(key)) {
      res = externalConfig.getProperty(key);
    } else {
      res = internalConfig.getProperty(key);
    }
    if (res == null) {
      return null;
    }
    if (res instanceof String) {
      return (String) res;
    }
    // if it isn't a string then it must be a list Ex: a,b,c,d
    return String.join(Constants.MISC_COMMA, (List<String>) res);
  }

  protected static String getLocalizedString(String key, String languageTag) {
    ResourceBundle localResourceBundle = ResourceBundle.getBundle("properties/lang", Locale.forLanguageTag(languageTag),
      new FolderBasedUTF8Control());
    String result = null;

    try {
      result = localResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      LOGGER.trace("Missing translation for {} in language: {}", key, localResourceBundle.getLocale().getDisplayName());
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
  protected static String getLocalizedString(String key) {
    String result = null;
    try {
      result = resourceBundle.getString(key);
    } catch (MissingResourceException e) {
      LOGGER.trace("Missing translation for {} in language: {}", key, locale.getDisplayName());
      try {
        result = defaultResourceBundle.getString(key);
      } catch (Exception e1) {
        LOGGER.trace("Missing translation for {} in language: {}", key, Locale.ENGLISH);
      }
    }
    return result;
  }

  protected static String getLocalizedHelp(String key) {
    String result = null;
    try {
      result = helpBundle.getString(key);
      if ("".equals(result)) {
        throw new MissingResourceException("", "", key);
      }
    } catch (MissingResourceException e) {
      LOGGER.trace("Missing translation for help {} in language: {}", key, locale.getDisplayName());
      try {
        result = defaultHelpBundle.getString(key);
      } catch (Exception e1) {
        LOGGER.trace("Missing translation for help {} in language: {}", key, Locale.ENGLISH);
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
    setConfig(key, value, false);
  }

  /**
   * Sets the value of a configuration.
   * 
   * @param key
   *          The key of the property.
   * @param value
   *          The value of the property
   * @param saveToFile
   *          true if after setting the key/value, is supposed to save that
   *          information to file
   */
  public static void setConfig(String key, String value, boolean saveToFile) {
    externalConfig.setProperty(key, value);
    if (saveToFile) {
      saveConfig();
    }
  }

  public static void setAppConfig(String key, String value, boolean saveToFile) {
    externalAppConfig.setProperty(key, value);
    if (saveToFile) {
      saveAppConfig();
    }
  }

  public static void setAppConfig(String key, String value) {
    setAppConfig(key, value, false);
  }

  public static String getAppConfig(String key) {
    Object res = null;
    if (externalAppConfig != null && externalAppConfig.containsKey(key)) {
      res = externalAppConfig.getProperty(key);
    }
    if (res == null) {
      return null;
    }
    if (res instanceof String) {
      return (String) res;
    }
    // if it isn't a string then it must be a list Ex: a,b,c,d
    return String.join(Constants.MISC_COMMA, (List<String>) res);
  }

  /**
   * Saves the configuration to a file in the application home folder.
   */
  public static void saveConfig() {
    try {
      PropertiesConfiguration temp = new PropertiesConfiguration();
      temp.load(new FileReader(externalConfigPath.toFile()));
      if (externalConfig != null) {
        HashSet<String> keys = new HashSet<>();
        externalConfig.getKeys().forEachRemaining(keys::add);
        temp.getKeys().forEachRemaining(keys::add);
        keys.forEach(s -> {
          // Add new properties to the ext_config
          if (temp.containsKey(s) && !externalConfig.containsKey(s)) {
            externalConfig.addProperty(s, temp.getProperty(s));
          } else {
            // check if there's any property in the current file that is
            // different from the ones we loaded in the beginning of the
            // execution of the application.
            if (temp.containsKey(s) && startExternalConfig.containsKey(s)
              && !temp.getProperty(s).equals(startExternalConfig.getProperty(s))) {
              // Set the property to keep the changes made outside the
              // application
              externalConfig.setProperty(s, temp.getProperty(s));
            }
          }
        });
      }
      externalConfig.save(externalConfigPath.toFile());
    } catch (ConfigurationException | FileNotFoundException e) {
      LOGGER.error("Error loading the config file", e);
    }
  }

  public static void saveAppConfig() {
    try {
      PropertiesConfiguration temp = new PropertiesConfiguration();
      temp.load(new FileReader(externalAppConfigPath.toFile()));
      if (externalAppConfig != null) {
        HashSet<String> keys = new HashSet<>();
        externalAppConfig.getKeys().forEachRemaining(keys::add);
        temp.getKeys().forEachRemaining(keys::add);
        keys.forEach(s -> {
          // Add new properties to the ext_config
          if (temp.containsKey(s) && !externalAppConfig.containsKey(s)) {
            externalAppConfig.addProperty(s, temp.getProperty(s));
          } else {
            // check if there's any property in the current file that is
            // different from the ones we loaded in the beginning of the
            // execution of the application.
            if (temp.containsKey(s) && startExternalAppConfig.containsKey(s)
              && !temp.getProperty(s).equals(startExternalAppConfig.getProperty(s))) {
              // Set the property to keep the changes made outside the
              // application
              externalAppConfig.setProperty(s, temp.getProperty(s));
            }
          }
        });
      }
      externalAppConfig.save(externalAppConfigPath.toFile());
    } catch (ConfigurationException | FileNotFoundException e) {
      LOGGER.error("Error saving the app config file", e);
    }
  }

  public static URL getBuildProperties() {
    return ClassLoader.getSystemResource("build.properties");
  }
}
