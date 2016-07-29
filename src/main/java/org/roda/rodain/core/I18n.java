package org.roda.rodain.core;

/**
 * Alias for the getLocalizedString method of the AppProperties class.
 * 
 * @author Andre Pereira apereira@keep.pt
 * @since 22-03-2016.
 */
public class I18n {

  public static String t(String key) {
    return AppProperties.getLocalizedString(key);
  }

  public static String t(String key, String languageTag) {
    return AppProperties.getLocalizedString(key, languageTag);
  }

  public static String help(String key){return AppProperties.getLocalizedHelp(key);};
}
