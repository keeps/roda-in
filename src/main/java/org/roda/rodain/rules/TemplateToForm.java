package org.roda.rodain.rules;

import com.samskivert.mustache.DefaultCollector;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.sip.MetadataValue;

import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-03-2016.
 */
public class TemplateToForm {
  private static List<String> list(String content) {
    List<String> result = new ArrayList<>();
    Template tmpl = Mustache.compiler().defaultValue("").withCollector(new DefaultCollector() {
      @Override
      public Mustache.VariableFetcher createFetcher(Object o, String s) {
        result.add(s);
        return super.createFetcher(o, s);
      }
    }).compile(content);
    tmpl.execute(new HashMap<>());
    return result;
  }

  public static Map<String, MetadataValue> createMap(String content) {
    Map<String, MetadataValue> result = new TreeMap<>();
    List<String> variables = list(content);
    for (String var : variables) {
      String title = getTitle(var);
      if (title == null)
        title = var;
      result.put(var, new MetadataValue(var, title, null));
    }
    return result;
  }

  private static String getTitle(String var) {
    String result = null;
    try {
      result = I18n.t(var);
    } catch (MissingResourceException e) {
      // we will use the name of the variable if there's no available title
      // no need to log the exception or rethrow it
    }
    return result;
  }
}
