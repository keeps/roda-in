package org.roda.rodain.core.template;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-03-2016.
 */
public class TemplateUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateUtils.class.getName());

  public static TreeSet<TemplateFieldValue> getTemplateFields(DescriptiveMetadata dom) {
    TreeSet<TemplateFieldValue> fields = new TreeSet<TemplateFieldValue>();
    String content = dom.getContentDecoded();
    String templateContent = dom.getTemplateType() != null
      ? ConfigurationManager.getTemplateContent(dom.getTemplateType()) : null;
    if (templateContent != null) {
      fields = processTemplate(templateContent);
      if (fields != null) {
        for (TemplateFieldValue field : fields) {
          String xpathRaw = (String) field.get("xpath");
          if (xpathRaw != null && xpathRaw.length() > 0) {
            String[] xpaths = xpathRaw.split("##%##");
            String value;
            List<String> allValues = new ArrayList<>();
            for (String xpath : xpaths) {
              allValues.addAll(applyXpath(content, xpath));
            }
            // if any of the values is different, concatenate all values in a
            // string, otherwise return the value
            boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.get(0).trim()));
            if (allEqual && !allValues.isEmpty()) {
              value = allValues.get(0);
            } else {
              value = String.join(" / ", allValues);
            }
            field.set("value", value.trim());
          }
        }
      }
    }
    return fields;
  }

  public static TreeSet<TemplateFieldValue> processTemplate(String templateContent) {
    if (templateContent == null)
      return null;

    TreeSet<TemplateFieldValue> values = new TreeSet<>();
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
            values.add(new TemplateFieldValue(tagID, new HashMap<>(newHash)));
            addedTags.add(tagID);
          }
        }
        return options.fn();
      });
      handlebars.registerHelper("ifCond", (context, options) -> {
        // the first parameter of ifCond is placed in the context field by the
        // parser
        String condition = (context == null) ? Constants.MISC_OR_OP : context.toString();
        List<Object> vals = Arrays.asList(options.params);
        boolean display;
        if (condition.equals(Constants.MISC_OR_OP)) {
          display = false;
          for (Object value : vals) {
            if (value != null) {
              display = true;
              break;
            }
          }
        } else if (condition.equals(Constants.MISC_AND_OP)) {
          display = true;
          for (Object value : vals) {
            if (value == null) {
              display = false;
              break;
            }
          }
        } else {
          display = false;
        }
        return display ? options.fn() : options.inverse();
      });
      // Prevent errors from unknown helpers
      handlebars.registerHelperMissing((o, options) -> options.fn());

      tmpl = handlebars.compileInline(templateContent);
      tmpl.apply(new HashMap<>());
    } catch (IOException e) {
      LOGGER.error("Error getting the MetadataValue list from the template", e);
    }
    return values;
  }

  public static String getXMLFromTemplate(String templateContent) {
    TreeSet<TemplateFieldValue> values = new TreeSet<>();
    Set<String> addedTags = new HashSet<>();
    Handlebars handlebars = new Handlebars();

    Template tmpl;
    String xml = null;
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
            values.add(new TemplateFieldValue(tagID, new HashMap<>(newHash)));
            addedTags.add(tagID);
          }
        }
        return options.fn();
      });
      handlebars.registerHelper("ifCond", (context, options) -> {
        // the first parameter of ifCond is placed in the context field by the
        // parser
        String condition = (context == null) ? Constants.MISC_OR_OP : context.toString();
        List<Object> vals = Arrays.asList(options.params);
        boolean display;
        if (condition.equals(Constants.MISC_OR_OP)) {
          display = false;
          for (Object value : vals) {
            if (value != null) {
              display = true;
              break;
            }
          }
        } else if (condition.equals(Constants.MISC_AND_OP)) {
          display = true;
          for (Object value : vals) {
            if (value == null) {
              display = false;
              break;
            }
          }
        } else {
          display = false;
        }
        return display ? options.fn() : options.inverse();
      });
      // Prevent errors from unknown helpers
      handlebars.registerHelperMissing((o, options) -> options.fn());

      tmpl = handlebars.compileInline(templateContent);
      xml = tmpl.apply(new HashMap<>());
    } catch (IOException e) {
      LOGGER.error("Error getting the MetadataValue list from the template", e);
    }
    return xml;
  }

  public static List<String> applyXpath(String xml, String xpathString) {
    List<String> result = new ArrayList<>();
    try {
      Processor proc = new Processor(false);
      XPathCompiler xpath = proc.newXPathCompiler();
      DocumentBuilder builder = proc.newDocumentBuilder();

      // Load the XML document.
      StringReader reader = new StringReader(xml);
      XdmNode doc = builder.build(new StreamSource(reader));

      // Compile the xpath
      XPathSelector selector = xpath.compile(xpathString).load();
      selector.setContextItem(doc);

      // Evaluate the expression.
      XdmValue nodes = selector.evaluate();

      for (XdmItem item : nodes) {
        result.add(item.toString());
      }

    } catch (Exception e) {
      LOGGER.error("Error applying XPath", e);
    }
    return result;
  }

}
