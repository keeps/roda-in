package org.roda.rodain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.sip.MetadataValue;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
public class DescriptionObject extends Observable {
  private String title, id, parentId, descriptionlevel;
  private List<DescObjMetadata> metadata = new ArrayList<>();
  private Map<String, Object> additionalProperties = new TreeMap<>();

  public DescriptionObject() {
    title = I18n.t("root");
    id = UUID.randomUUID().toString();
    metadata.add(new DescObjMetadata(MetadataTypes.TEMPLATE, "ead", "2002"));
  }

  /**
   * Gets the title of the description object
   *
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of the description object
   *
   * @param title
   *          The title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the id of the description object
   *
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the description object
   *
   * @param id
   *          The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the parentId of the description object
   *
   * @return The parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * Sets the parentId of the description object
   *
   * @param parentId
   *          The parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * Gets the description level of the description object
   *
   * @return The descriptionLevel
   */
  public String getDescriptionlevel() {
    return descriptionlevel;
  }

  /**
   * Sets the description level of the description object
   *
   * @param descriptionLevel
   *          The description level
   */
  public void setDescriptionlevel(String descriptionLevel) {
    this.descriptionlevel = descriptionLevel;
  }

  public List<DescObjMetadata> getMetadata() {
    return metadata;
  }

  /**
   * Gets the metadata list of the description object, replacing the fields from
   * the template.
   *
   * @return The metadata list
   */
  @JsonIgnore
  public Map<String, String> getMetadataWithReplaces() {
    Map<String, String> result = new HashMap<>();
    for (DescObjMetadata dom : metadata) {
      result.put(dom.getId(), getMetadataWithReplaces(dom));
    }
    return result;
  }

  @JsonIgnore
  public String getMetadataWithReplaces(DescObjMetadata dom) {
    String content = dom.getContentDecoded();
    if (content != null) {
      Template tmpl = Mustache.compiler().defaultValue("").compile(content);
      Map<String, String> data = new HashMap<>();
      Map<String, MetadataValue> values = getMetadataValueMap(dom);
      if (values != null) {
        values.forEach((s, metadataValue) -> data.put(s, metadataValue.getValue()));
      }
      content = tmpl.execute(data);
      // we need to clean the '\r' character in windows,
      // otherwise the strings are different even if no modification has been
      // made
      content = content.replace("\r", "");
    }
    return content;
  }

  @JsonIgnore
  public Map<String, MetadataValue> getMetadataValueMap(DescObjMetadata dom) {
    String content = dom.getContentDecoded();
    if (content != null) {
      Map<String, MetadataValue> values = dom.getValues();
      values.forEach((s, metadataValue) -> {
        String toSearch = s.toLowerCase();
        switch (toSearch) {
          case "title":
            metadataValue.setValue(title);
            break;
          case "now":
            metadataValue.setValue(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            break;
          case "id":
            metadataValue.setValue(id);
            break;
          case "level":
            metadataValue.setValue(descriptionlevel);
            break;
          case "parentid":
            metadataValue.setValue(parentId);
            break;
        }
      });
      return values;
    }
    return null;
  }

  public void updatedMetadata(DescObjMetadata dom) {
    dom.getValues().forEach((s, metadataValue) -> {
      String toSearch = s.toLowerCase();
      switch (toSearch) {
        case "title":
          title = metadataValue.getValue();
          break;
        case "id":
          id = metadataValue.getValue();
          break;
        case "level":
          descriptionlevel = metadataValue.getValue();
          break;
        case "parentid":
          parentId = metadataValue.getValue();
          break;
      }
    });
  }

  /**
   * Sets the metadata list of the description object
   *
   * @param metadata
   *          The metadata list
   */
  public void setMetadata(List<DescObjMetadata> metadata) {
    this.metadata = metadata;
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
