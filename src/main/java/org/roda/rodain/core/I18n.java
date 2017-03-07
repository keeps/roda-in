package org.roda.rodain.core;

/**
 * Alias for the getLocalizedString method of the AppProperties class.
 * 
 * @author Andre Pereira apereira@keep.pt
 * @since 22-03-2016.
 */
public class I18n {

  /**
   * Gets the localized message for the key parameter using the selected locale.
   * 
   * @param key
   *          The key of the required localized message
   * @return The localized message
   */
  public static String t(String key) {
    return ConfigurationManager.getLocalizedString(key);
  }

  /**
   * Gets the localized message for the key parameter using the parametrized
   * locale.
   * 
   * @param key
   *          The key of the required localized message
   * @param languageTag
   *          The language of the message.
   * @return The localized message
   */
  public static String t(String key, String languageTag) {
    return ConfigurationManager.getLocalizedString(key, languageTag);
  }

  /**
   * Gets the localized help message for the key parameter using the selected
   * locale.
   * 
   * @param key
   *          The key of the required help message
   * @return The localized help message
   */
  public static String help(String key) {
    return ConfigurationManager.getLocalizedHelp(key);
  };
}
