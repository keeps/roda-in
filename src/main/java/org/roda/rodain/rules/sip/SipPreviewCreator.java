package org.roda.rodain.rules.sip;

import java.util.Map;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public interface SipPreviewCreator {
    Map<String, SipPreview> getSips();
    int getCount();
    SipPreview getNext();
    boolean hasNext();
}
