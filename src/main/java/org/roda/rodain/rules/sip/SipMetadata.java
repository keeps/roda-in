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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 23/11/2015.
 */
public class SipMetadata {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipMetadata.class.getName());
  private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder documentBuilder;
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

    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      log.debug("Error creating XML document builder", e);
    }
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

  private void loadValues() {
    Map<String, String> paths = new HashMap<>();
    paths.put("Title", "//ead:titleproper|//ead:unittitle");
    paths.put("Date", "//ead:date/@normal|//ead:date|//ead:unitdate/@normal|//ead:unitdate");
    paths.put("Repository code", "//ead:unitid/@repositorycode");

    InputSource is = new InputSource(new StringReader(content));
    System.out.println();
    try {
      Document document = documentBuilder.parse(is);
      for (String title : paths.keySet()) {
        String xpath = paths.get(title);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xPath.evaluate(xpath, document.getDocumentElement(), XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
          Element elem = (Element) nodes.item(0);
          values.add(new MetadataValue(title, elem.getNodeValue(), xpath));
        }
      }
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (XPathExpressionException e) {
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
    return values.toString();
    // return content;
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
