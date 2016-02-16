package org.roda.rodain.rules.sip;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 23/11/2015.
 */
public class SipMetadata {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipMetadata.class.getName());
  private MetadataTypes type;
  private String version;
  private String templateType;
  private boolean loaded = false, modified = false;
  private String content;
  private Path path;
  private List<MetadataValue> values;

  /**
   * Creates a new SipMetadata object.
   *
   * @param type         The metadata type
   * @param path         The path to the metadata file
   * @param templateType The type of the metadata template
   */
  public SipMetadata(MetadataTypes type, Path path, String templateType, String version) {
    this.type = type;
    this.path = path;
    this.templateType = templateType;
    this.version = version;
    this.values = new ArrayList<>();
  }

  /**
   * @return True if the metadata has been modified, false otherwise.
   */
  public boolean isModified() {
    return modified;
  }

  private void loadMetadata() {
    try {
      if (type == MetadataTypes.TEMPLATE) {
        if (templateType != null) {
          content = AppProperties.getMetadataFile(templateType);
          loaded = true;
          loadValues();
        }
      } else {
        if (path != null) {
          content = Utils.readFile(path.toString(), Charset.defaultCharset());
          loaded = true;
        }
      }
    } catch (IOException e) {
      log.error("Error reading metadata file", e);
    }
  }

  public Document loadXMLFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();

    return builder.parse(new InputSource(new StringReader(xml)));
  }

  private void loadValues() {
    Map<String, List<String>> paths = new HashMap<>();
    List<String> titleXpaths = new ArrayList<>();
    titleXpaths.add("//*[local-name()='titleproper']");
    titleXpaths.add("//*[local-name()='unittitle']");
    paths.put("Title", titleXpaths);

    List<String> dateXpaths = new ArrayList<>();
    dateXpaths.add("//*[local-name()='date']/@normal");
    dateXpaths.add("//*[local-name()='date']");
    dateXpaths.add("//*[local-name()='unitdate']/@normal");
    dateXpaths.add("//*[local-name()='unitdate']");
    paths.put("Date", dateXpaths);

    List<String> repCodeXpaths = new ArrayList<>();
    repCodeXpaths.add("//*[local-name()='unitid']/@repositorycode");
    paths.put("Repository code", repCodeXpaths);

    List<String> idXpaths = new ArrayList<>();
    idXpaths.add("//*[local-name()='unitid']");
    paths.put("ID", idXpaths);

    try {
      Document document = loadXMLFromString(content);
      paths.forEach((title, xpathList) -> {
        //System.out.println(title + " - " + xpath);
        MetadataValue currentMeta = null;
        for (String xpath : xpathList) {
          XPath xPath = XPathFactory.newInstance().newXPath();
          NodeList nodes = null;
          try {
            nodes = (NodeList) xPath.evaluate(xpath, document.getDocumentElement(), XPathConstants.NODESET);
          } catch (XPathExpressionException e) {
            e.printStackTrace();
          }
          if (nodes.getLength() > 0) {
            if (currentMeta == null) {
              currentMeta = new MetadataValue(title, nodes.item(0).getTextContent());
            }
            currentMeta.addXpathDestination(xpath);
          }
        }
        values.add(currentMeta);
      });
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the metadata content of a SIP, loading it from the disk if that action
   * hasn't been previously done.
   *
   * @return The metadata content of the SIP.
   */
  public String getMetadataContent() {
    if (!loaded) {
      loadMetadata();
    }
    return content;
  }

  public List<MetadataValue> getValues() {
    return values;
  }

  /**
   * @return The type of the metadata.
   */
  public String getTemplateType() {
    return templateType;
  }

  /**
   * @return The version of the metadata.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Updates the metadata content of the SIP.
   *
   * @param meta The new metadata content.
   */
  public void update(String meta) {
    modified = true;
    content = meta;
  }
}
