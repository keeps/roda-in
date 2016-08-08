package org.roda.rodain.utils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
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
import org.roda.rodain.utils.validation.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public class Utils {
  private static final String LATEST_VERSION_API = "https://api.github.com/repos/keeps/roda-in/releases/latest";
  public static final String BUILD_PROPERTIES_FILE = "build.properties";
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

  public static String getCurrentVersion() throws ConfigurationException {
    PropertiesConfiguration pc = new PropertiesConfiguration(BUILD_PROPERTIES_FILE);
    return pc.getString("current.version");
  }

  public static String getLatestVersion() throws URISyntaxException, IOException {
    URI uri = new URI(LATEST_VERSION_API);
    JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
    JSONObject versionJSON = new JSONObject(tokener);

    return versionJSON.getString("tag_name");
  }

}
