package org.roda.rodain.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.roda.rodain.core.schema.ClassificationSchema;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.utils.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public final class ControllerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtils.class.getName());
  private static final String UTF8_BOM = "\uFEFF";

  private ControllerUtils() {
    // do nothing
  }

  /**
   * Reads a file and returns its content.
   *
   * @param path
   *          The path of the file to be read.
   * @return A String with the content of the file
   * @throws IOException
   */
  protected static String readFile(Path path) throws IOException {
    byte[] encoded = Files.readAllBytes(path);
    String temp = new String(encoded, Charset.forName(Constants.RODAIN_DEFAULT_ENCODING));
    // Consume BOM if it exists
    temp = removeUTF8BOM(temp);
    return temp;
  }

  private static String removeUTF8BOM(String s) {
    if (s.startsWith(UTF8_BOM)) {
      s = s.substring(1);
    }
    return s;
  }

  /**
   * Produces a String from the specified InputStream. NOTE: the stream is
   * closed in the end.
   * 
   * @param is
   *          The InputStream to be used.
   * @return The String produced using the InputStream.
   */
  protected static String convertStreamToString(InputStream is) {
    Scanner s = new Scanner(is).useDelimiter("\\A");
    String res = s.hasNext() ? s.next() : "";
    IOUtils.closeQuietly(s);
    return res;
  }

  /**
   * Validates a XML against a schema.
   * 
   * @param content
   *          The String content of the XML to be validated.
   * @param schemaString
   *          The String content of the schema used to validate.
   * @return True if the content can be validated using the schema, false
   *         otherwise.
   * @throws SAXException
   */
  protected static boolean validateSchema(String content, InputStream schemaInputStream) throws SAXException {
    boolean isValid = false;
    try {
      isValid = validateSchemaWithoutCatch(content, schemaInputStream);
    } catch (IOException e) {
      LOGGER.error("Can't access the schema file", e);
    }

    return isValid;
  }

  private static boolean validateSchemaWithoutCatch(String content, InputStream schemaStream)
    throws IOException, SAXException {
    // build the schema
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    factory.setResourceResolver(new ResourceResolver());
    StreamSource streamSource = new StreamSource(schemaStream);
    Schema schema = factory.newSchema(streamSource);
    Validator validator = schema.newValidator();

    // create a source from a string
    Source source = new StreamSource(new StringReader(content));

    // check input
    validator.validate(source);
    return true;
  }

  /**
   * @return The current version of the application.
   * @throws ConfigurationException
   */
  protected static String getCurrentVersion() throws ConfigurationException {
    PropertiesConfiguration pc = new PropertiesConfiguration(ConfigurationManager.getBuildProperties());
    return pc.getString("git.build.version");
  }

  /**
   * @return The latest application version available in the cloud. Will thrown
   *         an exception if no Internet connection is available.
   * @throws URISyntaxException
   * @throws IOException
   */
  protected static String getLatestVersion() throws URISyntaxException, IOException {
    URI uri = new URI(Constants.RODAIN_GITHUB_LATEST_VERSION_API_LINK);
    // FIXME 20170307 hsilva: possible leak as stream is not closed
    JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
    JSONObject versionJSON = new JSONObject(tokener);

    return versionJSON.getString("tag_name");
  }

  /**
   * @return The current version's build time.
   * @throws ConfigurationException
   */
  protected static Date getCurrentVersionBuildDate() throws ConfigurationException {
    PropertiesConfiguration pc = new PropertiesConfiguration(ConfigurationManager.getBuildProperties());
    String dateRaw = pc.getString("git.commit.time");
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_2);
    try {
      return sdf.parse(dateRaw);
    } catch (ParseException e) {
      LOGGER.warn("Cannot parse the date \"{}\"", dateRaw, e);
    }
    return null;
  }

  /**
   * @return The latest application version build time available in the cloud.
   *         Will thrown an exception if no Internet connection is available.
   * @throws URISyntaxException
   * @throws IOException
   */
  protected static Date getLatestVersionBuildDate() throws URISyntaxException, IOException {
    URI uri = new URI(Constants.RODAIN_GITHUB_LATEST_VERSION_API_LINK);
    JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
    JSONObject versionJSON = new JSONObject(tokener);
    String dateRaw = versionJSON.getString("created_at");

    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_3);
    try {
      return sdf.parse(dateRaw);
    } catch (ParseException e) {
      LOGGER.warn("Cannot parse the date \"{}\"", dateRaw, e);
    }
    return null;
  }

  protected static String createID() {
    String prefix = ConfigurationManager.getConfig(Constants.CONF_K_ID_PREFIX);
    if (prefix == null) {
      prefix = Constants.MISC_DEFAULT_ID_PREFIX;
    }
    return prefix + UUID.randomUUID().toString();
  }

  /**
   * Indents an XML document.
   * 
   * @param input
   *          The input XML document Reader.
   * @param output
   *          The Writer for the output of the indented XML document.
   * @throws TransformerException
   * @throws SAXParseException
   */
  protected static void indentXML(Reader input, Writer output) throws TransformerException, SAXParseException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    StreamSource source = new StreamSource(input);
    StreamResult result = new StreamResult(output);
    transformer.transform(source, result);
  }

  /**
   * Indents an XML document.
   * 
   * @param xml
   *          The XML document string.
   * @return A string with the XML document indented.
   */
  protected static String indentXML(String xml) {
    Reader input = new StringReader(xml);
    Writer output = new StringWriter();
    try {
      indentXML(input, output);
    } catch (TransformerException | SAXParseException e) {
      LOGGER.warn("Could not indent XML", e);
      return xml;
    }
    return output.toString();
  }

  protected static String formatSize(long v) {
    if (v < 1024)
      return v + " B";
    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }

  protected static DescriptiveMetadata updateTemplate(DescriptiveMetadata dm) {
    String metaTypesRaw = ConfigurationManager.getConfig(Constants.CONF_K_METADATA_TYPES);
    if (metaTypesRaw != null) {
      String[] metaTypes = metaTypesRaw.split(Constants.MISC_COMMA);
      for (String metaType : metaTypes) {
        String type = ConfigurationManager.getMetadataConfig(metaType + Constants.CONF_K_SUFFIX_TYPE);
        String version = ConfigurationManager.getMetadataConfig(metaType + Constants.CONF_K_SUFFIX_VERSION);
        if (dm.getMetadataType() != null && dm.getMetadataType().equalsIgnoreCase(type)) {
          if (dm.getMetadataVersion() != null && dm.getMetadataVersion().equalsIgnoreCase(version)) {
            dm.setTemplateType(metaType);
            break;
            // dm.setCreatorOption(MetadataOptions.TEMPLATE);
          }
        }
      }
    }
    return dm;
  }

  protected static void exportClassificationScheme(ClassificationSchema classSchema, String outputFile) {
    try {
      OutputStream outputStream = new FileOutputStream(outputFile);
      // create ObjectMapper instance
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      // convert object to json string
      objectMapper.writeValue(outputStream, classSchema);
    } catch (IOException e) {
      LOGGER.error("Error exporting classification scheme", e);
    }
  }

  public static void deleteQuietly(Path path) {
    FileUtils.deleteQuietly(path.toFile());
  }

}
