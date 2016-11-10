package org.roda.rodain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.util.Base64;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.template.TemplateFieldValue;
import org.roda.rodain.template.TemplateUtils;
import org.roda.rodain.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
@JsonIgnoreProperties({"path", "loaded","values"})
public class DescObjMetadata {
  private static final Logger LOGGER = LoggerFactory.getLogger(DescObjMetadata.class.getName());
  private String id, content, contentEncoding, metadataType, label;
  private Map<String, Object> additionalProperties = new HashMap<>();
  private TreeSet<TemplateFieldValue> values;
  private Path path;
  private boolean loaded = false;

  
  // template
  private MetadataOptions creatorOption;
  private String metadataVersion, templateType;

  public DescObjMetadata() {
    creatorOption = MetadataOptions.NEW_FILE;
  }

  public DescObjMetadata(MetadataOptions creatorOption, String templateType, String metadataType, String metadataVersion) {
    this.creatorOption = creatorOption;
    this.templateType = templateType;
    this.metadataVersion = metadataVersion;
    this.contentEncoding = "Base64";
    this.id = templateType + ".xml.hbs";
    this.metadataType = metadataType;
  }

  public DescObjMetadata(MetadataOptions creatorOption, Path path, String metadataType, String metadataVersion) {
    this.creatorOption = creatorOption;
    this.path = path;
    this.metadataType = metadataType;
    this.contentEncoding = "Base64";
    this.metadataVersion = metadataVersion;
    if (path != null) {
      this.id = path.getFileName().toString();
    }
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

  public MetadataOptions getCreatorOption() {
    return creatorOption;
  }

  public String getMetadataType() {
    return metadataType;
  }

  public void setCreatorOption(MetadataOptions creatorOption) {
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
    /*if(templateType==null){
      LOGGER.error("NO TEMPLATE FOR "+this.toString());
      String fake = (metadataType+metadataVersion).replaceAll("-", "");
      LOGGER.error("USING FAKE: "+fake);
      return fake.toLowerCase();
    }*/
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
    } else{
      return "";
    }
  }

  private void loadMetadata() {
    try {
      if (creatorOption == MetadataOptions.TEMPLATE) {
        if (getTemplateType() != null && getContent()==null) {
          String tempContent = AppProperties.getTemplateContent(getTemplateType());
          setContentDecoded(TemplateUtils.getXMLFromTemplate(tempContent));
          loaded = true;
        }else{
          loaded = true;
        }
      } else if(path!=null){
          String tempContent = Utils.readFile(path.toString(), Charset.forName("UTF-8"));
          setContentDecoded(tempContent);
          loaded = true;
        
      }
    } catch (IOException e) {
      LOGGER.error("Error reading metadata file", e);
    }
  }

  /**
   * Sets the content of the description object metadata.
   *Set
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

  public DescObjMetadata clone() {
    DescObjMetadata result = new DescObjMetadata();
    result.setCreatorOption(creatorOption);
    result.setId(id);
    result.setContent(content);
    result.setContentEncoding(contentEncoding);
    result.setValues((TreeSet<TemplateFieldValue>) values.clone());
    result.setPath(path);
    result.setMetadataVersion(metadataVersion);
    result.setMetadataType(metadataType);
    result.setTemplateType(templateType);
    return result;
  }

  @Override
  public String toString() {
    return "DescObjMetadata [id=" + id + ", content=" + content + ", contentEncoding=" + contentEncoding
      + ", metadataType=" + metadataType + ", additionalProperties=" + additionalProperties + ", values=" + values
      + ", path=" + path + ", loaded=" + loaded + ", creatorOption=" + creatorOption + ", metadataVersion=" + metadataVersion
      + ", templateType=" + templateType + "]";
  }

  public void initializeValues() {
    values = TemplateUtils.getTemplateFields(this);
  }

  
  
}
