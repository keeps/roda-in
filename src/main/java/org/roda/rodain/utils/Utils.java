package org.roda.rodain.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.utils.validation.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public class Utils {
  private static final String LATEST_VERSION_API = "https://api.github.com/repos/keeps/roda-in/releases/latest";
  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());
  private static String UTF8_BOM = "\uFEFF";

  private Utils() {
  }

  /**
   * Formats a number to a readable size format (B, KB, MB, GB, etc)
   *
   * @param v
   *          The value to be formatted
   * @return The formatted String
   */
  public static String formatSize(long v) {
    if (v < 1024)
      return v + " B";
    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }

  /**
   * Reads a file and returns its content.
   *
   * @param path
   *          The path of the file to be read.
   * @param encoding
   *          The encoding to be used when reading the file.
   * @return A String with the content of the file
   * @throws IOException
   */
  public static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    String temp = new String(encoded, encoding);
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
   * Produces a String from the specified InputStream.
   * 
   * @param is
   *          The InputStream to be used.
   * @return The String produced using the InputStream.
   */
  public static String convertStreamToString(InputStream is) {
    Scanner s = new Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
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
  public static boolean validateSchema(String content, String schemaString) throws SAXException {
    boolean isValid = false;
    try {
      isValid = validateSchemaWithoutCatch(content, IOUtils.toInputStream(schemaString, "UTF-8"));
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
    StreamSource streamSource = new StreamSource(schemaStream, AppProperties.getRodainPath().toString());
    Schema schema = factory.newSchema(streamSource);
    Validator validator = schema.newValidator();

    // create a source from a string
    Source source = new StreamSource(new StringReader(content));

    // check input
    validator.validate(source);
    return true;
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
  public static void indentXML(Reader input, Writer output) throws TransformerException, SAXParseException {
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
  public static String indentXML(String xml) {
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

  /**
   * @return The current version of the application.
   * @throws ConfigurationException
   */
  public static String getCurrentVersion() throws ConfigurationException {
    PropertiesConfiguration pc = new PropertiesConfiguration(AppProperties.getBuildProperties());
    return pc.getString("git.build.version");
  }

  /**
   * @return The latest application version available in the cloud. Will thrown
   *         an exception if no Internet connection is available.
   * @throws URISyntaxException
   * @throws IOException
   */
  public static String getLatestVersion() throws URISyntaxException, IOException {
    URI uri = new URI(LATEST_VERSION_API);
    JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
    JSONObject versionJSON = new JSONObject(tokener);

    return versionJSON.getString("tag_name");
  }

  /**
   * @return The current version's build time.
   * @throws ConfigurationException
   */
  public static Date getCurrentVersionBuildDate() throws ConfigurationException {
    PropertiesConfiguration pc = new PropertiesConfiguration(AppProperties.getBuildProperties());
    String dateRaw = pc.getString("git.build.time");
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy '@' HH:mm:ss z");
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
  public static Date getLatestVersionBuildDate() throws URISyntaxException, IOException {
    URI uri = new URI(LATEST_VERSION_API);
    JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
    JSONObject versionJSON = new JSONObject(tokener);
    String dateRaw = versionJSON.getString("published_at");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    try {
      return sdf.parse(dateRaw);
    } catch (ParseException e) {
      LOGGER.warn("Cannot parse the date \"{}\"", dateRaw, e);
    }
    return null;
  }

  public static DescObjMetadata updateTemplate(DescObjMetadata dm) {
    String metaTypesRaw = AppProperties.getConfig("metadata.types");
    if (metaTypesRaw != null) {
      String[] metaTypes = metaTypesRaw.split(",");
      for (String metaType : metaTypes) {
        String typeKey = "metadata." + metaType + ".type";
        String versionKey = "metadata." + metaType + ".version";
        String type = AppProperties.getConfig(typeKey);
        String version = AppProperties.getConfig(versionKey);
        if (dm.getMetadataType() != null && dm.getMetadataType().equalsIgnoreCase(type)) {
          if (dm.getMetadataVersion() != null && dm.getMetadataVersion().equalsIgnoreCase(version)) {
            dm.setTemplateType(metaType);
            dm.setCreatorOption(MetadataOptions.TEMPLATE);
          }
        }
      }
    }
    return dm;
  }

  public static boolean containsAtLeastOneNotIgnoredFile(Path path) {
    // LOGGER.error("containsAtLeastOneNotIgnoredFile("+path.toString()+")");
    boolean res = true;
    try {
      if (path.toFile().listFiles().length == 0) {
        res = false;
      } else {
        
         ValidFilesCounter visitor = new ValidFilesCounter();
          Files.walkFileTree(path, visitor); int validFiles =
          visitor.getValidFiles(); if (validFiles == 0) { res = false; }
      }
    } catch (Exception e) {
      LOGGER.debug("Error while checking if directory contains at least on valid file: " + e.getMessage(), e);
    }
    return res;
  }

  public static String createID() {
    String prefix = AppProperties.getConfig("idPrefix");
    if (prefix == null) {
      prefix = "uuid-";
    }
    return prefix + UUID.randomUUID().toString();
  }

}
