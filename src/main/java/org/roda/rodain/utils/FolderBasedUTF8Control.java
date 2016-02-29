package org.roda.rodain.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-01-2016.
 */
public class FolderBasedUTF8Control extends ResourceBundle.Control {
  public FolderBasedUTF8Control() {
  }

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
    throws IllegalAccessException, InstantiationException, IOException {

    if (!"java.properties".equals(format) || "".equals(locale.getDisplayName())) {
      return null;
    }

    String bundleName = toBundleName(baseName, locale) + ".properties";
    ResourceBundle bundle = null;

    InputStreamReader reader = null;
    InputStream is = null;
    try {
      is = ClassLoader.getSystemResource(bundleName).openStream();

      reader = new InputStreamReader(is, Charset.forName("UTF-8"));
      bundle = new PropertyResourceBundle(reader);
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(is);
    }
    return bundle;
  }
}
