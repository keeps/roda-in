package org.roda.rodain.core.sip.naming;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Constants.SipType;

/**
 * Similar to EARK name builder.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIPNameBuilderEARK2 extends SIPNameBuilderEARK {
  public SIPNameBuilderEARK2(String prefix, SipNameStrategy strategy) {
    super(prefix, strategy);
  }

  @Override
  public SipType getSIPType() {
    return Constants.SipType.EARK2;
  }
}
