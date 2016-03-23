package org.roda.rodain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.util.Base64;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.XMLToMetadataValue;
import org.roda.rodain.rules.sip.MetadataValue;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
@JsonIgnoreProperties({"values", "path", "loaded", "type", "version", "templateType"})
public class DescObjMetadata {
  private static final Logger log = LoggerFactory.getLogger(DescObjMetadata.class.getName());
  private String id, content, contentEncoding;
  private Map<String, Object> additionalProperties = new HashMap<>();
  private Map<String, MetadataValue> values = new HashMap<>();
  private Path path;
  private boolean loaded = false;

  // template
  private MetadataTypes type;
  private String version, templateType;

  public DescObjMetadata() {

  }

  public DescObjMetadata(MetadataTypes type, String templateType, String version) {
    this.type = type;
    this.templateType = templateType;
    this.version = version;
    this.contentEncoding = "Base64";
    this.id = templateType + ".xml";
  }

  public DescObjMetadata(MetadataTypes type, Path path) {
    this.type = type;
    this.path = path;
    this.contentEncoding = "Base64";
    if (path != null) {
      this.id = path.getFileName().toString();
    }
  }

  /**
   * @return The set of MetadataValue objects. Used to create the form.
   */
  @JsonIgnore
  public Map<String, MetadataValue> getValues() throws SAXException {
    values = XMLToMetadataValue.createEADMetadataValues(getContentDecoded());
    return values;
  }

  @JsonProperty
  public void setValues(Map<String, MetadataValue> val) {
    this.values = val;
  }

  /**
   * Applies the data from the MetadataValues to the XML string.
   */
  public void applyMetadataValues() {
    String result = XMLToMetadataValue.applyMetadataValues(getContentDecoded(), values);
    setContentDecoded(result);
  }

  public String getVersion() {
    return version;
  }

  public String getTemplateType() {
    return templateType;
  }

  /**
   * Gets the id of the description object metadata.
   *
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the description object metadata.
   *
   * @param id
   *          The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the content of the description object metadata.
   *
   * @return The content
   */
  public String getContent() {
    if (content == null) {
      loadMetadata();
    }
    return this.content;
  }

  /**
   * Gets the decoded content of the description object metadata.
   *
   * @return The content decoded
   */
  @JsonIgnore
  public String getContentDecoded() {
    if (!loaded) {
      loadMetadata();
    }
    if (content != null) {
      byte[] decoded = Base64.decodeBase64(content);
      return new String(decoded);
    } else
      return "";
  }

  private void loadMetadata() {
    try {
      if (type == MetadataTypes.TEMPLATE) {
        if (templateType != null) {
          String tempContent = AppProperties.getMetadataFile(templateType);
          setContentDecoded(tempContent);
          loaded = true;
        }
      } else {
        if (path != null) {
          String tempContent = Utils.readFile(path.toString(), Charset.defaultCharset());
          setContentDecoded(tempContent);
          loaded = true;
        }
      }
    } catch (IOException e) {
      log.error("Error reading metadata file", e);
    }
  }

  /**
   * Sets the content of the description object metadata.
   *
   * @param content
   *          The content encoded in Base64
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Sets the content of the description object metadata.
   *
   * @param content
   *          The decoded content
   */
  public void setContentDecoded(String content) {
    if (content != null) {
      byte[] encoded = Base64.encodeBase64(content.getBytes());
      this.content = new String(encoded);
    }
  }

  /**
   * Gets the content encoding of the description object metadata.
   *
   * @return The content encoding
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Sets the content encoding of the description object metadata.
   *
   * @param contentEncoding
   *          The contentEncoding
   */
  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  @JsonIgnore
  public String getSchema() {
    String result = null;
    if (templateType != null) {
      result = AppProperties.getSchemaFile(templateType);
    } else {
      if (path != null)
        result = AppProperties.getSchemaFile(FilenameUtils.removeExtension(path.getFileName().toString()));
    }
    return result;
  }

  /**
   * Gets the additional properties map.
   *
   * @return The additional properties map.
   */
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  /**
   * Sets an additional property.
   *
   * @param name
   *          The name of the property.
   * @param value
   *          The value of the property.
   */
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
