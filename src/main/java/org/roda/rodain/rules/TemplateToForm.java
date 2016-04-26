package org.roda.rodain.rules;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.roda.rodain.rules.sip.MetadataValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-03-2016.
 */
public class TemplateToForm {
  public static Set<MetadataValue> createSet(String content) {
    Set<MetadataValue> result = new TreeSet<>();
    Handlebars handlebars = new Handlebars();

    Template template;
    try {
      handlebars.registerHelperMissing((o, options) -> {
        // if (!options.hash.isEmpty())
        result.add(new MetadataValue(options.helperName, options.hash));
        return options.helperName;
      });
      template = handlebars.compileInline(content);
      template.apply(new HashMap<>());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}
