package org.roda.rodain.rules;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.sip.MetadataValue;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcabi.xml.XMLDocument;

/**
 * Created by adrapereira on 18-02-2016.
 */
public class XMLToMetadataValue {
  private static final Logger log = LoggerFactory.getLogger(XMLToMetadataValue.class.getName());

  public static Map<String, MetadataValue> createEADMetadataValues(String content)
    throws InvalidEADException {
    if (!Utils.isEAD(content)) {
      throw new InvalidEADException("Invalid EAD");
    }
    Map<String, MetadataValue> metadataValues = createEADForm();

    try {
      Document document = Utils.loadXMLFromString(content);
      metadataValues.forEach((id, metadataValue) -> {
        // if there was a MetadataValue already we don't need to add the xpaths
        // again
        List<String> xpaths = metadataValue.getXpathDestinations();
        for (String xpath : xpaths) {
          XPath xPath = XPathFactory.newInstance().newXPath();
          NodeList nodes = null;
          try {
            nodes = (NodeList) xPath.evaluate(xpath, document.getDocumentElement(), XPathConstants.NODESET);
          } catch (XPathExpressionException e) {
            log.error("Error parsing the XPath expression", e);
          }
          if (nodes != null && nodes.getLength() > 0) {
            metadataValue.setValue(nodes.item(0).getTextContent());
          }
        }
      });
    } catch (SAXException e) {
      log.error("Error parsing the XML", e);
    } catch (ParserConfigurationException e) {
      log.error("Error in the configuration of the parser", e);
    } catch (IOException e) {
      log.error("Error reading the XML", e);
    }
    return metadataValues;
  }

  /**
   * Applies the data from the MetadataValues to the XML string.
   */
  public static String applyMetadataValues(String content, Map<String, MetadataValue> values) {
    String result = null;
    try {
      Document document = Utils.loadXMLFromString(content);
      document.setXmlStandalone(true);
      for (MetadataValue mv : values.values()) {
        for (String xPath : mv.getXpathDestinations()) {
          XPath xPathObj = XPathFactory.newInstance().newXPath();
          NodeList nodes = (NodeList) xPathObj.evaluate(xPath, document.getDocumentElement(), XPathConstants.NODESET);
          for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            node.setTextContent(mv.getValue());
          }
        }
      }
      result = new XMLDocument(document).toString();
    } catch (SAXException e) {
      log.error("Error parsing the XML", e);
    } catch (ParserConfigurationException e) {
      log.error("Error in the configuration of the parser", e);
    } catch (IOException e) {
      log.error("Error reading the XML", e);
    } catch (XPathExpressionException e) {
      log.error("Error parsing the XPath expression", e);
    }
    return result;
  }

  private static Map<String, MetadataValue> createEADForm() {
    Map<String, MetadataValue> result = new LinkedHashMap<>();
    MetadataValue title = new MetadataValue("title", AppProperties.getLocalizedString("metadataValue.title"), null,
      "text");
    title.addXpathDestination("//*[local-name()='titleproper']");
    title.addXpathDestination("//*[local-name()='unittitle']");

    MetadataValue date = new MetadataValue("date", AppProperties.getLocalizedString("metadataValue.date"), null,
      "text");
    date.addXpathDestination("//*[local-name()='unitdate']/@normal");
    date.addXpathDestination("//*[local-name()='unitdate']");

    MetadataValue id = new MetadataValue("id", AppProperties.getLocalizedString("metadataValue.id"), null, "text");
    id.addXpathDestination("//*[local-name()='unitid']");

    MetadataValue level = new MetadataValue("level", AppProperties.getLocalizedString("metadataValue.level"), null,
      "combo");
    level.addXpathDestination("//*[local-name()='archdesc']/@level");
    String itemTypesRaw = AppProperties.getDescLevels("levels_ordered");
    String[] itemTypesArray = itemTypesRaw.split(",");
    for (String item : itemTypesArray) {
      UIPair pair = new UIPair(item, AppProperties.getDescLevels("label.en." + item));
      level.addFieldOption(pair);
    }

    MetadataValue description = new MetadataValue("description",
      AppProperties.getLocalizedString("metadataValue.description"), null, "text");
    description.addXpathDestination("//*[local-name()='scopecontent']/*[local-name()='p']");

    MetadataValue rights = new MetadataValue("rights", AppProperties.getLocalizedString("metadataValue.rights"), null,
      "text");
    rights.addXpathDestination("//*[local-name()='userestrict']/*[local-name()='p']");

    MetadataValue language = new MetadataValue("language", AppProperties.getLocalizedString("metadataValue.language"),
      null, "text");
    language.addXpathDestination("//*[local-name()='langmaterial']");

    MetadataValue producer = new MetadataValue("producer", AppProperties.getLocalizedString("metadataValue.producer"),
      null, "text");
    producer.addXpathDestination("//*[local-name()='origination'][@label=\"producer\"]");

    MetadataValue creator = new MetadataValue("creator", AppProperties.getLocalizedString("metadataValue.creator"),
      null, "text");
    creator.addXpathDestination("//*[local-name()='origination'][@label=\"creator\"]");

    // add values to result
    result.put("level", level);
    result.put("title", title);
    result.put("date", date);
    result.put("id", id);
    result.put("producer", producer);
    result.put("creator", creator);
    result.put("language", language);
    result.put("description", description);
    result.put("rights", rights);

    return result;
  }
}
