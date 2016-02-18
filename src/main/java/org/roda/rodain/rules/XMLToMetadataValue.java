package org.roda.rodain.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.sip.MetadataValue;
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

  public static Map<String, MetadataValue> createEADMetadataValues(String content, Map<String, MetadataValue> values) {
    Map<String, List<String>> formRules = createEADForm();

    try {
      Document document = Utils.loadXMLFromString(content);
      formRules.forEach((id, xpathList) -> {
        MetadataValue currentMeta = values.get(id);
        // if there was a MetadataValue already we don't need to add the xpaths
        // again
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
          values.put(id, currentMeta);
      });
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return values;
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

  private static Map<String, List<String>> createEADForm() {
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
