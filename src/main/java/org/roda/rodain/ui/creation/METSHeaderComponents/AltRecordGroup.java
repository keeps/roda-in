package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.Constants;
import org.roda_project.commons_ip.model.IPAltRecordID;
import org.roda_project.commons_ip.model.IPHeader;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class AltRecordGroup extends AbstractGroup {
  // i18n configurable strings
  private String i18nValueLabel;
  private String i18nValueDescription;
  private String i18nTypeLabel;
  private String i18nTypeDescription;

  // default (pre-selected and non-modifiable) values
  private String predefinedType;

  private String[] possibleValues;
  private Set<String> possibleValuesSet;

  private Iterator<IPAltRecordID> savedItemIterator;

  public AltRecordGroup(Constants.SipType sipType, String shortId, IPHeader savedHeader) {
    super(sipType, shortId, savedHeader);
  }

  @Override
  public String getHeaderText() {
    if (StringUtils.isNotBlank(predefinedType)) {
      return "AltRecords of type " + predefinedType;
    } else {
      return "AltRecords";
    }
  }

  @Override
  protected AbstractItem internalCreateRow(boolean usingSavedItems) {
    // if we have a saved header, try to fill the form with the next AltRecord
    // that can be used, according to the form config. If that fails add a
    // default (empty) field.
    if (usingSavedItems) {
      getSavedItemIterator();
      while (savedItemIterator.hasNext()) {
        IPAltRecordID savedItem = savedItemIterator.next();

        boolean validPredefinedType = StringUtils.isBlank(predefinedType) || predefinedType.equals(savedItem.getType());

        boolean validValue = possibleValuesSet.isEmpty() || possibleValuesSet.contains(savedItem.getValue());

        if (validPredefinedType && validValue) {
          return new AltRecordItem(this, i18nValueLabel, i18nValueDescription, i18nTypeLabel, i18nTypeDescription,
            predefinedType, possibleValues, savedItem);
        }
      }
      return null;
    } else {
      return new AltRecordItem(this, i18nValueLabel, i18nValueDescription, i18nTypeLabel, i18nTypeDescription,
        predefinedType, possibleValues);
    }
  }

  @Override
  protected void initBeforeGUI(IPHeader savedHeader) {
    super.initBeforeGUI(savedHeader);

    this.i18nValueLabel = getTextFromI18N(getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_LABEL, ""));

    this.i18nValueDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_DESCRIPTION, ""));

    this.i18nTypeLabel = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_LABEL, ""));

    this.i18nTypeDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_DESCRIPTION, ""));

    this.predefinedType = getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_VALUE, "");

    this.possibleValues = getFieldParameterAsStringArray(Constants.CONF_K_METS_HEADER_FIELD_COMBO_VALUES,
      new String[] {});
    this.possibleValuesSet = new LinkedHashSet<>(Arrays.asList(possibleValues));
  }

  public void addAltRecordsToHeader(IPHeader header) {
    for (AbstractItem abstractItem : getItems()) {
      if (!abstractItem.isEmpty() && abstractItem.isValid()) {
        header.addAltRecordID(((AltRecordItem) abstractItem).getValue());
      }
    }
  }

  @Override
  protected Iterator getSavedItemIterator() {
    if (savedItemIterator == null) {
      if (getSavedHeader() != null && getSavedHeader().getAltRecordIDs() != null) {
        savedItemIterator = getSavedHeader().getAltRecordIDs().iterator();
      } else {
        savedItemIterator = Collections.emptyIterator();
      }
    }
    return savedItemIterator;
  }
}
