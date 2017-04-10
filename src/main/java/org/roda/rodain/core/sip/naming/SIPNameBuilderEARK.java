package org.roda.rodain.core.sip.naming;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Constants.SipType;
import org.roda.rodain.core.schema.Sip;

/**
 * SIP naming for EARK SIPs.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIPNameBuilderEARK implements SIPNameBuilder {
  private String prefix;
  private SipNameStrategy strategy;

  public SIPNameBuilderEARK(String prefix, SipNameStrategy strategy) {
    this.prefix = prefix;
    this.strategy = strategy;

    if (!getSupportedSIPNameStrategies().contains(getSIPNameStrategy())) {
      throw new IllegalArgumentException(
        getSIPNameStrategy() + " is not a valid strategy for an " + getSIPType() + " SIP.");
    }
  }

  @Override
  public SipNameStrategy getSIPNameStrategy() {
    return strategy;
  }

  @Override
  public Set<SipNameStrategy> getSupportedSIPNameStrategies() {
    return getSIPType().getSipNameStrategies();
  }

  @Override
  public SipType getSIPType() {
    return SipType.EARK;
  }

  @Override
  public String build(Sip sip) {
    StringBuilder name = new StringBuilder();
    if (prefix != null && !"".equals(prefix)) {
      name.append(prefix).append(" - ");
    }
    switch (getSIPNameStrategy()) {
      case TITLE_ID:
        name.append(sip.getTitle());
        name.append(" - ");
        name.append(sip.getId());
        break;
      case TITLE_DATE:
        name.append(sip.getTitle());
        name.append(" - ");
        name.append(new SimpleDateFormat(Constants.DATE_FORMAT_1).format(new Date()));
        break;
      case ID:
      default:
        name.append(sip.getId());
        break;
    }
    return name.toString();
  }
}
