package org.roda.rodain.rules.sip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.XMLToMetadataValue;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 23/11/2015.
 */
public class SipMetadata {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipMetadata.class.getName());
  private MetadataTypes type;
  private String version;
  private String templateType;
  private boolean loaded = false, modified = false;
  private String template, content;
  private Path path;
  private Map<String, MetadataValue> values;

  /**
   * Creates a new SipMetadata object.
   *
   * @param type         The metadata type
   * @param path         The path to the metadata file
   * @param templateType The type of the metadata template
   */
  public SipMetadata(MetadataTypes type, Path path, String templateType, String version) {
    this.type = type;
    this.path = path;
    this.templateType = templateType;
    this.version = version;
    this.values = new HashMap<>();
  }

  /**
   * @return True if the metadata has been modified, false otherwise.
   */
  public boolean isModified() {
    return modified;
  }

  private void loadMetadata() {
    try {
      if (type == MetadataTypes.TEMPLATE) {
        if (templateType != null) {
          template = AppProperties.getMetadataFile(templateType);
          content = template;
          loaded = true;
        }
      } else {
        if (path != null) {
          content = Utils.readFile(path.toString(), Charset.defaultCharset());
          loaded = true;
        }
      }
    } catch (IOException e) {
      log.error("Error reading metadata file", e);
    }
  }

  /**
   * Gets the metadata content of a SIP, loading it from the disk if that action
   * hasn't been previously done.
   *
   * @return The metadata content of the SIP.
   */
  public String getMetadataContent() {
    if (!loaded) {
      loadMetadata();
    }
    return content;
  }

  /**
   * @return The set of MetadataValue objects. Used to create the form.
   */
  public Map<String, MetadataValue> getValues() {
    values = XMLToMetadataValue.createEADMetadataValues(getMetadataContent(), values);
    return values;
  }

  /**
   * @return The type of the metadata.
   */
  public String getTemplateType() {
    return templateType;
  }

  /**
   * @return The version of the metadata.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Updates the metadata content of the SIP.
   *
   * @param meta The new metadata content.
   */
  public void update(String meta) {
    modified = true;
    content = meta;
  }

  /**
   * Sets the metadata content of the SIP.
   * This will not flag the SIP as edited! Use update() instead.
   *
   * @param meta The new metadata content.
   * @see #update(String)
   */
  public void setContent(String meta) {
    content = meta;
  }

  /**
   * Applies the data from the MetadataValues to the XML string.
   */
  public void applyMetadataValues() {
    String result = XMLToMetadataValue.applyMetadataValues(getMetadataContent(), values);
    update(result);
  }
}
