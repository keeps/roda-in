package org.roda.rodain.rules;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.roda.rodain.sip.MetadataValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-03-2016.
 */
public class TemplateToForm {
  /**
   * The result type MUST be a TreeSet so that when loading an exported
   * classification scheme, the order of the fields is correct.
   * 
   * @param content
   * @return
   */
  public static TreeSet<MetadataValue> createSet(String content) {
    TreeSet<MetadataValue> result = new TreeSet<>();
    Set<String> addedTags = new HashSet<>();
    Handlebars handlebars = new Handlebars();

    Template template;
    try {
      handlebars.helpers().clear();
      handlebars.registerHelperMissing((context, options) -> {
        String tagID = options.helperName;
        if (context != null && !addedTags.contains(tagID)) {
          result.add(new MetadataValue(tagID, options.hash));
          addedTags.add(tagID);
        }
        return options.fn();
      });
      template = handlebars.compileInline(content);
      template.apply(new HashMap<>());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}
