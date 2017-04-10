package org.roda.rodain.core.sip.naming;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Constants.SipType;
import org.roda.rodain.core.schema.Sip;

/**
 * SIP naming for the Hungarian SIP 4
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIPNameBuilderHungarian implements SIPNameBuilder {
  private String transferring;
  private String serial;
  private SipNameStrategy strategy;

  public SIPNameBuilderHungarian(String transferring, String serial, SipNameStrategy strategy) {
    this.transferring = transferring;
    this.serial = serial;
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
    return SipType.HUNGARIAN;
  }

  @Override
  public String build(Sip sip) {
    StringBuilder name = new StringBuilder("SIP_");
    switch (getSIPNameStrategy()) {
      case DATE_TRANSFERRING_SERIALNUMBER:
      default:
        name.append(new SimpleDateFormat(Constants.DATE_FORMAT_5).format(new Date())).append("_");
        name.append(transferring).append("_");
        name.append(serial);
        incrementSerial();
    }
    return name.toString();
  }

  private void incrementSerial() {
    try {
      serial = String.format(Constants.SIP_NAME_STRATEGY_SERIAL_FORMAT_NUMBER, Integer.valueOf(serial) + 1);
    } catch (NumberFormatException e) {
      // ignore and save the value as is, without incrementing
    }
  }
}
