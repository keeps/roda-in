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
}
