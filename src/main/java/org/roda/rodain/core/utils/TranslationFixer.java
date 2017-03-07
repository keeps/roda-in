package org.roda.rodain.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class TranslationFixer {
  public static void main(String[] args) {
    try {
      String otherTranslationFile = "properties/help_hu.properties";
      String enTranslationFile = "properties/help_en.properties";

      Path otherTemp = Files.createTempFile("lang", ".properties");
      Path enTemp = Files.createTempFile("lang", ".properties");

      Files.copy(ClassLoader.getSystemResourceAsStream(otherTranslationFile), otherTemp,
        StandardCopyOption.REPLACE_EXISTING);
      Files.copy(ClassLoader.getSystemResourceAsStream(enTranslationFile), enTemp, StandardCopyOption.REPLACE_EXISTING);

      Configuration enConf = new PropertiesConfiguration(enTemp.toFile());
      Configuration otherConf = new PropertiesConfiguration(otherTemp.toFile());

      Iterator<String> confIt = enConf.getKeys();
      while (confIt.hasNext()) {
        String key = confIt.next();
        if (!otherConf.containsKey(key)) {
          System.out.println(key + " - " + enConf.getString(key));
        } else if (otherConf.getString(key).equalsIgnoreCase(enConf.getString(key))) {
          System.out.println("-> " + key);
        }
      }
    } catch (IOException | ConfigurationException e) {
      System.out.println("Error while running translation fixer: " + e.getMessage());
    }
  }
}
