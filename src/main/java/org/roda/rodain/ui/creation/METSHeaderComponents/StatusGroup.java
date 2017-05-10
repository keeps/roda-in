package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.Collections;
import java.util.Iterator;

import org.roda.rodain.core.Constants;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip.utils.IPEnums;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class StatusGroup extends AbstractGroup {
  // i18n configurable strings
  private String i18nValueLabel;
  private String i18nValueDescription;

  private Iterator<IPEnums.IPStatus> savedItemIterator;

  public StatusGroup(Constants.SipType sipType, String shortId, IPHeader savedHeader) {
    super(sipType, shortId, savedHeader);
  }

  @Override
  public String getHeaderText() {
    return "Record Status";
  }

  @Override
  protected AbstractItem internalCreateRow(boolean usingSavedItems) {
    if (usingSavedItems) {
      getSavedItemIterator();
      if (savedItemIterator.hasNext()) {
        return new StatusItem(this, i18nValueLabel, i18nValueDescription, savedItemIterator.next());
      } else {
        return null;
      }
    } else {
      return new StatusItem(this, i18nValueLabel, i18nValueDescription);
    }
  }

  @Override
  protected void initBeforeGUI(IPHeader savedHeader) {
    super.initBeforeGUI(savedHeader);

    this.i18nValueLabel = getTextFromI18N(getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_LABEL, ""));

    this.i18nValueDescription = getTextFromI18N(
      getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_DESCRIPTION, ""));
  }

  public void addStatusToHeader(IPHeader header) {
    for (AbstractItem abstractItem : getItems()) {
      if (!abstractItem.isEmpty() && abstractItem.isValid()) {
        header.setStatus(((StatusItem) abstractItem).getValue());
      }
    }
  }

  @Override
  protected Iterator getSavedItemIterator() {
    if (savedItemIterator == null) {
      if (getSavedHeader() != null && getSavedHeader().getStatus() != null) {
        savedItemIterator = Collections.singletonList(getSavedHeader().getStatus()).iterator();
      } else {
        savedItemIterator = Collections.emptyIterator();
      }
    }
    return savedItemIterator;
  }
}
