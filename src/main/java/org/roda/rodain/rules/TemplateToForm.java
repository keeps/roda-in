package org.roda.rodain.rules;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.roda.rodain.sip.MetadataValue;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-03-2016.
 */
public class TemplateToForm {
  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateToForm.class.getName());
  /**
   * The result type MUST be a TreeSet so that when loading an exported
   * classification scheme, the order of the fields is correct.
   * 
   * @param content
   * @return
   */
  public static TreeSet<MetadataValue> transform(String content) {
    if (content == null)
      return null;

    TreeSet<MetadataValue> values = new TreeSet<>();
    Set<String> addedTags = new HashSet<>();
    Handlebars handlebars = new Handlebars();

    Template tmpl;
    try {
      handlebars.registerHelper("field", (context, options) -> {
        if (options.hash.containsKey("name")) {
          String tagID = (String) options.hash.get("name");
          if (context != null && !addedTags.contains(tagID)) {
            HashMap<String, String> newHash = new HashMap<>();
            for (String hashKey : options.hash.keySet()) {
              String hashValue = options.hash.get(hashKey).toString();
              newHash.put(hashKey, hashValue);
            }
            values.add(new MetadataValue(tagID, new HashMap<>(newHash)));
            addedTags.add(tagID);
          }
        }
        return options.fn();
      });
      // Prevent errors from unknown helpers
      handlebars.registerHelperMissing((o, options) -> options.fn());

      tmpl = handlebars.compileInline(content);
      tmpl.apply(new HashMap<>());
    } catch (IOException e) {
      LOGGER.error("Error getting the MetadataValue list from the template");
    }
    return values;
  }

  public static TreeSet<MetadataValue> transformWithEdition(String template, String content){
    TreeSet<MetadataValue> templateMV = transform(template);
    if (templateMV != null) {
      for (MetadataValue mv : templateMV) {
        // clear the auto-generated values
        // mv.set("value", null);
        String xpathRaw = (String) mv.get("xpath");
        if (xpathRaw != null && xpathRaw.length() > 0) {
          String[] xpaths = xpathRaw.split("##%##");
          String value;
          List<String> allValues = new ArrayList<>();
          for (String xpath : xpaths) {
            allValues.addAll(Utils.applyXpath(content, xpath));
          }
          // if any of the values is different, concatenate all values in a
          // string, otherwise return the value
          boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.get(0).trim()));
          if (allEqual && !allValues.isEmpty()) {
            value = allValues.get(0);
          } else {
            value = String.join(" / ", allValues);
          }
          mv.set("value", value.trim());
        }
      }
    }
    return templateMV;
  }

  /**
   * Identity check. Test if the original XML is equal to the result of applying the extracted values to the template
   * @param editedTemplate The new XML with the values extracted using XPath and applied to the template
   * @param original The original XML metadata file.
   * @return
   */
  public static boolean isSimilar(String editedTemplate, String original){
    //
    try {
      XMLUnit.setIgnoreComments(true);
      XMLUnit.setIgnoreWhitespace(true);
      XMLUnit.setIgnoreAttributeOrder(true);
      XMLUnit.setCompareUnmatched(false);

      Diff xmlDiff = new Diff(original, editedTemplate);
      xmlDiff.overrideDifferenceListener(new XMLSimilarityIgnoreElements("schemaLocation"));
      return xmlDiff.identical() || xmlDiff.similar();
    } catch (SAXException | IOException e) {
      // Do not do anything, just return false
    }
    return false;
  }

}
