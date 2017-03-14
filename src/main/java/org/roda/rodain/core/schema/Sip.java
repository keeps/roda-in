package org.roda.rodain.core.schema;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.template.TemplateFieldValue;
import org.roda_project.commons_ip.model.IPContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 * @since 2017-03-14 hsilva: changed class name from DescriptionObject to Sip
 */
public class Sip extends Observable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sip.class.getName());
  private String title, id, parentId, descriptionlevel;
  private List<DescriptiveMetadata> metadata = new ArrayList<>();
  private Map<String, Object> additionalProperties = new TreeMap<>();
  private boolean isUpdateSIP = false;
  private String type;

  public Sip() {
    title = I18n.t(Constants.I18N_ROOT);
    id = Controller.createID();
    this.type = IPContentType.getMIXED().asString();
  }

  public Sip(DescriptiveMetadata template) {
    this();
    metadata.add(template);
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

  public List<DescriptiveMetadata> getMetadata() {
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
    for (DescriptiveMetadata dom : metadata) {
      result.put(dom.getId(), getMetadataWithReplaces(dom));
    }
    return result;
  }

  @JsonIgnore
  public String getMetadataWithReplaces(DescriptiveMetadata dom) {
    String content = dom.getContentDecoded();
    String templateContent = ConfigurationManager.getTemplateContent(dom.getTemplateType());
    if (content != null && dom.getCreatorOption() == MetadataOption.TEMPLATE) {
      try {
        Handlebars handlebars = new Handlebars();
        Map<String, String> data = new HashMap<>();
        handlebars.registerHelper("field", (o, options) -> options.fn());
        handlebars.registerHelper("ifCond", (context, options) -> {
          // the first parameter of ifCond is placed in the context field by the
          // parser
          String condition = (context == null) ? Constants.MISC_OR_OP : context.toString();
          List<Object> values = Arrays.asList(options.params);
          boolean display;
          if (condition.equals(Constants.MISC_OR_OP)) {
            display = false;
            for (Object value : values) {
              if (value != null) {
                display = true;
                break;
              }
            }
          } else if (condition.equals(Constants.MISC_AND_OP)) {
            display = true;
            for (Object value : values) {
              if (value == null) {
                display = false;
                break;
              }
            }
          } else {
            display = false;
          }
          return display ? options.fn() : options.inverse();
        });
        Template tmpl = handlebars.compileInline(templateContent);

        Set<TemplateFieldValue> values = getMetadataValueMap(dom);
        if (values != null) {
          values.forEach(metadataValue -> {
            if (metadataValue.get("value") != null && metadataValue.get("value") instanceof String) {
              String val = (String) metadataValue.get("value");
              if (val != null) {
                val = val.replaceAll("\\s", "");
                if (!"".equals(val)) {
                  data.put((String) metadataValue.get("name"), (String) metadataValue.get("value"));
                }
              }
            } else if (metadataValue.get("value") != null && metadataValue.get("value") instanceof Pair) {
              Pair valPair = (Pair) metadataValue.get("value");
              if (valPair != null) {
                String val = (String) valPair.getKey();
                val = val.replaceAll("\\s", "");
                if (!"".equals(val)) {
                  data.put((String) metadataValue.get("name"), (String) valPair.getKey());
                }
              }
            }

          });
        }
        content = tmpl.apply(data);
        content = Controller.indentXML(content);
      } catch (IOException e) {
        LOGGER.error("Error applying the values to the template", e);
      }
      // we need to clean the '\r' character in windows, otherwise the strings
      // are different even if no modification has been made
      content = content.replace("\r", "");
    }
    return content;
  }

  @JsonIgnore
  public Set<TemplateFieldValue> getMetadataValueMap(DescriptiveMetadata dom) {
    String content = ConfigurationManager.getTemplateContent(dom.getTemplateType());
    if (dom.getValues() == null) {
      dom.initializeValues();
    }
    if (content != null) {
      Set<TemplateFieldValue> values = dom.getValues();
      values.forEach(metadataValue -> {
        String autoGenerate = (String) metadataValue.get("auto-generate");
        if (autoGenerate != null && isEmptyMetadataValue(metadataValue.get("value"))) {
          autoGenerate = autoGenerate.toLowerCase();
          switch (autoGenerate) {
            case "title":
              metadataValue.set("value", title);
              break;
            case "now":
              metadataValue.set("value", new SimpleDateFormat(Constants.DATE_FORMAT_4).format(new Date()));
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

  private boolean isEmptyMetadataValue(Object value) {
    boolean isEmpty = false;

    if (value == null) {
      isEmpty = true;
    } else if (value instanceof String) {
      if (((String) value).trim().equalsIgnoreCase("")) {
        isEmpty = true;
      }
    } else if (value instanceof Pair) {
      // 20170307 hsilva: lets assume that if UIPair exists it isn't empty
      isEmpty = false;
    } else {
      LOGGER.error("Unknown data type: {} (value: {})", value.getClass().toString(), value);
    }
    return isEmpty;
  }

  // FIXME 20170315 hsilva:
  public void updatedMetadata(DescriptiveMetadata dom) {
    dom.getValues().forEach(metadataValue -> {
      String toSearch = metadataValue.getId().toLowerCase();
      switch (toSearch) {
        case "title":
          if (metadataValue.get("value") == null) {
            title = "";
          } else {
            title = (String) metadataValue.get("value");
          }
          break;
        case "id":
          id = (String) metadataValue.get("value");
          break;
        case "level":
          Object value = metadataValue.get("value");
          if (value != null) {
            if (value instanceof String) {
              descriptionlevel = (String) metadataValue.get("value");
            } else if (value instanceof Pair) {
              descriptionlevel = (String) ((Pair) metadataValue.get("value")).getKey();
            } else {
              descriptionlevel = value.toString();
            }
          }
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
  public void setMetadata(List<DescriptiveMetadata> metadata) {
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

  @Override
  public String toString() {
    return "DescriptionObject [title=" + title + ", id=" + id + ", parentId=" + parentId + ", descriptionlevel="
      + descriptionlevel + ", metadata=" + metadata + ", additionalProperties=" + additionalProperties + "]";
  }

  public boolean isUpdateSIP() {
    return isUpdateSIP;
  }

  public void setUpdateSIP(boolean isUpdateSIP) {
    this.isUpdateSIP = isUpdateSIP;
  }

  @JsonIgnore
  public IPContentType getContentType() {
    return new IPContentType(type);
  }

  public void setContentType(IPContentType contentType) {
    this.type = contentType.asString();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
