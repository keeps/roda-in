package org.roda.rodain.core.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.template.TemplateFieldValue;
import org.roda.rodain.core.template.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 * @since 2017-03-14 hsilva: changed class name from DescObjMetadata to
 *        DescriptiveMetadata
 */
@JsonIgnoreProperties({"path", "loaded", "values"})
public class DescriptiveMetadata {
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptiveMetadata.class.getName());
  private String id, content, contentEncoding, metadataType;
  private List<String> relatedTags = new ArrayList<>();
  private Map<String, Object> additionalProperties = new HashMap<>();
  private TreeSet<TemplateFieldValue> values;
  private Path path;
  private boolean loaded = false;

  // template
  private MetadataOption creatorOption;
  private String metadataVersion, templateType;

  public DescriptiveMetadata() {
    creatorOption = MetadataOption.NEW_FILE;
  }

  public DescriptiveMetadata(MetadataOption creatorOption, String templateType, String metadataType,
    String metadataVersion) {
    this.creatorOption = creatorOption;
    this.templateType = templateType;
    this.metadataVersion = metadataVersion;
    this.contentEncoding = Constants.ENCODING_BASE64;
    this.metadataType = metadataType;
    if (templateType != null) {
      this.id = templateType + Constants.MISC_XML_EXTENSION;
    } else {
      this.id = (metadataType != null ? metadataType : "") + (metadataVersion != null ? "_" + metadataVersion : "")
        + Constants.MISC_XML_EXTENSION;
    }
  }

  public DescriptiveMetadata(MetadataOption creatorOption, Path path, String metadataType, String metadataVersion,
    String templateType) {
    this.creatorOption = creatorOption;
    this.path = path;
    this.metadataType = metadataType;
    this.contentEncoding = Constants.ENCODING_BASE64;
    this.metadataVersion = metadataVersion;
    this.templateType = templateType;
    if (templateType != null) {
      if (templateType.endsWith(Constants.MISC_XML_EXTENSION)) {
        this.id = templateType;
      } else {
        this.id = templateType + Constants.MISC_XML_EXTENSION;
      }
    } else if (path != null) {
      this.id = path.getFileName().toString();
    }
  }

  public static DescriptiveMetadata buildDefaultDescObjMetadata() {
    return new DescriptiveMetadata(MetadataOption.TEMPLATE, "ead2002", "ead", "2002");
  }

  @JsonIgnore
  public Path getPath() {
    return path;
  }

  public void setPath(Path p) {
    path = p;
  }

  public void setMetadataType(String metadataType) {
    this.metadataType = metadataType;
  }

  public void setMetadataVersion(String metadataVersion) {
    this.metadataVersion = metadataVersion;
  }

  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  @JsonIgnore
  public boolean isLoaded() {
    return loaded;
  }

  public MetadataOption getCreatorOption() {
    return creatorOption;
  }

  public String getMetadataType() {
    return metadataType;
  }

  public void setCreatorOption(MetadataOption creatorOption) {
    this.creatorOption = creatorOption;
  }

  /**
   * @return The set of MetadataValue objects. Used to create the form.
   */
  @JsonIgnore
  public Set<TemplateFieldValue> getValues() {
    if (values == null) {
      // TODO FIX
      values = TemplateUtils.getTemplateFields(this);
    }
    return values;
  }

  public void setValues(TreeSet<TemplateFieldValue> val) {
    this.values = val;
  }

  public String getMetadataVersion() {
    return metadataVersion;
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
    } else {
      return "";
    }
  }

  private void loadMetadata() {
    try {
      if (creatorOption == MetadataOption.TEMPLATE) {
        if (getTemplateType() != null && getContent() == null) {
          String tempContent = ConfigurationManager.getTemplateContent(getTemplateType());
          setContentDecoded(TemplateUtils.getXMLFromTemplate(tempContent));
          loaded = true;
        } else {
          loaded = true;
        }
      } else if (path != null) {
        String tempContent = Controller.loadMetadataFile(path);
        setContentDecoded(tempContent);
        loaded = true;

      }
    } catch (IOException e) {
      LOGGER.error("Error reading metadata file", e);
    }
  }

  /**
   * Sets the content of the description object metadata. Set
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
  public InputStream getSchema() {
    // FIXME 20170307 hsilva: possible NPE
    InputStream result = null;
    if (templateType != null) {
      result = ConfigurationManager.getSchemaFile(templateType);
    } else {
      if (path != null)
        result = ConfigurationManager.getSchemaFile(FilenameUtils.removeExtension(path.getFileName().toString()));
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

  public List<String> getRelatedTags() {
    return relatedTags;
  }

  public void setRelatedTags(List<String> relatedTags) {
    this.relatedTags = relatedTags;
  }

  @Override
  public DescriptiveMetadata clone() {
    DescriptiveMetadata result = new DescriptiveMetadata();
    result.setCreatorOption(creatorOption);
    result.setId(id);
    result.setContent(content);
    result.setContentEncoding(contentEncoding);
    result.setValues((TreeSet<TemplateFieldValue>) values.clone());
    result.setPath(path);
    result.setMetadataVersion(metadataVersion);
    result.setMetadataType(metadataType);
    result.setTemplateType(templateType);
    result.setRelatedTags(relatedTags);
    return result;
  }

  @Override
  public String toString() {
    return "DescObjMetadata [id=" + id + ", content=" + content + ", contentEncoding=" + contentEncoding
      + ", metadataType=" + metadataType + ", additionalProperties=" + additionalProperties + ", values=" + values
      + ", path=" + path + ", loaded=" + loaded + ", creatorOption=" + creatorOption + ", metadataVersion="
      + metadataVersion + ", templateType=" + templateType + ", relatedTags=" + relatedTags + "]";
  }

  public void initializeValues() {
    values = TemplateUtils.getTemplateFields(this);
  }

}
