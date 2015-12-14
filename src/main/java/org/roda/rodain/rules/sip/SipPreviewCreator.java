package org.roda.rodain.rules.sip;

import java.util.Map;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public interface SipPreviewCreator {
  /**
   * @return The SIPs created.
   */
  Map<String, SipPreview> getSips();

  /**
   * @return The count of the created SIPs.
   */
  int getCount();

  /**
   * @return The next SIP in the created SIPs list.
   */
  SipPreview getNext();

  /**
   * @return True if the number of SIPs returned is smaller than the count of
   * added SIPs.
   */
  boolean hasNext();
}
