package org.roda.rodain.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.roda.rodain.core.schema.ClassificationSchema;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.ui.schema.ui.SchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class that holds application business logic
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @since 2017-03-10
 */
public class Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class.getName());
  private static final String SYSTEM_OS = System.getProperty("os.name").toLowerCase();

  public Controller() {
    // do nothing
  }

  /**
   * Method that checks for updates (that is, if this is the latest app version
   * available).
   * 
   * @param checkForEnvVariable
   * 
   * @return empty if app is up to date or optional<string> with the message
   *         explaining the version differences
   */
  public static Optional<String> checkForUpdates(boolean checkForEnvVariable) {
    Optional<String> res = Optional.empty();

    String rodaInEnv = System.getenv(Constants.RODAIN_ENV_VARIABLE);
    if (!(checkForEnvVariable && Constants.RODAIN_ENV_TESTING.equals(rodaInEnv))) {
      try {
        Date currentVersion = getCurrentVersionBuildDate();
        Date latestVersion = getLatestVersionBuildDate();

        if (currentVersion != null && latestVersion != null) {
          if (currentVersion.compareTo(latestVersion) < 0) {
            String content = String.format(I18n.t(Constants.I18N_NEW_VERSION_CONTENT), getCurrentVersion(),
              getLatestVersion());
            res = Optional.ofNullable(content);
          }
        }
      } catch (ConfigurationException e) {
        LOGGER.error("Could not retrieve application version from build.properties", e);
      } catch (URISyntaxException e) {
        LOGGER.warn("The URI is malformed", e);
      } catch (IOException e) {
        LOGGER.warn("Error accessing the GitHub API", e);
      }
    }
    return res;
  }

  public static void exportClassificationScheme(Set<SchemaNode> nodes, String outputFile) {
    List<Sip> dobjs = new ArrayList<>();
    for (SchemaNode sn : nodes) {
      dobjs.add(sn.getDob());
    }
    ClassificationSchema cs = new ClassificationSchema();
    cs.setDos(dobjs);
    ControllerUtils.exportClassificationScheme(cs, outputFile);
    ConfigurationManager.setAppConfig(Constants.CONF_K_APP_LAST_CLASS_SCHEME, outputFile, true);
  }

  public static ClassificationSchema loadClassificationSchemaFile(String filePath) throws IOException {
    ConfigurationManager.setAppConfig(Constants.CONF_K_APP_LAST_CLASS_SCHEME, filePath, true);
    try (InputStream input = new FileInputStream(filePath)) {

      // create ObjectMapper instance
      ObjectMapper objectMapper = new ObjectMapper();

      // convert json string to object
      return objectMapper.readValue(input, ClassificationSchema.class);
    }
  }

  public static boolean validateSchema(Path fileToValidate, String schemaString) throws SAXException, IOException {
    String fileContent = ControllerUtils.readFile(fileToValidate);
    return validateSchema(fileContent, schemaString);
  }

  public static boolean validateSchema(String content, String schemaString) throws SAXException {
    return ControllerUtils.validateSchema(content, schemaString);
  }

  public static String loadMetadataFile(Path path) throws IOException {
    return ControllerUtils.readFile(path);
  }

  public static String getCurrentVersion() throws ConfigurationException {
    return ControllerUtils.getCurrentVersion();
  }

  private static String getLatestVersion() throws URISyntaxException, IOException {
    return ControllerUtils.getLatestVersion();
  }

  private static Date getCurrentVersionBuildDate() throws ConfigurationException {
    return ControllerUtils.getCurrentVersionBuildDate();
  }

  private static Date getLatestVersionBuildDate() throws URISyntaxException, IOException {
    return ControllerUtils.getLatestVersionBuildDate();
  }

  public static String createID() {
    return ControllerUtils.createID();
  }

  public static String indentXML(String content) {
    return ControllerUtils.indentXML(content);
  }

  public static DescriptiveMetadata updateTemplate(DescriptiveMetadata dom) {
    return ControllerUtils.updateTemplate(dom);
  }

  /**
   * Formats a number to a readable size format (B, KB, MB, GB, etc)
   *
   * @param v
   *          The value to be formatted
   * @return The formatted String
   */
  public static String formatSize(long size) {
    return ControllerUtils.formatSize(size);
  }

  /**
   * <p>
   * Encodes ID chars that are dangerous to file systems or jar files entries.
   * We will URLEncode, but just some chars that we think are dangerous.
   * </p>
   * 
   * <p>
   * We will start by replacing all "%" => "%25", and then all the others
   * (forward slashes, etc.).
   * </p>
   * 
   * @param unsafeId
   *          non-null string to be encoded for safe use in file systems or jar
   *          files
   * 
   */
  public static String encodeId(String unsafeId) {
    return unsafeId.replaceAll("%", "%25").replaceAll("/", "%2F").replaceAll("\\\\", "%5C");
  }

  public static boolean systemIsWindows() {
    return SYSTEM_OS.contains("win");
  }

  public static boolean systemIsMac() {
    return SYSTEM_OS.contains("mac");
  }

  public static boolean systemIsUnix() {
    return SYSTEM_OS.contains("nix") || SYSTEM_OS.contains("nux") || SYSTEM_OS.contains("aix");
  }
}
