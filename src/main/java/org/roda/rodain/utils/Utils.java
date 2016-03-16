package org.roda.rodain.utils;

import org.apache.commons.io.IOUtils;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.utils.validation.ResourceResolver;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public class Utils {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Utils.class.getName());

  private Utils() {
  }

  public static String formatSize(long v) {
    if (v < 1024)
      return v + " B";
    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }

  public static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  public static String convertStreamToString(InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  public static Document loadXMLFromString(String xml) throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();

    return builder.parse(new InputSource(new StringReader(xml)));
  }

  public static boolean isEAD(String content) throws SAXException {
    boolean isValid = false;
    try {
      isValid = validateSchemaWithoutCatch(content, ClassLoader.getSystemResourceAsStream("templates/ead2002.xsd"));
    } catch (IOException e) {
      log.error("Can't access the schema file", e);
    }

    return isValid;
  }

  public static boolean validateSchema(String content, String schemaString) throws SAXException {
    boolean isValid = false;
    try {
      isValid = validateSchemaWithoutCatch(content, IOUtils.toInputStream(schemaString, "UTF-8"));
    } catch (IOException e) {
      log.error("Can't access the schema file", e);
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

}
