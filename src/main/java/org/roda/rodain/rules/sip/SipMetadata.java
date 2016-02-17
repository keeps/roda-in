package org.roda.rodain.rules.sip;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jcabi.xml.XMLDocument;

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
  private String template, content;
  private Path path;
  private Set<MetadataValue> values;

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
    this.values = new HashSet<>();
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
          template = AppProperties.getMetadataFile(templateType);
          content = template;
          loaded = true;
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
    Map<String, List<String>> formRules = createEADForm();

    try {
      Document document = loadXMLFromString(getMetadataContent());
      formRules.forEach((id, xpathList) -> {
        MetadataValue currentMeta = getMetadataValue(id);
        // if there was a MetadataValue already we don't need to add the xpaths again
        boolean wasNull = currentMeta == null;
        for (String xpath : xpathList) {
          XPath xPath = XPathFactory.newInstance().newXPath();
          NodeList nodes = null;
          try {
            nodes = (NodeList) xPath.evaluate(xpath, document.getDocumentElement(), XPathConstants.NODESET);
          } catch (XPathExpressionException e) {
            e.printStackTrace();
          }
          if (nodes != null && nodes.getLength() > 0) {
            if (currentMeta == null) {
              String title = AppProperties.getLocalizedString("metadataValue." + id);
              currentMeta = new MetadataValue(id, title, nodes.item(0).getTextContent());
            }
            if (wasNull) {
              currentMeta.addXpathDestination(xpath);
            }
            currentMeta.setValue(nodes.item(0).getTextContent());
          }
        }
        if (wasNull)
          values.add(currentMeta);
      });
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private MetadataValue getMetadataValue(String title) {
    MetadataValue result = null;
    for (MetadataValue mv : values) {
      if (mv.getId().equals(title)) {
        result = mv;
        break;
      }
    }
    return result;
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

  /**
   * @return The set of MetadataValue objects. Used to create the form.
   */
  public Set<MetadataValue> getValues() {
    loadValues();
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

  /**
   * Sets the metadata content of the SIP.
   * This will not flag the SIP as edited! Use update() instead.
   *
   * @param meta The new metadata content.
   * @see #update(String)
   */
  public void setContent(String meta) {
    content = meta;
  }

  /**
   * Applies the data from the MetadataValues to the XML string.
   */
  public void applyMetadataValues() {
    try {
      Document document = loadXMLFromString(getMetadataContent());
      for (MetadataValue mv : values) {
        for (String xPath : mv.getXpathDestinations()) {
          XPath xPathObj = XPathFactory.newInstance().newXPath();
          NodeList nodes = (NodeList) xPathObj.evaluate(xPath, document.getDocumentElement(), XPathConstants.NODESET);
          for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            node.setTextContent(mv.getValue());
          }
        }
      }
      String result = new XMLDocument(document).toString();
      update(result);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Map<String, List<String>> createEADForm() {
    Map<String, List<String>> paths = new HashMap<>();
    List<String> titleXpaths = new ArrayList<>();
    titleXpaths.add("//*[local-name()='titleproper']");
    titleXpaths.add("//*[local-name()='unittitle']");
    paths.put("title", titleXpaths);

    List<String> dateXpaths = new ArrayList<>();
    dateXpaths.add("//*[local-name()='date']/@normal");
    dateXpaths.add("//*[local-name()='date']");
    dateXpaths.add("//*[local-name()='unitdate']/@normal");
    dateXpaths.add("//*[local-name()='unitdate']");
    paths.put("date", dateXpaths);

    List<String> repCodeXpaths = new ArrayList<>();
    repCodeXpaths.add("//*[local-name()='unitid']/@repositorycode");
    paths.put("repcode", repCodeXpaths);

    List<String> idXpaths = new ArrayList<>();
    idXpaths.add("//*[local-name()='unitid']");
    paths.put("id", idXpaths);
    return paths;
  }
}
