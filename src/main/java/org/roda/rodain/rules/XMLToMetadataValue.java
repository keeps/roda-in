package org.roda.rodain.rules;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.sip.MetadataValue;
import org.roda.rodain.utils.UIPair;
import org.roda.rodain.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jcabi.xml.XMLDocument;

/**
 * Created by adrapereira on 18-02-2016.
 */
public class XMLToMetadataValue {

  public static Map<String, MetadataValue> createEADMetadataValues(String content)
    throws InvalidEADException {
    if (!Utils.isEAD(content)) {
      throw new InvalidEADException();
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
            e.printStackTrace();
          }
          if (nodes != null && nodes.getLength() > 0) {
            metadataValue.setValue(nodes.item(0).getTextContent());
          }
        }
      });
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
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
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private static Map<String, MetadataValue> createEADForm() {
    Map<String, MetadataValue> result = new TreeMap<>();
    MetadataValue title = new MetadataValue("title", AppProperties.getLocalizedString("metadataValue.title"), null,
      "text");
    title.addXpathDestination("//*[local-name()='titleproper']");
    title.addXpathDestination("//*[local-name()='unittitle']");
    result.put("title", title);

    MetadataValue date = new MetadataValue("date", AppProperties.getLocalizedString("metadataValue.date"), null,
      "date");
    date.addXpathDestination("//*[local-name()='date']/@normal");
    date.addXpathDestination("//*[local-name()='date']");
    date.addXpathDestination("//*[local-name()='unitdate']/@normal");
    date.addXpathDestination("//*[local-name()='unitdate']");
    result.put("date", date);

    MetadataValue repCode = new MetadataValue("repcode", AppProperties.getLocalizedString("metadataValue.repcode"),
      null, "text");
    repCode.addXpathDestination("//*[local-name()='unitid']/@repositorycode");
    result.put("repcode", repCode);

    MetadataValue id = new MetadataValue("id", AppProperties.getLocalizedString("metadataValue.id"), null, "text");
    id.addXpathDestination("//*[local-name()='unitid']");
    result.put("id", id);

    MetadataValue level = new MetadataValue("level", AppProperties.getLocalizedString("metadataValue.level"), null,
      "combo");
    level.addXpathDestination("//*[local-name()='archdesc']/@level");
    String itemTypesRaw = AppProperties.getDescLevels("levels_ordered");
    String[] itemTypesArray = itemTypesRaw.split(",");
    for (String item : itemTypesArray) {
      UIPair pair = new UIPair(item, AppProperties.getDescLevels("label.en." + item));
      level.addFieldOption(pair);
    }
    result.put("level", level);
    return result;
  }
}
