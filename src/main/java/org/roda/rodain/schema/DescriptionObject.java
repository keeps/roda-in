package org.roda.rodain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.MetadataOptions;
import org.roda.rodain.sip.MetadataValue;
import org.roda.rodain.utils.Utils;

import java.io.IOException;
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
    id = "ID" + UUID.randomUUID().toString();
    metadata.add(new DescObjMetadata(MetadataOptions.TEMPLATE, "ead", "ead2002", "2002"));
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

      try {
        Handlebars handlebars = new Handlebars();
        Map<String, String> data = new HashMap<>();
        handlebars.registerHelper("field", (o, options) -> options.fn());
        Template tmpl = handlebars.compileInline(content);

        Set<MetadataValue> values = getMetadataValueMap(dom);
        if (values != null) {
          values.forEach(metadataValue -> {
            String val = (String) metadataValue.get("value");
            if (val != null) {
              val = val.replaceAll("\\s", "");
              if (!"".equals(val)) {
                data.put((String)metadataValue.get("name"), (String)metadataValue.get("value"));
              }
            }
          });
        }
        content = tmpl.apply(data);
        content = Utils.indentXML(content);
      } catch (IOException e) {
        // log
      }
      // we need to clean the '\r' character in windows,
      // otherwise the strings are different even if no modification has been
      // made
      content = content.replace("\r", "");
    }
    return content;
  }

  @JsonIgnore
  public Set<MetadataValue> getMetadataValueMap(DescObjMetadata dom) {
    String content = dom.getContentDecoded();
    if (content != null) {
      Set<MetadataValue> values = dom.getValues();
      values.forEach(metadataValue -> {
        String autoGenerate = (String) metadataValue.get("auto-generate");
        if (autoGenerate != null) {
          autoGenerate = autoGenerate.toLowerCase();
          switch (autoGenerate) {
            case "title":
              metadataValue.set("value", title);
              break;
            case "now":
              metadataValue.set("value", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
              break;
            case "id":
              metadataValue.set("value", id);
              break;
            case "level":
              metadataValue.set("value", descriptionlevel);
              break;
            case "parentid":
              metadataValue.set("value", parentId);
              break;
            case "language":
              metadataValue.set("value", Locale.getDefault().getDisplayLanguage());
              break;
          }
        }
      });
      return values;
    }
    return null;
  }

  public void updatedMetadata(DescObjMetadata dom) {
    dom.getValues().forEach(metadataValue -> {
      String toSearch = metadataValue.getId().toLowerCase();
      switch (toSearch) {
        case "title":
          if (metadataValue.get("value") == null)
            title = "";
          else
            title = (String) metadataValue.get("value");
          break;
        case "id":
          id = (String) metadataValue.get("value");
          break;
        case "level":
          descriptionlevel = (String) metadataValue.get("value");
          break;
        case "otherlevel":
          if(metadataValue.get("value") != null && descriptionlevel != null && descriptionlevel.equals("otherlevel"))
            descriptionlevel = (String) metadataValue.get("value");
          break;
        case "parentid":
          parentId = (String) metadataValue.get("value");
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
