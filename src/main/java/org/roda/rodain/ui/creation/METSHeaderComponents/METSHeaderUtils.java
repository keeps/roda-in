package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda_project.commons_ip.model.IPHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;

/**
 * Utility class that provides a method the get the list of configurable
 * IPHeader form fields and to translate those configurable IPHeader form fields
 * into AbstractGroup objects.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class METSHeaderUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(METSHeaderUtils.class.getName());

  public static String[] getFieldList(Constants.SipType sipType) {
    String fieldList = ConfigurationManager.getConfig(
      Constants.CONF_K_METS_HEADER_FIELDS_PREFIX + sipType.name() + Constants.CONF_K_METS_HEADER_FIELDS_SUFFIX);
    if (StringUtils.isNotBlank(fieldList)) {
      return fieldList.split(Constants.MISC_COMMA);
    } else {
      return new String[] {};
    }
  }

  public static AbstractGroup getComponentForField(Constants.SipType sipType, String shortId, IPHeader savedHeader) {
    String type = getFieldParameterAsString(sipType, shortId, Constants.CONF_K_METS_HEADER_FIELD_TYPE, "");
    if (type.equalsIgnoreCase(Constants.CONF_V_METS_HEADER_FIELD_TYPE_RECORDSTATUS)) {
      return new StatusGroup(sipType, shortId, savedHeader);
    } else if (type.equalsIgnoreCase(Constants.CONF_V_METS_HEADER_FIELD_TYPE_ALTRECORDID)) {
      return new AltRecordGroup(sipType, shortId, savedHeader);
    } else if (type.equalsIgnoreCase(Constants.CONF_V_METS_HEADER_FIELD_TYPE_AGENT)) {
      return new AgentGroup(sipType, shortId, savedHeader);
    } else {
      LOGGER.error("Invalid METS Header field type: {}", type);
      return null;
    }
  }

  /**
   * Does a depth-first traversal of the element tree, starting with the parent
   * node. When the focusAfterThisNode node is found, the next textfield is
   * focused.
   * 
   * @param first
   *          Initial node
   * @param focusAfterThisNode
   *          The next textfield found after this node should be selected
   */
  public static void tryToFocusNextTextField(Parent first, Node focusAfterThisNode) {
    LinkedList<Node> nodeQueue = new LinkedList<>();
    boolean focusNow = false;

    nodeQueue.add(first);
    while (!nodeQueue.isEmpty()) {
      Node node = nodeQueue.pop();
      if (node != null) {
        if (node == focusAfterThisNode) {
          focusNow = true;
        } else if (focusNow && node instanceof TextField) {
          TextField fieldToFocus = (TextField) node;
          fieldToFocus.requestFocus();
          nodeQueue.clear();
        } else if (node instanceof Parent) {
          Parent parent = ((Parent) node);
          nodeQueue.addAll(0, parent.getChildrenUnmodifiable());
        }
      }
    }
  }

  /*
   * Get values from config
   * ____________________________________________________________________________________________________________________
   */
  public static String getFieldParameterAsString(Constants.SipType sipType, String fieldShortId, String suffix,
    String defaultValue) {
    String fieldParameterAsString = getFieldParameter(sipType, fieldShortId, suffix);
    if (StringUtils.isNotBlank(fieldParameterAsString)) {
      return fieldParameterAsString;
    }
    return defaultValue;
  }

  public static String[] getFieldParameterAsStringArray(Constants.SipType sipType, String fieldShortId, String suffix,
    String[] defaultValue) {
    String[] fieldParameterAsStringArray = getFieldParameterArray(sipType, fieldShortId, suffix);
    if (fieldParameterAsStringArray != null && fieldParameterAsStringArray.length > 0) {
      return fieldParameterAsStringArray;
    }
    return defaultValue;
  }

  public static Integer getFieldParameterAsInteger(Constants.SipType sipType, String fieldShortId, String suffix,
    Integer defaultValue) {
    String fieldParameterAsString = getFieldParameter(sipType, fieldShortId, suffix);
    if (StringUtils.isNotBlank(fieldParameterAsString)) {
      if (fieldParameterAsString.equalsIgnoreCase(Constants.CONF_V_METS_HEADER_FIELD_AMOUNT_MAX_INFINITE)) {
        return Integer.MAX_VALUE;
      } else {
        try {
          return Integer.valueOf(fieldParameterAsString);
        } catch (NumberFormatException e) {
          // let it return the default value
        }
      }
    }
    return defaultValue;
  }

  public static Boolean getFieldParameterAsBoolean(Constants.SipType sipType, String fieldShortId, String suffix,
    boolean defaultValue) {
    return ConfigurationManager.getConfigAsBoolean(Constants.CONF_K_METS_HEADER_FIELDS_PREFIX + sipType.name()
      + Constants.CONF_K_METS_HEADER_FIELD_SEPARATOR + fieldShortId + suffix, defaultValue);
  }

  public static String getTextFromI18N(String key) {
    if (StringUtils.isNotBlank(key)) {
      String value = I18n.t(key);
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return "???" + key + "???";
  }

  private static String getFieldParameter(Constants.SipType sipType, String fieldShortId, String suffix) {
    return ConfigurationManager.getConfig(Constants.CONF_K_METS_HEADER_FIELDS_PREFIX + sipType.name()
      + Constants.CONF_K_METS_HEADER_FIELD_SEPARATOR + fieldShortId + suffix);
  }

  private static String[] getFieldParameterArray(Constants.SipType sipType, String fieldShortId, String suffix) {
    return ConfigurationManager.getConfigAsStringArray(Constants.CONF_K_METS_HEADER_FIELDS_PREFIX + sipType.name()
      + Constants.CONF_K_METS_HEADER_FIELD_SEPARATOR + fieldShortId + suffix);
  }

}
