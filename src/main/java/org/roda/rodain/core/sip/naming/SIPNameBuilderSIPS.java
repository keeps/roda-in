package org.roda.rodain.core.sip.naming;

import org.roda.rodain.core.Constants;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class SIPNameBuilderSIPS extends SIPNameBuilderEARK {
  public SIPNameBuilderSIPS(String prefix, Constants.SipNameStrategy strategy) {
    super(prefix, strategy);
  }

  @Override
  public Constants.SipType getSIPType() {
    return Constants.SipType.EARK2S;
  }
}
