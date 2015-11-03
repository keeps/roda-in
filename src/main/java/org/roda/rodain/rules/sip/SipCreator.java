package org.roda.rodain.rules.sip;

import java.util.List;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public interface SipCreator {
    List<SipPreview> getSips();
    int getCount();
    SipPreview getNext();
    boolean hasNext();
}
