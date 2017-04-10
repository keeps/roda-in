package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.Constants;
import org.roda_project.commons_ip.model.IPAgent;
import org.roda_project.commons_ip.model.IPHeader;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class AgentGroup extends AbstractGroup {
  // i18n configurable strings
  private String i18nNameLabel;
  private String i18nNameDescription;
  private String i18nNoteLabel;
  private String i18nNoteDescription;
  private String i18nTypeLabel;
  private String i18nTypeDescription;
  private String i18nOtherTypeLabel;
  private String i18nOtherTypeDescription;
  private String i18nRoleLabel;
  private String i18nRoleDescription;

  // mandatory field configs
  private boolean mandatoryNote;

  // default (pre-selected and non-modifiable) values
  private String predefinedType;
  private String predefinedRole;
  private String predefinedOtherType;

  private Iterator<IPAgent> savedItemIterator = Collections.emptyIterator();

  public AgentGroup(Constants.SipType sipType, String shortId, IPHeader savedHeader) {
    super(sipType, shortId, savedHeader);
  }

  @Override
  public String getHeaderText() {
    StringBuilder text = new StringBuilder();
    text.append("Agents");

    if (StringUtils.isNotBlank(predefinedType) || StringUtils.isNotBlank(predefinedOtherType)
      || StringUtils.isNotBlank(predefinedRole)) {
      text.append(" having");

      if (StringUtils.isNotBlank(predefinedRole)) {
        text.append(" role ").append(predefinedRole);
        if (StringUtils.isNotBlank(predefinedType) || StringUtils.isNotBlank(predefinedOtherType)) {
          text.append(" and");
        }
      }
      if (StringUtils.isNotBlank(predefinedType)) {
        text.append(" type ").append(predefinedType);
        if (StringUtils.isNotBlank(predefinedOtherType)) {
          text.append(" (").append(predefinedOtherType).append(")");
        }
      } else {
        if (StringUtils.isNotBlank(predefinedOtherType)) {
          text.append(" other type ").append(predefinedOtherType);
        }
      }
    }

    return text.toString();
  }

  @Override
  protected AbstractItem internalCreateRow(boolean usingSavedItems) {
    // if we have a saved header, try to fill the form with the next Agent
    // that can be used, according to the form config. If that fails add a
    // default (empty) field.
    if (usingSavedItems) {
      getSavedItemIterator();
      while (savedItemIterator.hasNext()) {
        IPAgent savedItem = savedItemIterator.next();

        boolean validPredefinedType = StringUtils.isBlank(predefinedType)
          || (StringUtils.isNotBlank(predefinedType) && predefinedType.equals(savedItem.getType().toString()));

        boolean validPredefinedRole = StringUtils.isBlank(predefinedRole)
          || (StringUtils.isNotBlank(predefinedRole) && predefinedRole.equals(savedItem.getRole()));

        boolean validPredefinedOtherType = StringUtils.isBlank(predefinedOtherType)
          || (StringUtils.isNotBlank(predefinedOtherType) && predefinedOtherType.equals(savedItem.getOtherType()));

        if (validPredefinedType && validPredefinedOtherType && validPredefinedRole) {
          return new AgentItem(this, i18nNameLabel, i18nNameDescription, i18nNoteLabel, i18nNoteDescription,
            mandatoryNote, i18nTypeLabel, i18nTypeDescription, predefinedType, i18nRoleLabel, i18nRoleDescription,
            predefinedRole, i18nOtherTypeLabel, i18nOtherTypeDescription, predefinedOtherType, savedItem);
        }
      }
      return null;
    } else {
      return new AgentItem(this, i18nNameLabel, i18nNameDescription, i18nNoteLabel, i18nNoteDescription, mandatoryNote,
        i18nTypeLabel, i18nTypeDescription, predefinedType, i18nRoleLabel, i18nRoleDescription, predefinedRole,
        i18nOtherTypeLabel, i18nOtherTypeDescription, predefinedOtherType);
    }
  }

  @Override
  protected void initBeforeGUI(IPHeader savedHeader) {
    super.initBeforeGUI(savedHeader);

    this.i18nNameLabel = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NAME_LABEL, ""));

    this.i18nNameDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NAME_DESCRIPTION, ""));

    this.i18nNoteLabel = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NOTE_LABEL, ""));

    this.i18nNoteDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NOTE_DESCRIPTION, ""));

    this.mandatoryNote = getFieldParameterAsBoolean(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NOTE_MANDATORY, false);

    this.i18nTypeLabel = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_LABEL, ""));

    this.i18nTypeDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_DESCRIPTION, ""));

    this.predefinedType = getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_VALUE, "");

    this.i18nRoleLabel = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_ROLE_LABEL, ""));

    this.i18nRoleDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_ROLE_DESCRIPTION, ""));

    this.predefinedRole = getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_ROLE_VALUE, "");

    this.i18nOtherTypeLabel = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_OTHERTYPE_LABEL, ""));

    this.i18nOtherTypeDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_OTHERTYPE_DESCRIPTION, ""));

    this.predefinedOtherType = getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_OTHERTYPE_VALUE,
      "");
  }

  public void addAgentsToHeader(IPHeader header) {
    for (AbstractItem abstractItem : getItems()) {
      if (abstractItem.isValid()) {
        header.addAgent(((AgentItem) abstractItem).getValue());
      }
    }
  }

  @Override
  protected Iterator getSavedItemIterator() {
    if (savedItemIterator == null) {
      if (getSavedHeader() != null && getSavedHeader().getAgents() != null) {
        savedItemIterator = getSavedHeader().getAgents().iterator();
      } else {
        savedItemIterator = Collections.emptyIterator();
      }
    }
    return savedItemIterator;
  }
}
