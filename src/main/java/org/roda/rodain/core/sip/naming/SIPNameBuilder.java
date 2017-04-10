package org.roda.rodain.core.sip.naming;

import java.util.Set;

import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Constants.SipType;
import org.roda.rodain.core.schema.Sip;

/**
 * Stores all the values needed to create the SIP name, and provides a way to
 * build the name according to those parameters.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface SIPNameBuilder {
  SipNameStrategy getSIPNameStrategy();

  SipType getSIPType();

  Set<SipNameStrategy> getSupportedSIPNameStrategies();

  String build(Sip sip);
}
